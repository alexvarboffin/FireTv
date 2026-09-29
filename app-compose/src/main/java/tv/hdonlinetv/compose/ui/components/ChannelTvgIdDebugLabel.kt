package tv.hdonlinetv.compose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.hdonlinetv.compose.BuildConfig
import tv.hdonlinetv.compose.core.domain.model.ChannelUi

/** Debug-only `tvg-id` chip; renders nothing in release. */
@Composable
fun ChannelTvgIdDebugLabel(
    channel: ChannelUi,
    modifier: Modifier = Modifier,
) {
    if (!BuildConfig.DEBUG) return
    val id = channel.tvgId?.trim()?.takeIf { it.isNotEmpty() }
    BasicText(
        text = "tvg-id: ${id ?: "∅"}",
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            color = if (id != null) Color(0xFF7CFC9A) else Color(0xFFFF8A8A),
            fontSize = 10.sp,
        ),
        modifier = modifier
            .background(Color(0xCC000000), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp),
    )
}
