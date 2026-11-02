package app.onepause.util

import java.time.Duration
import java.util.Locale

fun formatMinutes(minutes: Int): String {
    if (minutes < 60) return "$minutes min"
    val hours = minutes / 60
    val remainder = minutes % 60
    return if (remainder == 0) "${hours}h" else "${hours}h ${remainder}m"
}

fun formatLongMinutes(minutes: Int): String {
    if (minutes < 60) return "$minutes minutes"
    val hours = minutes / 60
    val remainder = minutes % 60
    return if (remainder == 0) {
        if (hours == 1) "1 hour" else "$hours hours"
    } else {
        "${hours}h ${remainder}m"
    }
}

fun formatPauseLength(seconds: Int): String = "${seconds}s"

fun formatDurationShort(duration: Duration): String {
    val minutes = duration.toMinutes().toInt()
    return formatMinutes(minutes)
}

fun formatPercent(value: Int, total: Int): String {
    if (total <= 0) return "0%"
    return "${((value.toDouble() / total) * 100).toInt()}%"
}

fun pluralize(count: Int, singular: String, plural: String = "${singular}s"): String =
    if (count == 1) singular else plural

fun appNameOrPackage(packageName: String, label: String?): String {
    if (!label.isNullOrBlank()) return label
    val fallback = packageName.substringAfterLast('.').replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
    return fallback
}
