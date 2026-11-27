package dev.droidtop.shell.gamepad

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.droidtop.lib.ui.tokens.ThemePalette
import dev.droidtop.lib.ui.tokens.noticeColors
import dev.droidtop.shell.gamepad.input.PadModality
import dev.droidtop.ui.shared.Motion
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The tokens the components draw with: MenuTokens reads the tokens module's palette, and the scales are the design's. */
class TokensTest {
    @Test
    fun menuTokensAreTheTokensModulesPaletteAsComposeColours() {
        ComponentTheme.palette = PaletteColors(ThemePalette.DEFAULT)
        val p = ThemePalette.DEFAULT
        assertEquals(Color(p.accent), MenuTokens.Accent)
        assertEquals(Color(p.ground), MenuTokens.Ground)
        assertEquals(Color(p.card), MenuTokens.Card)
        assertEquals(Color(p.rowFillSelected), MenuTokens.SurfaceSelected)
        assertEquals(Color(p.affirmative), MenuTokens.Affirmative)
    }

    @Test
    fun theDangerColourIsTheNoticePalettesDangerInk() {
        ComponentTheme.palette = PaletteColors(ThemePalette.DEFAULT)
        assertEquals(Color(ThemePalette.DEFAULT.noticeColors().dangerInk), MenuTokens.NoticeDanger)
    }

    @Test
    fun aNewPaletteMovesEveryReader() {
        val other = ThemePalette.DEFAULT.copy(accent = 0xFFFF00FF.toInt())
        ComponentTheme.palette = PaletteColors(other)
        assertEquals(Color(0xFFFF00FF), MenuTokens.Accent)
        ComponentTheme.palette = PaletteColors(ThemePalette.DEFAULT)
        assertEquals(Color(ThemePalette.DEFAULT.accent), MenuTokens.Accent)
    }

    @Test
    fun theSpacingScaleIsFourDpSteps() {
        assertEquals(listOf(2.dp, 4.dp, 8.dp, 12.dp, 16.dp, 24.dp, 48.dp), listOf(Space.Hair, Space.Xs, Space.Sm, Space.Md, Space.Lg, Space.Xl, Space.Xxxl))
        assertEquals(48.dp, Measure.padTarget)
        assertEquals(8.dp, Corners.CrispRadius)
    }

    @Test
    fun onAffirmativeIsBlackOrWhiteWhicheverReads() {
        ComponentTheme.palette = PaletteColors(ThemePalette.DEFAULT.copy(affirmative = 0xFFFFFFFF.toInt()))
        assertEquals(Color.Black, MenuTokens.OnAffirmative)
        ComponentTheme.palette = PaletteColors(ThemePalette.DEFAULT.copy(affirmative = 0xFF000000.toInt()))
        assertEquals(Color.White, MenuTokens.OnAffirmative)
        ComponentTheme.palette = PaletteColors(ThemePalette.DEFAULT)
    }
}

/** The motion curves and durations every component animates with. */
class MotionTest {
    @Test
    fun theGlideEasingStartsAtZeroAndEndsAtOneAndNeverGoesBack() {
        assertEquals(0f, Motion.Glide.transform(0f), 1e-4f)
        assertEquals(1f, Motion.Glide.transform(1f), 1e-4f)
        var last = -1f
        for (i in 0..20) {
            val v = Motion.Glide.transform(i / 20f)
            assertTrue(v >= last, "the glide went back at $i")
            last = v
        }
    }

    @Test
    fun theDurationsAreTheDesignsAndKeepTheirOrder() {
        assertEquals(320, Motion.FocusMs)
        assertEquals(300, Motion.LiftMs)
        assertTrue(Motion.ReleaseMs > Motion.LiftMs, "a release is slower than a lift")
        assertTrue(Motion.PanelOutMs < Motion.PanelInMs, "a panel leaves faster than it arrives")
        assertTrue(Motion.PlayShineMs > Motion.ShineMs)
    }

    @Test
    fun theFocusLookLiftsFromOneToItsScale() {
        assertEquals(1f, FocusLook.scale(0f), 1e-6f)
        assertEquals(FocusLook.LiftScale, FocusLook.scale(1f), 1e-6f)
        assertEquals(FocusLook.LiftScaleWide, FocusLook.scale(1f, wide = true), 1e-6f)
        assertEquals(FocusLook.scale(1f), FocusLook.scale(7f), "progress is held to 0..1")
        assertEquals(0f, FocusLook.riseDp(0f), 1e-6f)
        assertEquals(-FocusLook.RiseDp, FocusLook.riseDp(1f), 1e-6f)
    }
}

/** What a list or strip scrolls by to keep the cursor in view, and what the rail and the focus attach logic say. */
class StateLogicTest {
    @Test
    fun anItemInViewNeedsNoScroll() {
        assertNull(inViewDelta(itemStart = 100, itemSize = 50, roomStart = 0, roomEnd = 400))
    }

    @Test
    fun anItemPastTheEdgesScrollsByAsLittleAsItTakes() {
        assertEquals(-20f, inViewDelta(itemStart = -20, itemSize = 50, roomStart = 0, roomEnd = 400))
        assertEquals(30f, inViewDelta(itemStart = 380, itemSize = 50, roomStart = 0, roomEnd = 400))
    }

    @Test
    fun anItemLongerThanTheRoomShowsItsStart() {
        assertEquals(10f, inViewDelta(itemStart = 10, itemSize = 900, roomStart = 0, roomEnd = 400))
        assertNull(inViewDelta(itemStart = 0, itemSize = 900, roomStart = 0, roomEnd = 400))
    }

    @Test
    fun centringIsTheItemsCentreMinusTheSpansCentre() {
        assertEquals(0f, centreDelta(itemStart = 150f, itemSize = 100f, spanStart = 0f, spanEnd = 400f))
        assertEquals(100f, centreDelta(itemStart = 250f, itemSize = 100f, spanStart = 0f, spanEnd = 400f))
    }

    @Test
    fun aRailTabSpeaksItsCountWhenItHasOne() {
        assertEquals("Apps", RailTab("a", "Apps", QuickGlyph.APPS).spoken)
        assertEquals("Apps, 3 new", RailTab("a", "Apps", QuickGlyph.APPS, badge = 3).spoken)
        assertEquals("Apps", RailTab("a", "Apps", QuickGlyph.APPS, dot = true).spoken)
    }

    @Test
    fun aLabelledTabNeedsItsLabelPlusItsPadding() {
        assertEquals(60.dp, labelExtent(48.dp))
    }

    @Test
    fun theFocusAttachRetriesOncePerFrameAndGivesUp() = runTest {
        var frames = 0
        var tries = 0
        val took = retryEachFrame(attempts = 5, attempt = { ++tries == 3 }, nextFrame = { frames++ })
        assertTrue(took)
        assertEquals(3, tries)
        assertEquals(2, frames)

        tries = 0
        frames = 0
        assertFalse(retryEachFrame(attempts = 4, attempt = { tries++; false }, nextFrame = { frames++ }))
        assertEquals(4, tries)
        assertEquals(3, frames, "no wait after the last try")
    }

    @Test
    fun theSelectionIsDrawnOnlyWhileAPadDrives() {
        PadModality.padDriving()
        assertTrue(PadModality.showsFocus)
        PadModality.touched()
        assertFalse(PadModality.showsFocus)
    }
}
