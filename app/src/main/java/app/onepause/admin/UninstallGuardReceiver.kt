package app.onepause.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import app.onepause.R
import app.onepause.util.UninstallGuard

class UninstallGuardReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        // Protection may have been switched off while Android's consent screen was open.
        UninstallGuard.releaseIfUnprotected(context)
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence =
        context.getString(R.string.uninstall_guard_disable_message)
}
