package dev.droidtop.shell.gamepad

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties

/**
 * The one way a shell element takes a tap (Droidtop/tracker#679): the click, and the click's own focus target made
 * unfocusable ahead of it. A `clickable` is a focus target, so without this the D-pad could park on a row or pill
 * whose screen moves its own cursor, a node with no visible selection where A does nothing (rig, build 546: Down out
 * of the PC grid landed in the hint bar). The shell's cursor is the selection everywhere; a tap is still the press.
 * [onLongClick] makes it a combined click. [silent] draws no press indication (the cursor is the one selection);
 * [interactionSource] then reports the presses to the caller. An element that is itself a focus target keeps its own
 * `focusable()` ahead of this; only the click's extra node goes.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun Modifier.padRow(
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    silent: Boolean = false,
    interactionSource: androidx.compose.foundation.interaction.MutableInteractionSource? = null,
    onClick: () -> Unit,
): Modifier {
    val press = when {
        silent && onLongClick != null -> Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onLongClick = onLongClick,
            onClick = onClick,
        )
        silent -> Modifier.clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onClick)
        onLongClick != null -> Modifier.combinedClickable(enabled = enabled, onLongClick = onLongClick, onClick = onClick)
        else -> Modifier.clickable(enabled = enabled, onClick = onClick)
    }
    return this.focusProperties { canFocus = false }.then(press)
}
