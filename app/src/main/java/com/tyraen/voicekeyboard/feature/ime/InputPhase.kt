package com.tyraen.voicekeyboard.feature.ime

import androidx.annotation.StringRes

sealed class InputPhase {
    object Ready : InputPhase()
    data class Capturing(val startTimeMs: Long = System.currentTimeMillis()) : InputPhase()

    /** [reasonRes] is a string resource so the status line reads in the interface language. */
    data class Failed(@StringRes val reasonRes: Int) : InputPhase()
}
