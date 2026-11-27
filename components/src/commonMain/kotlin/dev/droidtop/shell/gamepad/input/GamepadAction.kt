package dev.droidtop.shell.gamepad.input

import androidx.compose.ui.input.key.Key

/**
 * What a pad's button or a keyboard key means to the shell. The names are the face and shoulder roles, not Android's key
 * codes, so the same screens run under Compose's common key events (droidtop's own `GamepadAction.kt` holds the same enum).
 */
enum class GamepadAction {
    A, B, X, Y, START, SELECT,
    UP, DOWN, LEFT, RIGHT,
    L, R, L2, R2, L3, R3,
    BACK,
    ;

    val isDirection: Boolean get() = this == UP || this == DOWN || this == LEFT || this == RIGHT
}

/**
 * The key map over Compose's [Key] (droidtop's `GamepadKeyMap` is the same table over Android's key codes). [faceSwapped]
 * is a fact about the active pad's face buttons, read live (a handheld's own layout toggle changes it at any time); a
 * keyboard's own Enter and Escape are outside it.
 */
object PadKeyMap {
    var faceSwapped: () -> Boolean = { false }

    fun physicalAction(key: Key, shift: Boolean = false): GamepadAction? = when (key) {
        Key.ButtonA, Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> GamepadAction.A
        Key.ButtonB, Key.Escape -> GamepadAction.B
        Key.Back -> GamepadAction.BACK
        Key.ButtonX, Key.Spacebar -> GamepadAction.X
        Key.ButtonY, Key.Backspace -> GamepadAction.Y
        Key.ButtonStart -> GamepadAction.START
        Key.ButtonSelect, Key.Menu -> GamepadAction.SELECT
        Key.DirectionUp -> GamepadAction.UP
        Key.DirectionDown -> GamepadAction.DOWN
        Key.DirectionLeft -> GamepadAction.LEFT
        Key.DirectionRight -> GamepadAction.RIGHT
        Key.ButtonL1, Key.PageUp -> GamepadAction.L
        Key.ButtonR1, Key.PageDown -> GamepadAction.R
        Key.Tab -> if (shift) GamepadAction.L else GamepadAction.R
        Key.ButtonL2 -> GamepadAction.L2
        Key.ButtonR2, Key.F10 -> GamepadAction.R2
        Key.ButtonThumbLeft -> GamepadAction.L3
        Key.ButtonThumbRight -> GamepadAction.R3
        else -> null
    }

    /** Keys a text field types with; a preview handler leaves them alone. */
    fun isTextKey(key: Key): Boolean = key == Key.Spacebar || key == Key.Backspace || key == Key.Tab

    fun actionFor(key: Key, shift: Boolean = false): GamepadAction? {
        val action = physicalAction(key, shift) ?: return null
        val keyboardOnly = key == Key.Enter || key == Key.NumPadEnter || key == Key.Escape
        return if (keyboardOnly) action else applySwap(action)
    }

    private fun applySwap(action: GamepadAction): GamepadAction {
        if (!faceSwapped()) return action
        return when (action) {
            GamepadAction.A -> GamepadAction.B
            GamepadAction.B -> GamepadAction.A
            GamepadAction.X -> GamepadAction.Y
            GamepadAction.Y -> GamepadAction.X
            else -> action
        }
    }
}
