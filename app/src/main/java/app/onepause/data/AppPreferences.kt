package app.onepause.data

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var blockedPackages: Set<String>
        get() = prefs.getStringSet(KEY_BLOCKED_PACKAGES, emptySet())?.toSet().orEmpty()
        set(value) {
            prefs.edit().putStringSet(KEY_BLOCKED_PACKAGES, value.toSet()).apply()
        }

    var pauseSeconds: Int
        get() = prefs.getInt(KEY_PAUSE_SECONDS, DEFAULT_PAUSE_SECONDS)
            .coerceIn(MIN_PAUSE_SECONDS, MAX_PAUSE_SECONDS)
        set(value) {
            prefs.edit().putInt(KEY_PAUSE_SECONDS, value.coerceIn(MIN_PAUSE_SECONDS, MAX_PAUSE_SECONDS)).apply()
        }

    var reclaimedMinutesPerSkip: Int
        get() = prefs.getInt(KEY_RECLAIMED_MINUTES, DEFAULT_RECLAIMED_MINUTES)
            .coerceIn(MIN_RECLAIMED_MINUTES, MAX_RECLAIMED_MINUTES)
        set(value) {
            prefs.edit()
                .putInt(KEY_RECLAIMED_MINUTES, value.coerceIn(MIN_RECLAIMED_MINUTES, MAX_RECLAIMED_MINUTES))
                .apply()
        }

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS, true)
        set(value) {
            prefs.edit().putBoolean(KEY_HAPTICS, value).apply()
        }

    var onboardingComplete: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
        set(value) {
            prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, value).apply()
        }

    fun isBlocked(packageName: String): Boolean = packageName in blockedPackages

    fun setBlocked(packageName: String, blocked: Boolean) {
        val updated = blockedPackages.toMutableSet()
        if (blocked) updated += packageName else updated -= packageName
        blockedPackages = updated
    }

    fun resetAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "one_pause_preferences"
        private const val KEY_BLOCKED_PACKAGES = "blocked_packages"
        private const val KEY_PAUSE_SECONDS = "pause_seconds"
        private const val KEY_RECLAIMED_MINUTES = "reclaimed_minutes"
        private const val KEY_HAPTICS = "haptics"
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"

        const val DEFAULT_PAUSE_SECONDS = 10
        const val MIN_PAUSE_SECONDS = 5
        const val MAX_PAUSE_SECONDS = 20
        val PAUSE_OPTIONS = listOf(5, 10, 15, 20)

        const val DEFAULT_RECLAIMED_MINUTES = 5
        const val MIN_RECLAIMED_MINUTES = 2
        const val MAX_RECLAIMED_MINUTES = 30
        val RECLAIMED_OPTIONS = listOf(3, 5, 10)
    }
}
