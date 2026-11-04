package app.onepause.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import app.onepause.data.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable
)

data class LightAppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?
)

val suggestedPackages = setOf(
    "com.instagram.android",
    "com.zhiliaoapp.musically",
    "com.ss.android.ugc.trill",
    "com.google.android.youtube",
    "com.twitter.android",
    "com.facebook.katana",
    "com.facebook.lite",
    "com.snapchat.android",
    "com.reddit.frontpage",
    "com.linkedin.android",
    "com.pinterest",
    "com.discord",
    "org.telegram.messenger",
    "com.spotify.music",
    "com.netflix.mediaclient",
    "com.google.android.apps.youtube.music"
)

fun PackageManager.isLauncherPackage(packageName: String): Boolean {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    return runCatching {
        queryIntentActivities(intent, 0).any { it.activityInfo.packageName == packageName }
    }.getOrDefault(false)
}

suspend fun loadLaunchableApps(context: Context): List<InstalledApp> = withContext(Dispatchers.IO) {
    val packageManager = context.packageManager
    val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val homePackages = runCatching {
        packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
            0
        ).mapTo(mutableSetOf()) { it.activityInfo.packageName }
    }.getOrDefault(emptySet())

    val byPackage = linkedMapOf<String, InstalledApp>()
    runCatching {
        packageManager.queryIntentActivities(launchIntent, 0)
    }.getOrDefault(emptyList())
        .asSequence()
        .filter { it.activityInfo.enabled }
        .map { it.activityInfo.applicationInfo }
        .filter { it.enabled }
        .filter { it.packageName != context.packageName }
        .filter { it.packageName !in homePackages }
        .forEach { appInfo ->
            if (appInfo.packageName in byPackage) return@forEach
            runCatching {
                val label = packageManager.getApplicationLabel(appInfo).toString()
                val icon = packageManager.getApplicationIcon(appInfo)
                byPackage[appInfo.packageName] = InstalledApp(
                    packageName = appInfo.packageName,
                    label = label,
                    icon = icon
                )
            }
        }
    byPackage.values.sortedBy { it.label.lowercase() }
}

fun loadLightAppInfo(context: Context, packageName: String): LightAppInfo {
    if (packageName.startsWith("website:")) {
        return LightAppInfo(packageName, packageName.removePrefix("website:"), null)
    }
    val packageManager = context.packageManager
    return runCatching {
        val info = packageManager.getApplicationInfo(packageName, 0)
        LightAppInfo(
            packageName = packageName,
            label = packageManager.getApplicationLabel(info).toString(),
            icon = packageManager.getApplicationIcon(info)
        )
    }.getOrDefault(
        LightAppInfo(packageName = packageName, label = packageName.substringAfterLast('.'), icon = null)
    )
}

suspend fun loadSelectedAppInfo(context: Context, preferences: AppPreferences): List<LightAppInfo> {
    return withContext(Dispatchers.IO) {
        preferences.blockedPackages
            .map { loadLightAppInfo(context, it) }
            .sortedBy { it.label.lowercase() }
    }
}
