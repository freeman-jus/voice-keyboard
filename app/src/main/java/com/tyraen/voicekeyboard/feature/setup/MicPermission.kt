package com.tyraen.voicekeyboard.feature.setup

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tyraen.voicekeyboard.R
import com.tyraen.voicekeyboard.core.config.PreferenceStore

/** The one place that knows how to obtain RECORD_AUDIO, shared by the setup screen and the keyboard. */
object MicPermission {

    const val REQUEST_CODE = 100

    fun isGranted(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Show the system prompt, or, once the system will not show it any more ("Don't ask again"),
     * open the app's settings page with a hint. Callers decide *when* to ask; this decides *how*.
     * Returns true when the prompt was shown, i.e. `onRequestPermissionsResult` will follow.
     */
    suspend fun requestOrOpenSettings(activity: Activity, store: PreferenceStore): Boolean {
        if (isGranted(activity)) return false
        val deniedForever = store.isMicPermissionDeniedForever()
        val canPrompt = !deniedForever ||
            ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)
        if (canPrompt) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_CODE)
            return true
        } else {
            Toast.makeText(activity, R.string.mic_permission_open_settings, Toast.LENGTH_LONG).show()
            try {
                activity.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", activity.packageName, null)
                    )
                )
            } catch (_: Exception) {}
            return false
        }
    }

    /**
     * Remember the outcome of a prompt. Android reports "never asked" and "denied, don't ask again"
     * identically, so the flag is derived from an explicit denial with the rationale gone; a
     * dismissed or cancelled prompt (empty results) changes nothing, and a grant clears it.
     */
    suspend fun recordResult(activity: Activity, store: PreferenceStore, grantResults: IntArray) {
        if (grantResults.isEmpty()) return
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            store.setMicPermissionDeniedForever(false)
        } else if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)) {
            store.setMicPermissionDeniedForever(true)
        }
    }
}
