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
import app.onepause.data.AppPreferences
import app.onepause.ui.pause.PauseScreen
import app.onepause.ui.theme.OnePauseTheme

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
            OnePauseTheme(dark = true) {
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
