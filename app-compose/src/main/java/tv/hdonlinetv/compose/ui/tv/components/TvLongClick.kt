package tv.hdonlinetv.compose.ui.tv.components

import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/**
 * D-pad OK click / long-click for TV items (reference: Cinema MovieCardTvSimple).
 * Swallows DPAD_CENTER/ENTER/NUMPAD_ENTER; decides on KeyUp by hold duration
 * (KeyEvent.downTime -> eventTime), so exactly one of onClick/onLongClick fires.
 * Ups without our own Down (e.g. after a dialog closed) are swallowed too.
 * Pass the same lambdas to Card(onClick/onLongClick) for pointer input.
 */
private class TvPressState {
    var armed = false
    var long = false
}

fun Modifier.tvDpadClick(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    longPressMs: Long = 450L,
): Modifier = composed {
    val st = remember { TvPressState() }
    onPreviewKeyEvent { event ->
        when (event.key) {
            Key.Enter, Key.DirectionCenter, Key.NumPadEnter -> {
                val native = event.nativeKeyEvent
                when (event.type) {
                    KeyEventType.KeyDown -> {
                        if (native.repeatCount == 0) {
                            st.armed = true
                            st.long = false
                        } else if (st.armed) {
                            // Held: auto-repeat / FLAG_LONG_PRESS from the system.
                            st.long = true
                        }
                        if (native.isLongPress) st.long = true
                        // android.util.Log.d("TvDpadClick", "down rep=${native.repeatCount} lp=${native.isLongPress}")
                        true
                    }
                    KeyEventType.KeyUp -> {
                        val wasArmed = st.armed
                        st.armed = false
                        if (wasArmed && !native.isCanceled) {
                            val held = native.eventTime - native.downTime
                            val isLong = st.long || held >= longPressMs
                            // android.util.Log.d("TvDpadClick", "up held=$held long=$isLong")
                            if (onLongClick != null && isLong) onLongClick() else onClick()
                        }
                        true
                    }
                    else -> false
                }
            }
            else -> false
        }
    }
}
