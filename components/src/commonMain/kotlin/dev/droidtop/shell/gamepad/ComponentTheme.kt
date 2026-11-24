package dev.droidtop.shell.gamepad

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import dev.droidtop.lib.ui.tokens.ThemePalette
import dev.droidtop.lib.ui.tokens.noticeColors
import dev.droidtop.notices.NoticeColors

/**
 * A palette as the components draw it: mytop-ui's [ThemePalette] (ARGB ints) as Compose colours, with the notice colours
 * derived from it. This is the slice of droidtop's `GamingPalette` that [MenuTokens] reads; droidtop keeps its own
 * (typefaces, texture, ES-DE) and hands [ComponentTheme] the palette it resolved.
 */
class PaletteColors(val colors: ThemePalette, val highContrast: Boolean = false) {
    val ground = Color(colors.ground)
    val overlaySurface = Color(colors.overlaySurface)
    val card = Color(colors.card)
    val cardFocused = Color(colors.cardFocused)
    val cardInset = Color(colors.cardInset)
    val hintBar = Color(colors.hintBar)
    val onSurface = Color(colors.onSurface)
    val onSurfaceMuted = Color(colors.onSurfaceMuted)
    val value = Color(colors.value)
    val placeholder = Color(colors.placeholder)
    val sectionLabel = Color(colors.sectionLabel)
    val onSurfaceDisabled = Color(colors.onSurfaceDisabled)
    val accent = Color(colors.accent)
    val onAccent = Color(colors.onAccent)
    val affirmative = Color(colors.affirmative)
    val favourite = Color(colors.favourite)
    val rowFill = Color(colors.rowFill)
    val rowFillSelected = Color(colors.rowFillSelected)
    val cardOutline = Color(colors.cardOutline)
    val hintPillOutline = Color(colors.hintPillOutline)
    val selected = Color(colors.selected)
    val onSelected = Color(colors.onSelected)
    val scrim = Color(colors.scrim)
    val launch = Color(colors.launch)
    val launchFocused = Color(colors.launchFocused)
    val launchDisabled = Color(colors.launchDisabled)
    val onLaunchMuted = Color(colors.onLaunchMuted)

    /** The notice colours of this palette (mytop-ui `noticeColors`). */
    val notice: NoticeColors by lazy { colors.noticeColors(highContrast) }
}

/**
 * Where [MenuTokens] reads its colours: the live palette, snapshot state, so a change redraws every reader. A program
 * sets [palette] from the palette its theme engine resolved (droidtop's ES-DE mapping, Pythia's themes); the default is
 * mytop-ui's own.
 */
object ComponentTheme {
    var palette: PaletteColors by mutableStateOf(PaletteColors(ThemePalette.DEFAULT))
}
