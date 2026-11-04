package app.onepause.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import app.onepause.service.PauseAccessibilityService

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    if (Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED,
            0
        ) != 1
    ) return false
    val expected = ComponentName(context, PauseAccessibilityService::class.java)
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices
        .split(':')
        .mapNotNull { ComponentName.unflattenFromString(it) }
        .any { it == expected }
}

fun openAccessibilitySettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
