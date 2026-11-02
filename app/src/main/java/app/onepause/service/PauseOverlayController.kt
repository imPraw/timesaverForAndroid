package app.onepause.service

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import app.onepause.data.PauseDecision
import app.onepause.ui.pause.PauseScreen
import app.onepause.ui.theme.OnePauseTheme

/**
 * Draws the pause as an accessibility overlay. Using TYPE_ACCESSIBILITY_OVERLAY avoids the
 * draw-over-other-apps permission and lets the pause appear immediately over the target app.
 */
class PauseOverlayController(
    private val service: AccessibilityService,
    private val onDecision: (PauseDecision) -> Unit
) {
    private val windowManager = service.getSystemService(WindowManager::class.java)
    private val handler = Handler(Looper.getMainLooper())

    private var composeView: ComposeView? = null
    private var overlayOwner: OverlayOwner? = null
    private var decided = false
    private var showing = false

    private val timeout = Runnable {
        if (!decided) {
            decided = true
            onDecision(PauseDecision.LEFT)
        }
    }

    fun show(
        appPackage: String,
        appLabel: String,
        appIcon: Drawable?,
        pauseSeconds: Int,
        hapticsEnabled: Boolean,
        onBreathPhase: (Boolean) -> Unit
    ): Boolean {
        if (showing) return false
        decided = false

        val owner = OverlayOwner().also { it.onCreate() }
        val view = ComposeView(service).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            isFocusable = true
            isFocusableInTouchMode = true
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    decide(PauseDecision.AVOIDED)
                    true
                } else {
                    false
                }
            }
            setContent {
                OnePauseTheme {
                    PauseScreen(
                        appLabel = appLabel,
                        appPackage = appPackage,
                        appIcon = appIcon,
                        pauseSeconds = pauseSeconds,
                        isPreview = false,
                        onAvoid = { decide(PauseDecision.AVOIDED) },
                        onContinue = { decide(PauseDecision.CONTINUED) },
                        onPhaseChange = { inhaling ->
                            if (hapticsEnabled) onBreathPhase(inhaling)
                        }
                    )
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            title = "One Pause"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        return try {
            windowManager.addView(view, params)
            owner.onResume()
            view.requestFocus()
            composeView = view
            overlayOwner = owner
            showing = true
            handler.postDelayed(timeout, 90_000L)
            true
        } catch (error: Exception) {
            owner.destroy()
            runCatching { windowManager.removeViewImmediate(view) }
            false
        }
    }

    fun hide() {
        if (!showing) return
        showing = false
        decided = true
        handler.removeCallbacks(timeout)
        val view = composeView
        val owner = overlayOwner
        composeView = null
        overlayOwner = null
        if (view != null) {
            runCatching { windowManager.removeViewImmediate(view) }
        }
        owner?.destroy()
    }

    private fun decide(decision: PauseDecision) {
        if (decided) return
        decided = true
        handler.removeCallbacks(timeout)
        onDecision(decision)
    }

    private class OverlayOwner : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateController = SavedStateRegistryController.create(this)

        override val lifecycle: Lifecycle get() = lifecycleRegistry
        override val savedStateRegistry: SavedStateRegistry
            get() = savedStateController.savedStateRegistry
        override val viewModelStore: ViewModelStore = ViewModelStore()

        fun onCreate() {
            savedStateController.performRestore(null)
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }

        fun onResume() {
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        }

        fun destroy() {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
            viewModelStore.clear()
        }
    }
}
