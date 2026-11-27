package dev.droidtop.shell.gamepad

import dev.droidtop.shell.gamepad.input.GamepadAction
import dev.droidtop.shell.gamepad.input.PadCadence
import dev.droidtop.shell.gamepad.input.PadEdges
import dev.droidtop.shell.gamepad.input.PadPress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PackedPadKeysTest {
    @Test
    fun overlappingKeysWithTheSameLowBitsKeepTheirOwnReleaseAndRepeat() {
        val edges = PadEdges(PadCadence.CHROME)
        // Android Compose: Key(nativeKeyCode) stores packInts(nativeKeyCode, 0).
        val up = 19L shl 32
        val right = 22L shl 32
        val presses = mutableListOf<PadPress>()
        val take: (PadPress) -> Boolean = { presses.add(it); true }
        assertTrue(edges.down(up, GamepadAction.UP, 0, 0, 0, take))
        assertTrue(edges.down(right, GamepadAction.RIGHT, 0, 10, 10, take))
        assertTrue(edges.up(up))
        assertTrue(edges.down(right, GamepadAction.RIGHT, 1, 10, 1_000, take))
        assertTrue(edges.up(right))
        assertFalse(edges.up(right))
        assertEquals(listOf(GamepadAction.UP, GamepadAction.RIGHT, GamepadAction.RIGHT), presses.map { it.action })
        assertEquals(listOf(false, false, true), presses.map { it.repeat })
    }

    @Test
    fun lowAndHighHalvesBothParticipateInKeyIdentity() {
        val edges = PadEdges(PadCadence.CHROME)
        val first = 1L shl 32
        val second = first or 1L
        assertTrue(edges.down(first, GamepadAction.A, 0, 0, 0) { true })
        assertTrue(edges.down(second, GamepadAction.B, 0, 0, 0) { true })
        assertTrue(edges.up(first))
        assertTrue(edges.up(second))
    }
}
