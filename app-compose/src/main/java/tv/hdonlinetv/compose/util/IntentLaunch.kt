package tv.hdonlinetv.compose.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import tv.hdonlinetv.compose.R

/** Starts an intent; on missing handler shows Toast instead of crashing. */
fun Context.startActivitySafe(intent: Intent) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, R.string.no_app_to_handle, Toast.LENGTH_SHORT).show()
    }
}
