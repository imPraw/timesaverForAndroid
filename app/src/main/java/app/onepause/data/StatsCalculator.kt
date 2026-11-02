package app.onepause.data

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

data class DayStat(
    val date: LocalDate,
    val pauses: Int,
    val avoided: Int
)

data class AppStat(
    val packageName: String,
    val pauses: Int,
    val avoided: Int
)

data class EventSummary(
    val totalPauses: Int,
    val totalAvoided: Int,
    val totalContinued: Int,
    val totalLeft: Int,
    val todayPauses: Int,
    val todayAvoided: Int,
    val weekPauses: Int,
    val weekAvoided: Int,
    val lastWeekAvoided: Int,
    val allTimeReclaimedMinutes: Int,
    val weekReclaimedMinutes: Int,
    val streakDays: Int,
    val lastSevenDays: List<DayStat>,
    val appStats: List<AppStat>
)

object StatsCalculator {

    fun calculate(
        events: List<PauseEvent>,
        reclaimedMinutesPerSkip: Int,
        now: Instant = Instant.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): EventSummary {
        val today = now.atZone(zone).toLocalDate()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val lastWeekStart = weekStart.minusWeeks(1)
        val valid = events.filter { it.timestamp > 0L }

        val dateOf: (PauseEvent) -> LocalDate = {
            Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate()
        }

        val avoided = valid.filter { it.decision == PauseDecision.AVOIDED }
        val continued = valid.filter { it.decision == PauseDecision.CONTINUED }
        val left = valid.filter { it.decision == PauseDecision.LEFT }

        val todayEvents = valid.filter { dateOf(it) == today }
        val weekEvents = valid.filter { dateOf(it) >= weekStart && dateOf(it) <= today }
        val lastWeekEvents = valid.filter { dateOf(it) >= lastWeekStart && dateOf(it) < weekStart }

        val dayMap = valid.groupBy(dateOf)
        val lastSevenDays = (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val dayEvents = dayMap[date].orEmpty()
            DayStat(
                date = date,
                pauses = dayEvents.size,
                avoided = dayEvents.count { it.decision == PauseDecision.AVOIDED }
            )
        }

        val apps = valid.groupBy { it.packageName }
            .map { (packageName, appEvents) ->
                AppStat(
                    packageName = packageName,
                    pauses = appEvents.size,
                    avoided = appEvents.count { it.decision == PauseDecision.AVOIDED }
                )
            }
            .sortedWith(compareByDescending<AppStat> { it.pauses }.thenBy { it.packageName })

        val activeDays = dayMap.keys.sortedDescending()
        var streakStart = if (today in activeDays) today else today.minusDays(1)
        var streak = 0
        while (streakStart in activeDays) {
            streak++
            streakStart = streakStart.minusDays(1)
        }

        return EventSummary(
            totalPauses = valid.size,
            totalAvoided = avoided.size,
            totalContinued = continued.size,
            totalLeft = left.size,
            todayPauses = todayEvents.size,
            todayAvoided = todayEvents.count { it.decision == PauseDecision.AVOIDED },
            weekPauses = weekEvents.size,
            weekAvoided = weekEvents.count { it.decision == PauseDecision.AVOIDED },
            lastWeekAvoided = lastWeekEvents.count { it.decision == PauseDecision.AVOIDED },
            allTimeReclaimedMinutes = avoided.size * reclaimedMinutesPerSkip,
            weekReclaimedMinutes = weekEvents.count { it.decision == PauseDecision.AVOIDED } *
                reclaimedMinutesPerSkip,
            streakDays = streak,
            lastSevenDays = lastSevenDays,
            appStats = apps
        )
    }
}
