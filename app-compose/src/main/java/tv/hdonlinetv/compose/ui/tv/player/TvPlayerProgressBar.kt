package tv.hdonlinetv.compose.ui.tv.player

import android.view.KeyEvent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SEEK_SHORT_SECONDS = 10f
private const val SEEK_LONG_SECONDS = 30f
private const val LONG_PRESS_THRESHOLD_MS = 400L
private const val LONG_PRESS_REPEAT_MS = 200L

/**
 * Cinema [PlayerProgressSliderPhone] for TV:
 * - Left/Right seek while focused (short / long-press repeat)
 * - [focusProperties] left/right = Cancel so D-pad does not escape the bar horizontally
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvPlayerProgressBar(
    progress: Float,
    durationMs: Long,
    onProgressChange: (Float) -> Unit,
    onUserInteraction: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    upFocus: FocusRequester? = null,
    downFocus: FocusRequester? = null,
) {
    val durationSeconds = (durationMs / 1000f).coerceAtLeast(0f)
    val currentProgress by rememberUpdatedState(progress)
    val scope = rememberCoroutineScope()
    var leftPressed by remember { mutableStateOf(false) }
    var rightPressed by remember { mutableStateOf(false) }
    var leftPressStart by remember { mutableLongStateOf(0L) }
    var rightPressStart by remember { mutableLongStateOf(0L) }
    var leftLongPressActive by remember { mutableStateOf(false) }
    var rightLongPressActive by remember { mutableStateOf(false) }
    var repeatJob by remember { mutableStateOf<Job?>(null) }
    val seekable = durationMs > 0L

    fun seekBy(deltaSeconds: Float) {
        if (durationSeconds <= 0f) return
        val currentSeconds = currentProgress * durationSeconds
        val newSeconds = (currentSeconds + deltaSeconds).coerceIn(0f, durationSeconds)
        onProgressChange(newSeconds / durationSeconds)
    }

    LaunchedEffect(leftLongPressActive, rightLongPressActive) {
        repeatJob?.cancel()
        if (leftLongPressActive || rightLongPressActive) {
            repeatJob = scope.launch {
                while (leftLongPressActive || rightLongPressActive) {
                    if (leftLongPressActive) seekBy(-SEEK_LONG_SECONDS)
                    if (rightLongPressActive) seekBy(SEEK_LONG_SECONDS)
                    delay(LONG_PRESS_REPEAT_MS)
                }
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose { repeatJob?.cancel() }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .focusProperties {
                // Cinema: trap horizontal focus — Left/Right are seek, not navigation.
                left = FocusRequester.Cancel
                right = FocusRequester.Cancel
                upFocus?.let { up = it }
                downFocus?.let { down = it }
            }
            .focusable(enabled = seekable)
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .onKeyEvent {
                if (!seekable) return@onKeyEvent false
                onUserInteraction()
                val now = System.currentTimeMillis()
                when (it.nativeKeyEvent.keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        when (it.nativeKeyEvent.action) {
                            KeyEvent.ACTION_DOWN -> {
                                leftPressed = true
                                leftPressStart = now
                                leftLongPressActive = false
                                scope.launch {
                                    delay(LONG_PRESS_THRESHOLD_MS)
                                    if (leftPressed) leftLongPressActive = true
                                }
                                true
                            }
                            KeyEvent.ACTION_UP -> {
                                if (!leftLongPressActive &&
                                    now - leftPressStart < LONG_PRESS_THRESHOLD_MS
                                ) {
                                    seekBy(-SEEK_SHORT_SECONDS)
                                }
                                leftPressed = false
                                leftLongPressActive = false
                                true
                            }
                            else -> false
                        }
                    }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        when (it.nativeKeyEvent.action) {
                            KeyEvent.ACTION_DOWN -> {
                                rightPressed = true
                                rightPressStart = now
                                rightLongPressActive = false
                                scope.launch {
                                    delay(LONG_PRESS_THRESHOLD_MS)
                                    if (rightPressed) rightLongPressActive = true
                                }
                                true
                            }
                            KeyEvent.ACTION_UP -> {
                                if (!rightLongPressActive &&
                                    now - rightPressStart < LONG_PRESS_THRESHOLD_MS
                                ) {
                                    seekBy(SEEK_SHORT_SECONDS)
                                }
                                rightPressed = false
                                rightLongPressActive = false
                                true
                            }
                            else -> false
                        }
                    }
                    else -> false
                }
            },
    ) {
        // Visual only — focus lives on the Column above (Cinema key handling).
        Slider(
            value = progress.coerceIn(0f, 1f),
            onValueChange = {
                onUserInteraction()
                onProgressChange(it)
            },
            enabled = seekable,
            modifier = Modifier
                .fillMaxWidth()
                .focusProperties { canFocus = false },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                disabledThumbColor = Color.White.copy(alpha = 0.4f),
                disabledActiveTrackColor = Color.White.copy(alpha = 0.25f),
                disabledInactiveTrackColor = Color.White.copy(alpha = 0.15f),
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = formatPlayerTime((progress * durationMs).toLong().coerceAtLeast(0L)),
                color = Color.White,
                fontSize = 12.sp,
            )
            Text(
                text = if (seekable) formatPlayerTime(durationMs) else "LIVE",
                color = Color.White,
                fontSize = 12.sp,
            )
        }
    }
}

fun formatPlayerTime(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) {
        "%d:%02d:%02d".format(h, m, s)
    } else {
        "%02d:%02d".format(m, s)
    }
}
