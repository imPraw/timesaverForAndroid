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
import app.onepause.ui.theme.isDarkAppearance
import app.onepause.util.isAccessibilityServiceEnabled
import app.onepause.util.openAccessibilitySettings

class MainActivity : ComponentActivity() {

    private lateinit var preferences: AppPreferences

    private var serviceEnabled by mutableStateOf(false)
    private var onboardingComplete by mutableStateOf(false)
    private var resumeTick by mutableIntStateOf(0)
    private var appearanceMode by mutableStateOf("system")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        preferences = AppPreferences(this)
        appearanceMode = preferences.appearanceMode
        onboardingComplete = preferences.onboardingComplete
        serviceEnabled = isAccessibilityServiceEnabled(this)

        setContent {
            val dark = isDarkAppearance(appearanceMode)
            OnePauseTheme(dark = dark) {
                val view = LocalView.current
                SideEffect {
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = !dark
                        isAppearanceLightNavigationBars = !dark
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
                        resumeTick = resumeTick,
                        appearanceMode = appearanceMode,
                        onAppearanceModeChange = {
                            preferences.appearanceMode = it
                            appearanceMode = it
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::preferences.isInitialized) {
            appearanceMode = preferences.appearanceMode
            app.onepause.util.UninstallGuard.releaseIfUnprotected(this)
            serviceEnabled = isAccessibilityServiceEnabled(this)
            resumeTick++
        }
    }
}
