package dev.droidtop.shell.gamepad

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.droidtop.lib.ui.tokens.FocusRingInks

/**
 * The ring on one control, drawn still here: the same two opaque edges as droidtop's (the accent outside, black or white
 * inside, [FocusRingInks]), [outset] outside the control's edge. This is the still-ring branch of droidtop's
 * `Modifier.focusRing` (`FocusGlide.kt`), which also slides one ring between controls when a window hosts it, keyed on
 * an Android `View`; that part is not moved yet (CHANGELOG 0.4.0).
 */
fun Modifier.focusRing(shown: Boolean, shape: Shape, outset: Dp = 0.dp): Modifier {
    if (!shown) return this
    return drawWithContent {
        drawContent()
        val accent = MenuTokens.Accent
        val inner = Color(FocusRingInks.inner(accent.toArgb()))
        val outerDp = FocusRingInks.outerDp(ComponentTheme.palette.highContrast)
        val o = outset.toPx()
        // The two edges, outer first: each stroke is drawn on the middle of its own band.
        listOf(
            Triple(accent, outerDp.dp.toPx(), 0f),
            Triple(inner, FocusRingInks.INNER_DP.dp.toPx(), outerDp.dp.toPx()),
        ).forEach { (ink, w, inset) ->
            val band = Size(size.width + 2 * o - 2 * inset - w, size.height + 2 * o - 2 * inset - w)
            if (band.width <= 0f || band.height <= 0f) return@forEach
            translate(left = inset + w / 2f - o, top = inset + w / 2f - o) {
                drawOutline(shape.createOutline(band, layoutDirection, this), ink, style = Stroke(w))
            }
        }
    }
}
