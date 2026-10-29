package tv.hdonlinetv.compose.ui.tv.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.delay

enum class TvSidePanelSide { Start, End }

/**
 * Shared base for in-player TV side menus (channel/EPG curtain on the left,
 * "Audio & subtitles" on the right): slide-in/out from [side], dimmed scrim over the video,
 * Home / Escape dismiss. Panel content decides its own D-pad / focus logic.
 */
@Composable
fun TvSidePanel(
    visible: Boolean,
    side: TvSidePanelSide,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    scrimAlpha: Float = 0.22f,
    content: @Composable RowScope.() -> Unit,
) {
    val sign = if (side == TvSidePanelSide.Start) -1 else 1
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally { sign * it } + fadeIn(),
        exit = slideOutHorizontally { sign * it } + fadeOut(),
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (!visible || event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.SystemHome, Key.MoveHome, Key.Escape -> {
                        onDismiss()
                        true
                    }
                    else -> false
                }
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha)),
            horizontalArrangement = if (side == TvSidePanelSide.Start) Arrangement.Start else Arrangement.End,
            content = content,
        )
    }
}

/**
 * Focus-restore helper for side panels: remember who opened the panel, and return focus
 * there after it closes (small delay lets the panel leave composition first).
 */
class TvFocusReturn {
    var opener: FocusRequester? by mutableStateOf(null)
    internal var pending: FocusRequester? by mutableStateOf(null)

    fun open(from: FocusRequester?) {
        opener = from
    }

    fun close() {
        pending = opener
        opener = null
    }
}

@Composable
fun rememberTvFocusReturn(onRestored: () -> Unit = {}): TvFocusReturn {
    val state = remember { TvFocusReturn() }
    LaunchedEffect(state.pending) {
        val target = state.pending ?: return@LaunchedEffect
        delay(250) // after slide-out, and after the chrome's own focus effect
        runCatching { target.requestFocus() }
        state.pending = null
        onRestored()
    }
    return state
}
