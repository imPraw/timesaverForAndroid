package app.onepause.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

enum class PauseDecision {
    AVOIDED,
    CONTINUED,
    LEFT
}

data class PauseEvent(
    val timestamp: Long,
    val packageName: String,
    val decision: PauseDecision
)

/**
 * Small, local-only event log. It is intentionally stored as JSON in SharedPreferences:
 * the expected volume is tiny and it keeps the app dependency-free and easy to audit.
 */
object PauseEventStore {
    private const val PREFS_NAME = "one_pause_events"
    private const val KEY_EVENTS = "events"
    private const val MAX_EVENTS = 4000
    private val lock = Any()

    fun record(context: Context, packageName: String, decision: PauseDecision): PauseEvent {
        val event = PauseEvent(
            timestamp = System.currentTimeMillis(),
            packageName = packageName,
            decision = decision
        )
        synchronized(lock) {
            val events = readUnsafe(context).toMutableList()
            events += event
            writeUnsafe(context, events.takeLast(MAX_EVENTS))
        }
        return event
    }

    fun all(context: Context): List<PauseEvent> = synchronized(lock) {
        readUnsafe(context)
    }

    fun clear(context: Context) {
        synchronized(lock) {
            prefs(context).edit().remove(KEY_EVENTS).apply()
        }
    }

    private fun prefs(context: Context) = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun readUnsafe(context: Context): List<PauseEvent> {
        val raw = prefs(context).getString(KEY_EVENTS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val decision = runCatching {
                        PauseDecision.valueOf(item.optString("decision").uppercase())
                    }.getOrNull() ?: continue
                    add(
                        PauseEvent(
                            timestamp = item.optLong("timestamp"),
                            packageName = item.optString("package"),
                            decision = decision
                        )
                    )
                }
            }.filter { it.timestamp > 0 && it.packageName.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    private fun writeUnsafe(context: Context, events: List<PauseEvent>) {
        val array = JSONArray()
        events.forEach { event ->
            array.put(
                JSONObject()
                    .put("timestamp", event.timestamp)
                    .put("package", event.packageName)
                    .put("decision", event.decision.name.lowercase())
            )
        }
        prefs(context).edit().putString(KEY_EVENTS, array.toString()).apply()
    }
}
