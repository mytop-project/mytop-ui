package dev.droidtop.shell.gamepad.input

import androidx.compose.runtime.mutableStateOf

/**
 * Whether the shell draws its selection (docs/SPEC.md 6e, Droidtop/tracker
 * #159). The selection is ONE thing whatever moves it -- a tap moves it as
 * much as the D-pad does -- but it is DRAWN only while a pad or keyboard is
 * driving: a person using a finger sees what they touch, and a ring left on
 * some other row reads as "the default" or "the chosen one" (first tester,
 * 2026-09-29: an onboarding option circled while another was ticked; a
 * theme outlined that was not the one picked). The first pad or keyboard
 * press shows it again, where the last tap left it.
 *
 * Starts as "a pad is attached" (a console's own controls count), so a
 * screen that opens with a selection shows it on a console and not on a
 * phone. A snapshot state: everything that draws the ring re-reads it.
 */
object PadModality {
    private val drivingState = mutableStateOf(false)
    private var assumed = false

    /** Whether the selection ring is drawn right now. */
    val showsFocus: Boolean get() = drivingState.value

    fun padDriving() = set(true)

    fun touched() = set(false)

    fun assumeOnce(padAttached: () -> Boolean) {
        if (assumed) return
        assumed = true
        set(padAttached())
    }

    private fun set(value: Boolean) {
        if (drivingState.value != value) drivingState.value = value
    }
}
