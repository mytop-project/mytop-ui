package dev.droidtop.lib.ui.format

// droidtop runtime-common's dev.droidtop.library.format ValueFormatTest.kt, moved here (Droidtop/tracker#867):
// every size, time and state a person reads goes through one formatter, in every UI. SPEC references are droidtop's.

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The one value formatter (docs/SPEC.md 7k, "Values"; Droidtop/tracker#485 F2a). These cases used to live
 * beside five private copies (the PC game page, the companion, the theme renderer, Containers, the store
 * content sheet); they are held here once so the copies cannot come back with their own rules.
 */
class ValueFormatTest {
    private val day = 24L * 60 * 60 * 1000
    private val now = 1_700_000_000_000L

    @Test
    fun `sizes are decimal, whole below a gigabyte and one decimal above`() {
        assertEquals("0 KB", ValueFormat.size(0))
        assertEquals("1 KB", ValueFormat.size(10))
        assertEquals("12 KB", ValueFormat.size(12_400))
        assertEquals("420 MB", ValueFormat.size(420_000_000L))
        assertEquals("1.5 GB", ValueFormat.size(1_500_000_000L))
        assertEquals("2.0 TB", ValueFormat.size(2_000_000_000_000L))
        assertEquals("", ValueFormat.sizeOrBlank(0))
        assertEquals("12.3 GB free of 128.0 GB", ValueFormat.freeOf(12_300_000_000L, 128_000_000_000L))
    }

    @Test
    fun `days ago reads today, yesterday, days, weeks, months, then over a year`() {
        assertEquals("today", ValueFormat.daysAgo(now, now))
        assertEquals("today", ValueFormat.daysAgo(now, now - day + 1))
        // A clock slightly behind the last play never reads in the future.
        assertEquals("today", ValueFormat.daysAgo(now, now + 5 * 60 * 1000))
        assertEquals("yesterday", ValueFormat.daysAgo(now, now - day))
        assertEquals("yesterday", ValueFormat.daysAgo(now, now - 2 * day + 1))
        assertEquals("2 days ago", ValueFormat.daysAgo(now, now - 2 * day))
        assertEquals("13 days ago", ValueFormat.daysAgo(now, now - 13 * day))
        assertEquals("2 weeks ago", ValueFormat.daysAgo(now, now - 14 * day))
        assertEquals("8 weeks ago", ValueFormat.daysAgo(now, now - 59 * day))
        assertEquals("2 months ago", ValueFormat.daysAgo(now, now - 60 * day))
        assertEquals("12 months ago", ValueFormat.daysAgo(now, now - 364 * day))
        assertEquals("over a year ago", ValueFormat.daysAgo(now, now - 365 * day))
    }

    @Test
    fun `a recent check reads in minutes and hours`() {
        assertEquals("Just now", ValueFormat.ago(now, now - 30_000))
        assertEquals("5 min ago", ValueFormat.ago(now, now - 5 * 60_000))
        assertEquals("2 h ago", ValueFormat.ago(now, now - 2 * 3_600_000))
        assertEquals("Yesterday", ValueFormat.ago(now, now - day - 1000))
    }

    @Test
    fun `durations leave out what is zero and never read 0 min`() {
        assertEquals("Not played yet", ValueFormat.duration(0, none = "Not played yet"))
        assertEquals("", ValueFormat.duration(-5))
        assertEquals("Under a minute", ValueFormat.duration(59))
        assertEquals("1 min", ValueFormat.duration(60))
        assertEquals("59 min", ValueFormat.duration(59 * 60L + 59))
        assertEquals("1 h", ValueFormat.duration(3600))
        assertEquals("3 h", ValueFormat.duration(3 * 3600L + 30))
        assertEquals("1 h 1 min", ValueFormat.duration(3660))
        assertEquals("2 h 5 min", ValueFormat.duration(2 * 3600L + 5 * 60))
    }

    @Test
    fun `a spoken duration uses whole words`() {
        assertEquals("2 hours 5 minutes", ValueFormat.durationSpoken(2 * 3600L + 5 * 60))
        assertEquals("1 hour", ValueFormat.durationSpoken(3600))
        assertEquals("1 minute", ValueFormat.durationSpoken(60))
    }

    @Test
    fun `remaining time is rounded to what the rate can say`() {
        assertEquals("", ValueFormat.remaining(0))
        assertEquals("Under a minute left", ValueFormat.remaining(40))
        assertEquals("About 8 min left", ValueFormat.remaining(8 * 60 + 10))
        assertEquals("About 2 h 15 min left", ValueFormat.remaining(2 * 3600 + 14 * 60))
    }

    @Test
    fun `percent, count and clock time`() {
        assertEquals("45%", ValueFormat.percent(0.456f))
        assertEquals("100%", ValueFormat.percent(3f))
        assertEquals("1,234", ValueFormat.count(1234))
        val utc = TimeZone.UTC
        val t = 1_700_000_000_000L // 22:13 UTC
        assertEquals("22:13", ValueFormat.time(t, is24Hour = true, zone = utc))
        assertEquals("10:13 PM", ValueFormat.time(t, is24Hour = false, zone = utc))
    }

    @Test
    fun `state words come from one table and unknown wire values read Unknown`() {
        assertEquals("Not installed", ValueFormat.State.of("not_installed").label)
        assertEquals("Running", ValueFormat.State.of("RUNNING").label)
        assertEquals(ValueFormat.State.UNKNOWN, ValueFormat.State.of("sparkling"))
        assertEquals(ValueFormat.State.UNKNOWN, ValueFormat.State.of(null))
    }

    @Test
    fun `a size rounds one decimal half up on its exact value`() {
        assertEquals("1.3 GB", ValueFormat.size(1_250_000_000L))
        assertEquals("1.2 GB", ValueFormat.size(1_249_999_999L))
        assertEquals("1.1 GB", ValueFormat.size(1_050_000_000L))
        assertEquals("1.0 GB", ValueFormat.size(1_000_000_000L))
        assertEquals("10.0 GB", ValueFormat.size(9_960_000_000L))
        assertEquals("999 MB", ValueFormat.size(999_999_999L))
        assertEquals("1.0 TB", ValueFormat.size(1_000_000_000_000L))
        assertEquals("1.5 TB", ValueFormat.size(1_450_000_000_000L))
        assertEquals("9223372.0 TB", ValueFormat.size(Long.MAX_VALUE))
    }

    @Test
    fun `a count is grouped by thousands, negatives and the ends included`() {
        assertEquals("0", ValueFormat.count(0))
        assertEquals("999", ValueFormat.count(999))
        assertEquals("1,000", ValueFormat.count(1000))
        assertEquals("12,345", ValueFormat.count(12_345))
        assertEquals("123,456", ValueFormat.count(123_456))
        assertEquals("1,234,567", ValueFormat.count(1_234_567))
        assertEquals("-1", ValueFormat.count(-1))
        assertEquals("-1,000", ValueFormat.count(-1000))
        assertEquals("9,223,372,036,854,775,807", ValueFormat.count(Long.MAX_VALUE))
        assertEquals("-9,223,372,036,854,775,808", ValueFormat.count(Long.MIN_VALUE))
    }

    @Test
    fun `a clock time pads minutes and reads midnight and noon as 12`() {
        val midnight = 1_700_006_400_000L // 2023-11-15 00:00 UTC
        assertEquals("00:00", ValueFormat.time(midnight, is24Hour = true, zone = TimeZone.UTC))
        assertEquals("12:00 AM", ValueFormat.time(midnight, is24Hour = false, zone = TimeZone.UTC))
        assertEquals("12:05 AM", ValueFormat.time(midnight + 5 * 60_000, is24Hour = false, zone = TimeZone.UTC))
        assertEquals("09:07", ValueFormat.time(midnight + (9 * 60 + 7) * 60_000L, is24Hour = true, zone = TimeZone.UTC))
        assertEquals("9:07 AM", ValueFormat.time(midnight + (9 * 60 + 7) * 60_000L, is24Hour = false, zone = TimeZone.UTC))
        assertEquals("12:00 PM", ValueFormat.time(midnight + 12 * 3_600_000L, is24Hour = false, zone = TimeZone.UTC))
        assertEquals("1:30 PM", ValueFormat.time(midnight + (13 * 60 + 30) * 60_000L, is24Hour = false, zone = TimeZone.UTC))
        assertEquals("11:59 PM", ValueFormat.time(midnight + (23 * 60 + 59) * 60_000L, is24Hour = false, zone = TimeZone.UTC))
    }

    @Test
    fun `a clock time is read in the zone it is asked for`() {
        val t = 1_700_000_000_000L // 22:13 UTC
        assertEquals("03:43", ValueFormat.time(t, is24Hour = true, zone = TimeZone.of("+05:30")))
        assertEquals("3:43 AM", ValueFormat.time(t, is24Hour = false, zone = TimeZone.of("+05:30")))
        assertEquals("17:13", ValueFormat.time(t, is24Hour = true, zone = TimeZone.of("-05:00")))
    }
}
