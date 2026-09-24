package tv.hdonlinetv.compose.player

import android.content.Context
import android.os.Build
import androidx.mediarouter.app.MediaRouteButton

/**
 * Cinema livehack: keep track of MediaRouteButton instances attached to the window so Compose
 * UI can fake a click ([MediaRouteButtonManager.current]) without embedding the View in chrome.
 */
class CustomMediaRouteButton(context: Context) : MediaRouteButton(context) {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusable = NOT_FOCUSABLE
        }
        MediaRouteButtonManager.add(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        MediaRouteButtonManager.remove(this)
    }
}

object MediaRouteButtonManager {
    private val buttons = mutableListOf<CustomMediaRouteButton>()

    fun add(button: CustomMediaRouteButton) {
        if (!buttons.contains(button)) buttons.add(button)
    }

    fun remove(button: CustomMediaRouteButton) {
        buttons.remove(button)
    }

    val current: CustomMediaRouteButton?
        get() = buttons.lastOrNull()
}
