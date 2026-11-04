package app.onepause

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import app.onepause.data.AppPreferences
import app.onepause.ui.pause.PauseScreen
import app.onepause.ui.theme.OnePauseTheme
import app.onepause.ui.theme.isDarkAppearance

class PausePreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val preferences = AppPreferences(this)

        setContent {
            val dark = isDarkAppearance(preferences.appearanceMode)
            OnePauseTheme(dark = dark) {
                val view = LocalView.current
                SideEffect {
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = !dark
                        isAppearanceLightNavigationBars = !dark
                    }
                }
                val hapticFeedback = LocalHapticFeedback.current
                BackHandler { finish() }
                PauseScreen(
                    appLabel = null,
                    appPackage = null,
                    appIcon = null,
                    pauseSeconds = preferences.pauseSeconds,
                    isPreview = true,
                    onAvoid = { finish() },
                    onContinue = { finish() },
                    onPhaseChange = {
                        if (preferences.hapticsEnabled) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                )
            }
        }
    }
}
