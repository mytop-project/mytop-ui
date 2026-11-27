package dev.droidtop.shell.gamepad

import androidx.compose.foundation.basicMarquee
import androidx.compose.ui.Modifier

/**
 * The one scrolling-text rule for a label that must stay on one line (a grid tile, a carousel card, a tab label): while
 * [active] (the focused item) the whole text scrolls, so nothing is unreadable forever (docs/SPEC.md "Text in rows and
 * tiles"). Pair with maxLines = 1. Moved unchanged from droidtop's `MenuRow.kt`.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun Modifier.focusMarquee(active: Boolean): Modifier =
    if (active) basicMarquee(iterations = Int.MAX_VALUE, initialDelayMillis = 600) else this

/**
 * A label as a theme's caps style draws it, or as written when the palette says sentence case. Droidtop's `labelCase`
 * read its own live palette; this reads [ComponentTheme.palette].
 */
fun String.labelCase(): String = if (ComponentTheme.palette.sentenceCase) this else uppercase()
