package dev.droidtop.shell.gamepad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The one button glyph the shell draws (docs/SPEC.md 7k): a keycap, round
 * for a single letter ("A") and a pill for a word ("Start", "L1"), filled
 * with the muted ink and lettered in the ground colour, so it reads as a
 * quieter thing than the label beside it, as Steam's footer glyphs do. The
 * hint bar's glyphs, the L1/R1 beside a strip and every other place that
 * names a button use this and nothing else. Not focusable, no tap target of
 * its own: the row it sits in is the control. [dimmed] is a glyph whose
 * press has nowhere to go (a strip at its end), drawn at half strength.
 * The keycap is ported in spirit from DroidDeck's non-focusable bumper caps
 * (ui/SettingsWidgets.kt at 9310d19).
 */
@Composable
fun Keycap(label: String, modifier: Modifier = Modifier, height: Dp = MenuTokens.KeycapHeight, dimmed: Boolean = false) {
    val style = MaterialTheme.typography.labelMedium.copy(
        fontSize = MenuTokens.HintGlyphTextSize,
        fontWeight = FontWeight.Bold,
    )
    Text(
        label,
        color = MenuTokens.Ground,
        style = style,
        maxLines = 1,
        textAlign = TextAlign.Center,
        modifier = modifier
            .graphicsLayer { alpha = if (dimmed) 0.5f else 1f }
            .background(MenuTokens.OnSurfaceMuted, Corners.Pill)
            .widthIn(min = height)
            .padding(horizontal = MenuTokens.HintGlyphPaddingHorizontal)
            .opticallyCentred(height, style.fontSize),
    )
}

/**
 * A tiny shoulder-button glyph ("L1"/"R1") beside a tab or section row it
 * switches, replacing a pill in the hint bar for the one thing every
 * screen with tabs does the same way (owner, 2026-09-25: "Can remove the
 * next/previous section pills"). It is a [Keycap], not a hint pill -- no
 * label and no tap target of its own -- because the row it sits beside
 * already IS the switch's visible state, and L1/R1 keep working exactly
 * as before; this only says which buttons step it. One composable, used
 * beside every tab row L1/R1 drives (the shell's top-level section tabs,
 * the Quick Menu's own Notifications/System tabs), so all of them read
 * the same way and none of them repeats the label text or the styling.
 */
@Composable
fun ShoulderGlyph(label: String, modifier: Modifier = Modifier, badge: Boolean = false, dimmed: Boolean = false) {
    // The strip's edition sits in a box as tall as the selected tab's pill,
    // centred the same way, so the two read on one line (tester, 2026-09-29,
    // Droidtop/tracker#157: "L1/R1 are smaller than the tab labels and look
    // misaligned with the pill"). One keycap style either way.
    if (badge) {
        Box(modifier.heightIn(min = MenuTokens.TabPillHeight), contentAlignment = Alignment.Center) {
            Keycap(label, dimmed = dimmed)
        }
    } else {
        Keycap(label, modifier, height = MenuTokens.KeycapHeightSmall, dimmed = dimmed)
    }
}

/** How far a fading edge reaches (36dp), shared by the row and column fades. */
internal const val MoreFadeDp = 36

/**
 * The ends of a scrolling row fading out on the side it can still scroll to, so a tab or chip cut at the edge reads
 * as "more this way" (Droidtop/tracker#661): a row of tabs never ends mid-letter with nothing to say so. Drawn in the
 * draw phase; the row is composited offscreen only while a fade shows.
 */
fun Modifier.fadeWhileMoreSideways(state: androidx.compose.foundation.lazy.LazyListState): Modifier =
    this
        .graphicsLayer {
            compositingStrategy = if (state.canScrollForward || state.canScrollBackward) {
                androidx.compose.ui.graphics.CompositingStrategy.Offscreen
            } else {
                androidx.compose.ui.graphics.CompositingStrategy.Auto
            }
        }
        .drawWithContent {
            drawContent()
            val fade = MoreFadeDp.dp.toPx().coerceAtMost(size.width / 4f)
            if (state.canScrollForward) {
                drawRect(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(Color.Black, Color.Transparent),
                        startX = size.width - fade,
                        endX = size.width,
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(size.width - fade, 0f),
                    size = androidx.compose.ui.geometry.Size(fade, size.height),
                    blendMode = androidx.compose.ui.graphics.BlendMode.DstIn,
                )
            }
            if (state.canScrollBackward) {
                drawRect(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color.Transparent, Color.Black), startX = 0f, endX = fade),
                    size = androidx.compose.ui.geometry.Size(fade, size.height),
                    blendMode = androidx.compose.ui.graphics.BlendMode.DstIn,
                )
            }
        }
