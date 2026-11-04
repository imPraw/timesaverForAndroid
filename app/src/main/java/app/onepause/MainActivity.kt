package app.onepause

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import app.onepause.data.AppPreferences
import app.onepause.ui.MainShell
import app.onepause.ui.onboarding.OnboardingScreen
import app.onepause.ui.theme.OnePauseTheme
import app.onepause.util.isAccessibilityServiceEnabled
import app.onepause.util.openAccessibilitySettings

class MainActivity : ComponentActivity() {

    private lateinit var preferences: AppPreferences

    private var serviceEnabled by mutableStateOf(false)
    private var onboardingComplete by mutableStateOf(false)
    private var resumeTick by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        preferences = AppPreferences(this)
        onboardingComplete = preferences.onboardingComplete
        serviceEnabled = isAccessibilityServiceEnabled(this)

        setContent {
            OnePauseTheme(dark = !onboardingComplete || androidx.compose.foundation.isSystemInDarkTheme()) {
                val view = LocalView.current
                val dark = androidx.compose.foundation.isSystemInDarkTheme()
                SideEffect {
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = onboardingComplete && !dark
                        isAppearanceLightNavigationBars = onboardingComplete && !dark
                    }
                }

                if (!onboardingComplete) {
                    OnboardingScreen(
                        serviceEnabled = serviceEnabled,
                        onOpenAccessibilitySettings = { openAccessibilitySettings(this) },
                        onFinish = {
                            preferences.onboardingComplete = true
                            onboardingComplete = true
                        }
                    )
                } else {
                    MainShell(
                        preferences = preferences,
                        serviceEnabled = serviceEnabled,
                        resumeTick = resumeTick
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::preferences.isInitialized) {
            app.onepause.util.UninstallGuard.releaseIfUnprotected(this)
            serviceEnabled = isAccessibilityServiceEnabled(this)
            resumeTick++
        }
    }
}
