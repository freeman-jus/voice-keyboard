package com.tyraen.voicekeyboard.feature.ime

/**
 * Decides the padding around a dictated chunk from the single character on each side of the
 * cursor, so that dictating after "Hello" yields "Hello world" rather than "Helloworld", and
 * dictating in front of an existing space or full stop does not double the space.
 *
 * `null` means the app could not report its text (some fields return null even mid-sentence);
 * an empty string means there is nothing on that side. Unknown is treated as "no neighbour" on
 * the left, because the character before the cursor is usually our own trailing space and adding
 * another would double it, and as "free space" on the right, which keeps the old behaviour.
 */
object DictationSpacing {

    data class Padding(val leading: Boolean, val trailing: Boolean)

    /**
     * Characters a dictated chunk wants a space after: the end of a word, sentence punctuation
     * and unambiguous closers. Anything else (`/` `@` `#` `-` `_` `=`, openers like `(` `«`, and
     * the straight quotes `"` `'`, which are as often opening as closing) hugs the text that
     * follows, so an address, a handle, a hyphenated word or a quotation is not torn apart.
     */
    private const val SPACE_AFTER = ".,;:!?…)]}»”’%"

    /** Closers and sentence punctuation that should hug the text before them. */
    private const val NO_SPACE_BEFORE = ".,;:!?)]}»\"'”’…"

    fun decide(before: CharSequence?, after: CharSequence?, wantTrailing: Boolean): Padding {
        val prev = before?.lastOrNull()
        val next = after?.firstOrNull()
        val leading = prev != null && (prev.isLetterOrDigit() || prev in SPACE_AFTER)
        val trailing = wantTrailing && (next == null || (!isSpace(next) && next !in NO_SPACE_BEFORE))
        return Padding(leading, trailing)
    }

    /** Whether a punctuation key should swallow the character before the cursor (our trailing space). */
    fun eatsPrecedingSpace(before: CharSequence?): Boolean =
        before != null && before.length == 1 && before[0] == ' '

    private fun isSpace(c: Char): Boolean = c.isWhitespace() || c == ' '
}
