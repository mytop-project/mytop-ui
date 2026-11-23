package dev.droidtop.notices

import kotlin.test.Test
import kotlin.test.assertEquals

/** The rounding that stands in for `java.lang.Math.round` on every target: halves go up, ties are read on the true fraction. */
class RoundingTest {
    @Test
    fun `halves round up, for negatives toward zero`() {
        assertEquals(1L, ColorMath.roundHalfUp(0.5))
        assertEquals(2L, ColorMath.roundHalfUp(1.5))
        assertEquals(3L, ColorMath.roundHalfUp(2.5))
        assertEquals(0L, ColorMath.roundHalfUp(-0.5))
        assertEquals(-1L, ColorMath.roundHalfUp(-1.5))
        assertEquals(-2L, ColorMath.roundHalfUp(-2.5))
        assertEquals(128L, ColorMath.roundHalfUp(127.5))
    }

    @Test
    fun `just below a half rounds down even where adding a half would carry`() {
        assertEquals(0L, ColorMath.roundHalfUp(0.49999999999999994))
        assertEquals(1L, ColorMath.roundHalfUp(0.5000000000000001))
        assertEquals(2L, ColorMath.roundHalfUp(1.5000000000000002))
        assertEquals(1L, ColorMath.roundHalfUp(1.4999999999999998))
    }

    @Test
    fun `non-finite and huge values saturate as Java's do`() {
        assertEquals(0L, ColorMath.roundHalfUp(Double.NaN))
        assertEquals(Long.MAX_VALUE, ColorMath.roundHalfUp(Double.POSITIVE_INFINITY))
        assertEquals(Long.MIN_VALUE, ColorMath.roundHalfUp(Double.NEGATIVE_INFINITY))
        assertEquals(Long.MAX_VALUE, ColorMath.roundHalfUp(1e30))
        assertEquals(Long.MIN_VALUE, ColorMath.roundHalfUp(-1e30))
        assertEquals(4503599627370497L, ColorMath.roundHalfUp(4503599627370497.0))
    }

    @Test
    fun `whole numbers are themselves`() {
        for (n in -300L..300L) assertEquals(n, ColorMath.roundHalfUp(n.toDouble()))
    }
}
