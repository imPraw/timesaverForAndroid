package app.onepause.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.onepause.ui.components.SoftCard
import app.onepause.util.UninstallGuard
import kotlinx.coroutines.delay

@Composable
internal fun UninstallGuardCard(serviceEnabled: Boolean, onOpenProtection: () -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var active by remember { mutableStateOf(UninstallGuard.isActive(context)) }
    var refresh by remember { mutableIntStateOf(0) }
    var explain by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        refresh++ // Read Android's state; cancelling consent never turns the guard on.
    }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh++
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(serviceEnabled, refresh) {
        UninstallGuard.releaseIfUnprotected(context)
        // Admin removal is asynchronous, so allow Android to deliver its deactivation broadcast.
        repeat(4) {
            active = UninstallGuard.isActive(context)
            delay(250)
        }
    }
    SoftCard {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("Uninstall guard", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                if (active) "On · an extra step before uninstalling" else "Off · optional extra friction",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Android asks you to deactivate this guard before removing One Pause. It is not a permanent lock: you can always deactivate it in Android Settings.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Turn off app protection in Accessibility to release the guard. If Android has stopped One Pause, reopen it to finish. Turning protection on again requires your approval to restore the guard.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            FilledTonalButton(onClick = {
                error = null
                if (active || !serviceEnabled) onOpenProtection() else explain = true
            }) {
                Text(when {
                    active -> "Turn off protection to remove guard"
                    !serviceEnabled -> "Enable app protection first"
                    else -> "Set up uninstall guard"
                })
            }
            if (active) {
                TextButton(onClick = {
                    runCatching { context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS)) }
                        .onFailure { error = "Open Android Settings and search for Device admin apps to deactivate the guard." }
                }) { Text("Manage in Android Settings") }
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        }
    }
    if (explain) {
        AlertDialog(
            onDismissRequest = { explain = false },
            title = { Text("Add an uninstall step?") },
            text = { Text("Android will ask you to activate One Pause as a device administrator. We request no wipe, password, or screen-lock controls. This adds removal friction, but you can deactivate it in Android Settings at any time.") },
            confirmButton = {
                TextButton(onClick = {
                    explain = false
                    runCatching { launcher.launch(UninstallGuard.activationIntent(context)) }
                        .onFailure { error = "This phone could not open device administrator setup. The guard is still off." }
                }) { Text("Continue to Android setup") }
            },
            dismissButton = { TextButton(onClick = { explain = false }) { Text("Not now") } }
        )
    }
}
