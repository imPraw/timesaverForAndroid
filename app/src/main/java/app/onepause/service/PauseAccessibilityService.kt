package app.onepause.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import app.onepause.data.AppPreferences
import app.onepause.data.PauseDecision
import app.onepause.data.PauseEventStore
import app.onepause.util.UninstallGuard

class PauseAccessibilityService : AccessibilityService() {

    private lateinit var preferences: AppPreferences
    private var overlayController: PauseOverlayController? = null
    private val handler = Handler(Looper.getMainLooper())
    private var observingProtection = false
    private val protectionObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            UninstallGuard.releaseIfUnprotected(this@PauseAccessibilityService)
        }
    }

    private var finishingPause = false
    private var overlayVisible = false
    private var currentPackage: String? = null
    private var currentLabel: String = ""
    private val cooldownUntil = mutableMapOf<String, Long>()
    private var launchableHomePackages: Set<String> = emptySet()

    override fun onServiceConnected() {
        super.onServiceConnected()
        if (!observingProtection) {
            listOf(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, Settings.Secure.ACCESSIBILITY_ENABLED)
                .forEach { setting ->
                    contentResolver.registerContentObserver(
                        Settings.Secure.getUriFor(setting), false, protectionObserver
                    )
                }
            observingProtection = true
        }
        preferences = AppPreferences(this)
        launchableHomePackages = resolveHomePackages()
        overlayController = PauseOverlayController(this) { decision ->
            finishPause(decision)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString()?.takeIf { it.isNotBlank() } ?: return
        if (packageName == this.packageName) return

        if (overlayVisible) {
            if (packageName != currentPackage && packageName in launchableHomePackages) {
                // Pressing Home while the pause is visible is a clear "leave it for now".
                finishPause(PauseDecision.AVOIDED)
            }
            return
        }

        if (shouldPause(packageName)) {
            showPause(packageName)
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        stopObservingProtection()
        handler.removeCallbacksAndMessages(null)
        finishingPause = false
        overlayController?.hide()
        overlayController = null
        overlayVisible = false
        currentPackage = null
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopObservingProtection()
        handler.removeCallbacksAndMessages(null)
        finishingPause = false
        UninstallGuard.releaseIfUnprotected(this)
        overlayController?.hide()
        overlayVisible = false
        currentPackage = null
        return super.onUnbind(intent)
    }

    private fun stopObservingProtection() {
        if (observingProtection) {
            contentResolver.unregisterContentObserver(protectionObserver)
            observingProtection = false
        }
    }

    private fun shouldPause(packageName: String): Boolean {
        if (!preferences.blockedPackages.contains(packageName)) return false
        if (packageName == this.packageName) return false
        if (packageName in launchableHomePackages) return false
        val cooldown = cooldownUntil[packageName] ?: 0L
        return SystemClock.elapsedRealtime() >= cooldown
    }

    private fun showPause(packageName: String) {
        val controller = overlayController ?: return
        val appInfo = runCatching {
            val info = packageManager.getApplicationInfo(packageName, 0)
            val label = packageManager.getApplicationLabel(info).toString()
            val icon = packageManager.getApplicationIcon(info)
            label to icon
        }.getOrNull() ?: return

        currentPackage = packageName
        currentLabel = appInfo.first
        overlayVisible = true

        val shown = controller.show(
            appPackage = packageName,
            appLabel = appInfo.first,
            appIcon = appInfo.second,
            pauseSeconds = preferences.pauseSeconds,
            hapticsEnabled = preferences.hapticsEnabled,
            onBreathPhase = { vibrateTick() }
        )

        if (!shown) {
            overlayVisible = false
            currentPackage = null
        }
    }

    private fun finishPause(decision: PauseDecision) {
        if (!overlayVisible || finishingPause) return
        finishingPause = true
        val packageName = currentPackage

        if (packageName != null) {
            cooldownUntil[packageName] = SystemClock.elapsedRealtime() +
                if (decision == PauseDecision.CONTINUED) CONTINUE_COOLDOWN_MS else LEAVE_COOLDOWN_MS
            PauseEventStore.record(this, packageName, decision)
        }

        val dismiss = Runnable {
            val controller = overlayController
            if (controller != null) {
                controller.hide(animated = true) {
                    overlayVisible = false
                    currentPackage = null
                    finishingPause = false
                }
            } else {
                overlayVisible = false
                currentPackage = null
                finishingPause = false
            }
        }
        if (decision != PauseDecision.CONTINUED) {
            // Keep the app covered while the launcher comes forward, then gently reveal Home.
            performGlobalAction(GLOBAL_ACTION_HOME)
            handler.postDelayed(dismiss, 180L)
        } else {
            dismiss.run()
        }
    }

    private fun vibrateTick() {
        if (!preferences.hapticsEnabled) return
        runCatching {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(VibratorManager::class.java).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Vibrator::class.java)
            }
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        14L,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }
        }
    }

    private fun resolveHomePackages(): Set<String> = runCatching {
        packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
            0
        ).mapTo(mutableSetOf()) { it.activityInfo.packageName }
    }.getOrDefault(emptySet())

    companion object {
        private const val CONTINUE_COOLDOWN_MS = 60_000L
        private const val LEAVE_COOLDOWN_MS = 4_000L
    }
}
