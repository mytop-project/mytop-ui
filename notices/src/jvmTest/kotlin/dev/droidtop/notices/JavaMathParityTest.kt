package dev.droidtop.notices

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What the common code replaced is still there on the JVM to be compared with: every value below rounds, and a
 * hue converts to degrees, exactly as `java.lang.Math` did.
 */
class JavaMathParityTest {
    private fun same(x: Double) = assertEquals(Math.round(x), ColorMath.roundHalfUp(x), "round($x)")

    @Test
    fun `rounding is Math round on halves, neighbours of halves and random values`() {
        for (n in -2000..2000) {
            val half = n + 0.5
            same(half)
            same(Math.nextUp(half))
            same(Math.nextDown(half))
            same(n.toDouble())
            same(Math.nextUp(n.toDouble()))
            same(Math.nextDown(n.toDouble()))
        }
        val random = Random(20261123)
        repeat(200_000) { same((random.nextDouble() - 0.5) * 600) }
        repeat(50_000) { same(random.nextDouble() * 255) }
        for (x in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 1e30, -1e30, 0.49999999999999994, -0.49999999999999994, 4503599627370497.0, 9.007199254740993E15, Long.MAX_VALUE.toDouble(), Long.MIN_VALUE.toDouble())) same(x)
    }

    @Test
    fun `every colour channel computation rounds as it did`() {
        // ColorMath.mix and over, against the Math.round form they were written in.
        val random = Random(7)
        repeat(20_000) {
            val a = random.nextInt() or (0xFF shl 24)
            val b = random.nextInt() or (0xFF shl 24)
            val t = random.nextDouble()
            fun m(x: Int, y: Int) = Math.round(x + (y - x) * t).toInt()
            val expected = ColorMath.rgb(m(ColorMath.red(a), ColorMath.red(b)), m(ColorMath.green(a), ColorMath.green(b)), m(ColorMath.blue(a), ColorMath.blue(b)))
            assertEquals(expected, ColorMath.mix(a, b, t))
        }
    }

    @Test
    fun `the radians to degrees factor is the JDK's`() {
        for (r in listOf(1.0, 2.0, 3.14159, 0.5, -1.25, 1e-3, 7.0)) {
            assertEquals(Math.toDegrees(r), r * 57.29577951308232, "toDegrees($r)")
        }
    }
}
