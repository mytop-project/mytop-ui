package dev.droidtop.lib.ui.format

import java.util.Calendar
import java.util.Locale
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What the hand-written digits replaced is still there on the JVM to be compared with: sizes against `%.1f`, counts
 * against `%,d`, clock times against `Calendar`, over values that include every tie.
 */
class JavaFormatParityTest {
    private fun javaSize(bytes: Long): String = when {
        bytes <= 0L -> "0 KB"
        bytes >= 1_000_000_000_000L -> String.format(Locale.ROOT, "%.1f TB", bytes / 1_000_000_000_000L.toDouble())
        bytes >= 1_000_000_000L -> String.format(Locale.ROOT, "%.1f GB", bytes / 1_000_000_000L.toDouble())
        bytes >= 1_000_000L -> "${bytes / 1_000_000L} MB"
        else -> "${(bytes / 1_000L).coerceAtLeast(1)} KB"
    }

    @Test
    fun `sizes read as %f read them`() {
        val random = Random(1123)
        // Every tenth of a gigabyte and terabyte, and the values either side of each tie.
        for (tenth in 0..2000L) {
            for (unit in listOf(1_000_000_000L, 1_000_000_000_000L)) {
                val tie = unit / 10 * tenth + unit / 20
                for (bytes in longArrayOf(tie - 1, tie, tie + 1, unit / 10 * tenth)) {
                    assertEquals(javaSize(bytes), ValueFormat.size(bytes), "size($bytes)")
                }
            }
        }
        repeat(300_000) {
            val bytes = (random.nextDouble() * 5e13).toLong()
            assertEquals(javaSize(bytes), ValueFormat.size(bytes), "size($bytes)")
        }
        repeat(100_000) {
            val bytes = random.nextLong(0, 4_000_000_000_000_000L)
            assertEquals(javaSize(bytes), ValueFormat.size(bytes), "size($bytes)")
        }
    }

    @Test
    fun `counts read as %,d reads them`() {
        val random = Random(99)
        val edges = listOf(0L, 1L, -1L, 999L, 1000L, -999L, -1000L, 999_999L, 1_000_000L, Long.MAX_VALUE, Long.MIN_VALUE)
        for (n in edges) assertEquals(String.format(Locale.US, "%,d", n), ValueFormat.count(n), "count($n)")
        repeat(100_000) {
            val n = random.nextLong() shr random.nextInt(0, 63)
            assertEquals(String.format(Locale.US, "%,d", n), ValueFormat.count(n), "count($n)")
        }
    }

    @Test
    fun `clock times read as Calendar reads them`() {
        val random = Random(5)
        val zones = listOf("UTC", "Asia/Kolkata", "America/New_York", "Pacific/Chatham", "Australia/Lord_Howe", "Asia/Kathmandu")
        repeat(20_000) {
            val ms = random.nextLong(-2_000_000_000_000L, 4_000_000_000_000L)
            val id = zones[random.nextInt(zones.size)]
            val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone(id)).apply { timeInMillis = ms }
            val h = cal.get(Calendar.HOUR_OF_DAY)
            val m = cal.get(Calendar.MINUTE)
            val h12 = if (h % 12 == 0) 12 else h % 12
            val zone = kotlinx.datetime.TimeZone.of(id)
            assertEquals(String.format(Locale.ROOT, "%02d:%02d", h, m), ValueFormat.time(ms, true, zone), "$id $ms")
            assertEquals(String.format(Locale.ROOT, "%d:%02d %s", h12, m, if (h < 12) "AM" else "PM"), ValueFormat.time(ms, false, zone), "$id $ms")
        }
    }
}
