package com.tyraen.voicekeyboard.feature.ime

/**
 * Decides when the keyboard hands the screen back to the one that opened it, typically another
 * keyboard's mic key. Only once a dictation has actually been typed into the field and nothing is
 * left for this panel to show: no recording in progress, nothing still queued, and no failed
 * recording waiting for resend, so the queue badge and the resend button are never hidden.
 */
object ReturnToPreviousKeyboard {

    /**
     * [inserted] is false when the text went to the clipboard instead of the field.
     * [pendingCount] must not count the recording that was just delivered.
     */
    fun shouldReturnToPreviousKeyboard(
        enabled: Boolean,
        inserted: Boolean,
        capturing: Boolean,
        pendingCount: Int,
        failedCount: Int
    ): Boolean = enabled && inserted && !capturing && pendingCount == 0 && failedCount == 0
}
