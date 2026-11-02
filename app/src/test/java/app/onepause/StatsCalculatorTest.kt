package app.onepause

import app.onepause.data.PauseDecision
import app.onepause.data.PauseEvent
import app.onepause.data.StatsCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsCalculatorTest {

    private val zone = ZoneId.of("UTC")
    private val now = Instant.parse("2026-11-04T12:00:00Z") // Wednesday

    private fun event(day: String, hour: Int, pkg: String, decision: PauseDecision): PauseEvent {
        val date = LocalDate.parse(day)
        val timestamp = LocalDateTime.of(date, java.time.LocalTime.of(hour, 0))
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
        return PauseEvent(timestamp, pkg, decision)
    }

    @Test
    fun `empty history has no counts and no streak`() {
        val summary = StatsCalculator.calculate(emptyList(), 5, now, zone)

        assertEquals(0, summary.totalPauses)
        assertEquals(0, summary.allTimeReclaimedMinutes)
        assertEquals(0, summary.streakDays)
        assertEquals(7, summary.lastSevenDays.size)
        assertEquals(0, summary.lastSevenDays.sumOf { it.pauses })
    }

    @Test
    fun `counts decisions and reclaims only avoided sessions`() {
        val events = listOf(
            event("2026-11-04", 8, "com.instagram.android", PauseDecision.AVOIDED),
            event("2026-11-04", 9, "com.instagram.android", PauseDecision.CONTINUED),
            event("2026-11-04", 10, "com.zhiliaoapp.musically", PauseDecision.AVOIDED),
            event("2026-11-03", 20, "com.zhiliaoapp.musically", PauseDecision.LEFT)
        )

        val summary = StatsCalculator.calculate(events, 5, now, zone)

        assertEquals(4, summary.totalPauses)
        assertEquals(2, summary.totalAvoided)
        assertEquals(1, summary.totalContinued)
        assertEquals(1, summary.totalLeft)
        assertEquals(3, summary.todayPauses)
        assertEquals(2, summary.todayAvoided)
        assertEquals(4, summary.weekPauses)
        assertEquals(2, summary.weekAvoided)
        assertEquals(10, summary.allTimeReclaimedMinutes)
        assertEquals(10, summary.weekReclaimedMinutes)
        assertEquals(2, summary.streakDays)
    }

    @Test
    fun `seven day chart runs from oldest to today`() {
        val events = listOf(
            event("2026-10-29", 8, "com.reddit.frontpage", PauseDecision.AVOIDED),
            event("2026-11-04", 9, "com.reddit.frontpage", PauseDecision.AVOIDED),
            event("2026-11-04", 10, "com.reddit.frontpage", PauseDecision.AVOIDED)
        )

        val summary = StatsCalculator.calculate(events, 3, now, zone)

        assertEquals(LocalDate.parse("2026-10-29"), summary.lastSevenDays.first().date)
        assertEquals(LocalDate.parse("2026-11-04"), summary.lastSevenDays.last().date)
        assertEquals(2, summary.lastSevenDays.last().pauses)
        assertEquals(1, summary.lastSevenDays.first().pauses)
        assertEquals(3, summary.appStats.first().pauses)
    }

    @Test
    fun `streak survives when today is empty but yesterday was active`() {
        val events = listOf(
            event("2026-11-03", 8, "com.instagram.android", PauseDecision.AVOIDED),
            event("2026-11-02", 8, "com.instagram.android", PauseDecision.AVOIDED)
        )

        val summary = StatsCalculator.calculate(events, 5, now, zone)

        assertEquals(2, summary.streakDays)
    }

    @Test
    fun `previous week counts separate from current week`() {
        val events = listOf(
            event("2026-10-28", 8, "com.instagram.android", PauseDecision.AVOIDED),
            event("2026-11-04", 8, "com.instagram.android", PauseDecision.AVOIDED)
        )

        val summary = StatsCalculator.calculate(events, 10, now, zone)

        assertEquals(1, summary.weekAvoided)
        assertEquals(1, summary.lastWeekAvoided)
        assertEquals(10, summary.weekReclaimedMinutes)
        assertEquals(20, summary.allTimeReclaimedMinutes)
    }
}
