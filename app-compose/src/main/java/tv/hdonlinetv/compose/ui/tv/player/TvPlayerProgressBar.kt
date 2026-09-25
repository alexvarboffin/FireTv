package tv.hdonlinetv.compose.ui.tv.player

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Cinema [VideoPlayerSeekerTV]: Canvas track (not Material Slider),
 * time labels on sides, thicker when focused, accelerating D-pad seek.
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
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scope = rememberCoroutineScope()
    val seekable = durationMs > 0L
    val clampedProgress = progress.coerceIn(0f, 1f)
    val latestProgress by rememberUpdatedState(clampedProgress)

    val trackColor by rememberUpdatedState(
        if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
    )
    val animatedIndicatorHeight by animateDpAsState(
        targetValue = 4.dp * (if (isFocused) 2.5f else 1f),
        label = "seekHeight",
    )

    var seekingJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(isFocused) {
        if (!isFocused) {
            seekingJob?.cancel()
            seekingJob = null
        }
    }

    val dPadEventsModifier = Modifier.onPreviewKeyEvent { event ->
        if (!isFocused || !seekable) return@onPreviewKeyEvent false
        if (event.key != Key.DirectionLeft && event.key != Key.DirectionRight) {
            return@onPreviewKeyEvent false
        }
        onUserInteraction()
        if (event.type == KeyEventType.KeyDown && seekingJob == null) {
            val direction = if (event.key == Key.DirectionLeft) -1f else 1f
            seekingJob = scope.launch {
                val pressStartTime = System.nanoTime()
                var fastForwardInterval = 10_000L
                val intervalIncreaseRate = 20_000L
                val maxInterval = 60_000L
                while (isActive) {
                    val elapsedMs = (System.nanoTime() - pressStartTime) / 1_000_000
                    if (elapsedMs > 2_000) {
                        fastForwardInterval =
                            (fastForwardInterval + intervalIncreaseRate).coerceAtMost(maxInterval)
                    }
                    val seekAmount =
                        (fastForwardInterval.toFloat() / durationMs.toFloat()) * direction
                    onProgressChange((latestProgress + seekAmount).coerceIn(0f, 1f))
                    delay(100)
                }
            }
        } else if (event.type == KeyEventType.KeyUp) {
            seekingJob?.cancel()
            seekingJob = null
        }
        true
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .focusProperties {
                left = FocusRequester.Cancel
                right = FocusRequester.Cancel
                upFocus?.let { up = it }
                downFocus?.let { down = it }
            }
            .then(if (seekable) dPadEventsModifier else Modifier)
            .focusable(
                enabled = seekable,
                interactionSource = interactionSource,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (seekable) {
                formatPlayerTime((clampedProgress * durationMs).toLong())
            } else {
                "00:00"
            },
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            modifier = Modifier.width(60.dp),
        )
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(animatedIndicatorHeight)
                .padding(horizontal = 4.dp),
        ) {
            val yOffset = size.height / 2
            drawLine(
                color = trackColor.copy(alpha = 0.24f),
                start = Offset(0f, yOffset),
                end = Offset(size.width, yOffset),
                strokeWidth = size.height,
                cap = StrokeCap.Round,
            )
            if (seekable) {
                drawLine(
                    color = trackColor,
                    start = Offset(0f, yOffset),
                    end = Offset(size.width * clampedProgress, yOffset),
                    strokeWidth = size.height,
                    cap = StrokeCap.Round,
                )
            }
        }
        Text(
            text = if (seekable) formatPlayerTime(durationMs) else stringResource(R.string.player_live),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            textAlign = TextAlign.End,
            modifier = Modifier
                .width(60.dp)
                .padding(start = 8.dp),
        )
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
