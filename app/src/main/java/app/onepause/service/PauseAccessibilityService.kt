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
import app.onepause.util.WebsiteDomain

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
    private var currentTarget: String? = null
    private var foregroundPackage: String? = null
    private val cooldownUntil = mutableMapOf<String, Long>()
    private var launchableHomePackages: Set<String> = emptySet()
    private var browserCheckScheduled = false
    private val browserCheck = object : Runnable {
        override fun run() {
            browserCheckScheduled = false
            if (overlayVisible) return
            val packageName = rootInActiveWindow?.packageName?.toString() ?: return
            if (packageName !in BROWSER_ADDRESS_IDS) return
            foregroundPackage = packageName
            checkWebsite(packageName)
            if (!overlayVisible) scheduleBrowserCheck(BROWSER_CHECK_INTERVAL_MS)
        }
    }

    private fun scheduleBrowserCheck(delay: Long = 200L) {
        if (browserCheckScheduled) return
        browserCheckScheduled = true
        handler.postDelayed(browserCheck, delay)
    }

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
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return
        val packageName = event.packageName?.toString()?.takeIf { it.isNotBlank() } ?: return
        if (packageName == this.packageName) return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) foregroundPackage = packageName

        if (overlayVisible) {
            if (!finishingPause && packageName != currentPackage && packageName in launchableHomePackages &&
                event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                finishPause(PauseDecision.AVOIDED)
            }
            return
        }
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && shouldPause(packageName)) {
            showPause(packageName)
        }
        // Window-state events can come from the keyboard or browser dialogs, rather than
        // the browser itself. Inspect the active window after it has settled instead of
        // trusting the package on the last event.
        scheduleBrowserCheck()
    }

    private fun checkWebsite(packageName: String) {
        if (preferences.blockedDomains.isEmpty()) return
        val ids = BROWSER_ADDRESS_IDS[packageName] ?: return
        val root = rootInActiveWindow ?: return
        if (root.packageName?.toString() != packageName) return
        for (id in ids) {
            val nodes = root.findAccessibilityNodeInfosByViewId("$packageName:id/$id")
            for (node in nodes) {
                if (!node.isVisibleToUser || node.isFocused) continue
                val address = node.text?.toString().orEmpty().ifBlank {
                    node.contentDescription?.toString().orEmpty()
                }
                val host = WebsiteDomain.parse(address) ?: continue
                val domain = preferences.blockedDomains.firstOrNull { WebsiteDomain.matches(host, it) } ?: continue
                val target = "website:$domain"
                if (SystemClock.elapsedRealtime() >= (cooldownUntil[target] ?: 0L)) {
                    showPause(packageName, target, domain)
                    return
                }
            }
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        stopObservingProtection()
        handler.removeCallbacksAndMessages(null)
        browserCheckScheduled = false
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
        browserCheckScheduled = false
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

    private fun showPause(packageName: String, target: String = packageName, label: String? = null) {
        val controller = overlayController ?: return
        val appInfo = runCatching {
            val info = packageManager.getApplicationInfo(packageName, 0)
            val label = packageManager.getApplicationLabel(info).toString()
            val icon = packageManager.getApplicationIcon(info)
            label to icon
        }.getOrNull() ?: return

        currentPackage = packageName
        currentTarget = target
        overlayVisible = true

        val shown = controller.show(
            appPackage = packageName,
            appLabel = label ?: appInfo.first,
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
        val target = currentTarget

        if (packageName != null && target != null) {
            if (decision == PauseDecision.CONTINUED) {
                cooldownUntil[target] = SystemClock.elapsedRealtime() + CONTINUE_COOLDOWN_MS
            } else {
                cooldownUntil.remove(target)
            }
            PauseEventStore.record(this, packageName, decision)
        }

        val dismiss = Runnable {
            val controller = overlayController
            if (controller != null) {
                controller.hide(animated = true) {
                    overlayVisible = false
                    currentPackage = null
                    finishingPause = false
                    // Recheck the actual foreground window if it changed while the overlay hid.
                    val foreground = rootInActiveWindow?.packageName?.toString() ?: foregroundPackage
                    if (foreground != null && foreground !in launchableHomePackages &&
                        (decision != PauseDecision.CONTINUED || foreground != packageName)) {
                        if (shouldPause(foreground)) showPause(foreground) else checkWebsite(foreground)
                    }
                    if (!overlayVisible) scheduleBrowserCheck()
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
        private const val BROWSER_CHECK_INTERVAL_MS = 500L
        private val BROWSER_ADDRESS_IDS = mapOf(
            "com.android.chrome" to listOf("url_bar"),
            "com.chrome.beta" to listOf("url_bar"),
            "com.chrome.dev" to listOf("url_bar"),
            "com.chrome.canary" to listOf("url_bar"),
            "com.brave.browser" to listOf("url_bar"),
            "com.microsoft.emmx" to listOf("url_bar"),
            "com.sec.android.app.sbrowser" to listOf("location_bar_edit_text"),
            "org.mozilla.firefox" to listOf("mozac_browser_toolbar_url_view", "url_bar_title"),
            "org.mozilla.firefox_beta" to listOf("mozac_browser_toolbar_url_view", "url_bar_title")
        )
    }
}
