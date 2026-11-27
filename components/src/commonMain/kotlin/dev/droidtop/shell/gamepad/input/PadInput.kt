package dev.droidtop.shell.gamepad.input

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import kotlin.time.TimeSource

/** One press of a pad button the shell acts on: which [action], and whether it is a held button repeating. */
class PadPress(val action: GamepadAction, val repeat: Boolean)

/**
 * How fast a held direction repeats: tiers of (how long the tier lasts, the delay between steps in it); the last tier
 * (length 0) lasts as long as the hold does (docs/SPEC.md 6e). Moved from droidtop's `PadInput.kt`.
 */
class PadCadence(private val tiers: List<Pair<Long, Long>>) {
    fun delayAt(heldMs: Long): Long {
        var start = 0L
        for ((length, delay) in tiers) {
            if (length == 0L || heldMs < start + length) return delay
            start += length
        }
        return tiers.last().second
    }

    companion object {
        val CHROME = PadCadence(listOf(500L to 500L, 1100L to 180L, 0L to 80L))
        val THEMED_LIST = PadCadence(listOf(500L to 500L, 1200L to 114L, 0L to 16L))
        val THEMED_CAROUSEL = PadCadence(listOf(500L to 500L, 0L to 200L))
        val THEMED_CAROUSEL_FAST = PadCadence(listOf(500L to 500L, 1100L to 180L, 0L to 80L))
    }
}

/**
 * The edges of a pad: a key's first DOWN is a press; while it is held only directions repeat, at [cadence]; its UP is
 * consumed exactly when this owner took the press (so a release that went to another window is not ours).
 */
class PadEdges(private val cadence: PadCadence) {
    private val lastStep = HashMap<Long, Long>()
    private val downAt = HashMap<Long, Long>()

    fun down(
        keyCode: Int,
        action: GamepadAction,
        repeatCount: Int,
        downTime: Long,
        eventTime: Long,
        handle: (PadPress) -> Boolean,
    ): Boolean = down(keyCode.toLong(), action, repeatCount, downTime, eventTime, handle)

    /** Compose keys may pack a platform code in either half of their Long. Keep the complete identity. */
    internal fun down(
        keyCode: Long,
        action: GamepadAction,
        repeatCount: Int,
        downTime: Long,
        eventTime: Long,
        handle: (PadPress) -> Boolean,
    ): Boolean {
        if (repeatCount == 0) {
            // A fresh press: whatever this key's last press left behind (a release that went to another window) is over.
            lastStep.remove(keyCode)
            if (!handle(PadPress(action, repeat = false))) return false
            lastStep[keyCode] = eventTime
            return true
        }
        val last = lastStep[keyCode] ?: return false
        if (!action.isDirection) return true
        if (eventTime - last < cadence.delayAt(eventTime - downTime)) return true
        lastStep[keyCode] = eventTime
        handle(PadPress(action, repeat = true))
        return true
    }

    /** An UP edge: consumed exactly when this owner took the press. */
    fun up(keyCode: Int): Boolean = up(keyCode.toLong())

    internal fun up(keyCode: Long): Boolean = lastStep.remove(keyCode) != null

    /**
     * One raw key event. The system back key is never taken: it belongs to the back dispatcher. Nor is a FALLBACK key
     * ([PadKeyHooks.isFallback]): that is Android re-sending a pad button nobody took as some other key (`Generic.kcm`:
     * Y becomes SPACE, X becomes DEL, A becomes DPAD_CENTER), and acting on it would make an unused Y mean X.
     *
     * Compose's common key event has no repeat count or down time, so a DOWN of a key already down is a repeat, and the
     * down time is when the first one arrived ([PadKeyHooks.repeatCount] gives Android's own count where there is one).
     */
    fun dispatch(event: KeyEvent, preview: Boolean, handle: (PadPress) -> Boolean): Boolean {
        val key = event.key
        if (key == Key.Back) return false
        if (PadKeyHooks.isFallback(event)) return false
        if (preview && PadKeyMap.isTextKey(key)) return false
        val action = PadKeyMap.actionFor(key, event.isShiftPressed) ?: return false
        // Android's Key packs nativeKeyCode into the high 32 bits; toInt() made every one of them zero.
        val id = key.keyCode
        return when (event.type) {
            KeyEventType.KeyDown -> {
                val now = nowMs()
                val first = downAt[id]
                if (first == null) downAt[id] = now
                val repeats = PadKeyHooks.repeatCount(event) ?: if (first == null) 0 else 1
                down(id, action, repeats, downTime = first ?: now, eventTime = now, handle = handle)
            }
            KeyEventType.KeyUp -> {
                downAt.remove(id)
                up(id)
            }
            else -> false
        }
    }
}

/**
 * What only the platform knows about a key event. The defaults are the common ones; Android sets them from its own
 * event (`flags and FLAG_FALLBACK`, `repeatCount`).
 */
object PadKeyHooks {
    var isFallback: (KeyEvent) -> Boolean = { false }
    var repeatCount: (KeyEvent) -> Int? = { null }
}

private val clock = TimeSource.Monotonic.markNow()

private fun nowMs(): Long = clock.elapsedNow().inWholeMilliseconds

/**
 * How a screen takes the pad: [handler] gets every [PadPress] that reaches this node and answers whether it used it
 * (docs/SPEC.md 6e). The edges, the repeats and who owns a release are [PadEdges]'s, not the screen's. [preview] for the
 * rare node that must answer before the focused child does; a preview node leaves the keys a text field types to the field.
 */
fun Modifier.onPad(
    cadence: PadCadence = PadCadence.CHROME,
    preview: Boolean = false,
    handler: (PadPress) -> Boolean,
): Modifier = composed {
    val edges = remember(cadence) { PadEdges(cadence) }
    val current by rememberUpdatedState(handler)
    if (preview) {
        onPreviewKeyEvent { event -> edges.dispatch(event, preview = true) { current(it) } }
    } else {
        onKeyEvent { event -> edges.dispatch(event, preview = false) { current(it) } }
    }
}

/**
 * One step of a menu's cursor. Real ES-DE's menus never loop (`ComponentList`, es-core/src/components/ComponentList.cpp:17,
 * `LIST_NEVER_LOOP`): the cursor stops at both ends, held or not, so a held direction can never spin through a menu and
 * land somewhere unexpected.
 */
fun menuStep(index: Int, count: Int, delta: Int): Int =
    if (count <= 0) 0 else (index + delta).coerceIn(0, count - 1)
