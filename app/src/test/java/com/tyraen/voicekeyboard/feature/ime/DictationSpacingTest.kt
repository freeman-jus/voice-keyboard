package com.tyraen.voicekeyboard.feature.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DictationSpacingTest {

    private fun pad(before: String?, after: String?, trailing: Boolean = true) =
        DictationSpacing.decide(before, after, trailing)

    @Test fun `empty field gets no leading space and the configured trailing one`() {
        assertEquals(DictationSpacing.Padding(leading = false, trailing = true), pad("", ""))
        assertEquals(DictationSpacing.Padding(leading = false, trailing = false), pad("", "", trailing = false))
    }

    @Test fun `after a word a leading space is added`() {
        assertTrue(pad("o", "").leading)
        assertTrue(pad("5", "").leading)
        assertTrue(pad(".", "").leading)
        assertTrue(pad("?", "").leading)
    }

    @Test fun `after whitespace, an opener or a joining symbol no leading space`() {
        assertFalse(pad(" ", "").leading)
        assertFalse(pad("\n", "").leading)
        assertFalse(pad("\u00A0", "").leading)
        assertFalse(pad("(", "").leading)
        assertFalse(pad("«", "").leading)
        assertFalse(pad("\"", "").leading)
        assertFalse(pad("/", "").leading)
        assertFalse(pad("@", "").leading)
        assertFalse(pad("#", "").leading)
        assertFalse(pad("-", "").leading)
        assertFalse(pad("_", "").leading)
    }

    @Test fun `after a closer or percent a leading space is added`() {
        assertTrue(pad(")", "").leading)
        assertTrue(pad("»", "").leading)
        assertTrue(pad("%", "").leading)
        assertTrue(pad("й", "").leading)
    }

    @Test fun `unknown neighbour on the left means no leading space`() {
        assertFalse(pad(null, "").leading)
    }

    @Test fun `trailing space is skipped before existing space or closing punctuation`() {
        assertFalse(pad("", " ").trailing)
        assertFalse(pad("", ".").trailing)
        assertFalse(pad("", ",").trailing)
        assertFalse(pad("", ")").trailing)
        assertFalse(pad("", "»").trailing)
        assertTrue(pad("", "w").trailing)
    }

    @Test fun `unknown neighbour on the right keeps the configured trailing space`() {
        assertTrue(pad("", null).trailing)
        assertFalse(pad("", null, trailing = false).trailing)
    }

    @Test fun `punctuation key eats exactly one plain space`() {
        assertTrue(DictationSpacing.eatsPrecedingSpace(" "))
        assertFalse(DictationSpacing.eatsPrecedingSpace(""))
        assertFalse(DictationSpacing.eatsPrecedingSpace(null))
        assertFalse(DictationSpacing.eatsPrecedingSpace("o"))
        assertFalse(DictationSpacing.eatsPrecedingSpace("\n"))
    }
}
