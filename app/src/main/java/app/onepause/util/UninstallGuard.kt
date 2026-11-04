package app.onepause.util

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import app.onepause.R
import app.onepause.admin.UninstallGuardReceiver

/** Optional removal friction, never device-owner enrollment or a permanent uninstall block. */
object UninstallGuard {
    fun component(context: Context) = ComponentName(context, UninstallGuardReceiver::class.java)

    fun isActive(context: Context): Boolean =
        context.getSystemService(DevicePolicyManager::class.java)?.isAdminActive(component(context)) == true

    fun activationIntent(context: Context): Intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component(context))
        .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, context.getString(R.string.uninstall_guard_description))

    fun releaseIfUnprotected(context: Context) {
        if (!isAccessibilityServiceEnabled(context) && isActive(context)) {
            context.getSystemService(DevicePolicyManager::class.java)?.removeActiveAdmin(component(context))
        }
    }
}
