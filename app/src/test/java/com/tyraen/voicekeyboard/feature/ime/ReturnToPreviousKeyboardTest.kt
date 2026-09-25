package com.tyraen.voicekeyboard.feature.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReturnToPreviousKeyboardTest {

    private fun decide(
        enabled: Boolean = true,
        inserted: Boolean = true,
        capturing: Boolean = false,
        pendingCount: Int = 0,
        failedCount: Int = 0,
        parkedLoaded: Boolean = true
    ) = ReturnToPreviousKeyboard.shouldReturnToPreviousKeyboard(
        enabled, inserted, capturing, pendingCount, failedCount, parkedLoaded
    )

    @Test fun `returns once the last dictation is typed and nothing else is left`() {
        assertTrue(decide())
    }

    @Test fun `stays when the setting is off`() {
        assertFalse(decide(enabled = false))
    }

    @Test fun `stays when the text went to the clipboard instead of the field`() {
        assertFalse(decide(inserted = false))
    }

    @Test fun `stays while a new recording is in progress`() {
        assertFalse(decide(capturing = true))
    }

    @Test fun `stays until the last queued recording is typed`() {
        assertFalse(decide(pendingCount = 1))
        assertFalse(decide(pendingCount = 3))
    }

    @Test fun `stays while a failed recording waits for resend`() {
        assertFalse(decide(failedCount = 1))
        assertFalse(decide(failedCount = 2))
    }

    @Test fun `stays until parked recordings have loaded`() {
        assertFalse(decide(parkedLoaded = false))
    }

    @Test fun `across the whole truth table only the one clear combination returns`() {
        val flags = listOf(false, true)
        val returning = mutableListOf<List<Any>>()
        for (enabled in flags) {
            for (inserted in flags) {
                for (capturing in flags) {
                    for (pending in 0..2) {
                        for (failed in 0..2) {
                            for (parkedLoaded in flags) {
                                if (decide(enabled, inserted, capturing, pending, failed, parkedLoaded)) {
                                    returning.add(listOf(enabled, inserted, capturing, pending, failed, parkedLoaded))
                                }
                            }
                        }
                    }
                }
            }
        }
        assertEquals(listOf(listOf(true, true, false, 0, 0, true)), returning)
    }
}
