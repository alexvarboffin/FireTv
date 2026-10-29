package tv.hdonlinetv.compose.ui.tv.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R

/** Purely decorative remote-key legend. Never focusable, so D-pad navigation is unaffected. */
@Composable
fun TvKeyHints(visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.focusProperties { canFocus = false },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            KeyHint(RemoteKey.Left, stringResource(R.string.tv_hint_channels))
            KeyHint(RemoteKey.Menu, stringResource(R.string.tv_hint_settings))
            KeyHint(RemoteKey.Back, stringResource(R.string.tv_hint_hide))
        }
    }
}

private enum class RemoteKey { Left, Menu, Back }

@Composable
private fun KeyHint(key: RemoteKey, label: String) {
    val fg = Color.White.copy(alpha = 0.75f)
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Round remote-button cap, glyph drawn on Canvas.
        Canvas(Modifier.size(20.dp)) {
            val r = size.minDimension / 2f
            drawCircle(Color.White.copy(alpha = 0.14f), r)
            drawCircle(fg.copy(alpha = 0.55f), r - 0.5.dp.toPx(), style = Stroke(1.dp.toPx()))
            val c = center
            val u = r * 0.42f
            when (key) {
                RemoteKey.Left -> drawPath(Path().apply {
                    moveTo(c.x - u * 0.9f, c.y)
                    lineTo(c.x + u * 0.6f, c.y - u)
                    lineTo(c.x + u * 0.6f, c.y + u)
                    close()
                }, fg)
                RemoteKey.Menu -> for (dy in listOf(-u * 0.7f, 0f, u * 0.7f)) drawLine(
                    fg, Offset(c.x - u, c.y + dy), Offset(c.x + u, c.y + dy),
                    strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round,
                )
                RemoteKey.Back -> {
                    val sw = 1.4.dp.toPx()
                    // ↩ : hook from right going up/left, arrow head on the left.
                    drawArc(fg, -90f, 180f, false,
                        topLeft = Offset(c.x - u * 0.2f, c.y - u * 0.8f),
                        size = Size(u * 1.2f, u * 1.6f),
                        style = Stroke(sw, cap = StrokeCap.Round))
                    drawLine(fg, Offset(c.x + u * 0.4f, c.y + u * 0.8f), Offset(c.x - u * 0.6f, c.y + u * 0.8f), sw, StrokeCap.Round)
                    drawLine(fg, Offset(c.x + u * 0.4f, c.y - u * 0.8f), Offset(c.x - u * 0.9f, c.y - u * 0.8f), sw, StrokeCap.Round)
                    drawLine(fg, Offset(c.x - u * 0.9f, c.y - u * 0.8f), Offset(c.x - u * 0.45f, c.y - u * 1.2f), sw, StrokeCap.Round)
                    drawLine(fg, Offset(c.x - u * 0.9f, c.y - u * 0.8f), Offset(c.x - u * 0.45f, c.y - u * 0.4f), sw, StrokeCap.Round)
                }
            }
        }
        Box(Modifier.width(6.dp))
        Text(label, color = fg, fontSize = 11.sp)
    }
}
