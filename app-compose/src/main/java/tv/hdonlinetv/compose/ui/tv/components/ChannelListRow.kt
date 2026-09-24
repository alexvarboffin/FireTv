package tv.hdonlinetv.compose.ui.tv.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.BuildConfig
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.ui.components.ChannelGeoLockBadge
import tv.hdonlinetv.compose.ui.components.RemoteImage
import tv.hdonlinetv.compose.ui.components.isGeoBlocked

/** Legacy [item_channel_list] parity — name, category, desc, geo, favorite. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ChannelListRow(
    channel: ChannelUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 70.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RemoteImage(
                    url = channel.cover,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, end = 40.dp),
                ) {
                    Text(
                        text = channel.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    channel.category?.takeIf { it.isNotBlank() }?.let { cat ->
                        Text(
                            text = cat,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    channel.desc?.takeIf { it.isNotBlank() }?.let { desc ->
                        Text(
                            text = desc,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.55f),
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    if (BuildConfig.DEBUG) {
                        val debugBits = buildList {
                            if (!channel.extUserAgent.isNullOrBlank() ||
                                !channel.extReferer.isNullOrBlank()
                            ) {
                                add("headers")
                            }
                            if (!channel.ua.isNullOrBlank()) add("ua")
                        }
                        if (debugBits.isNotEmpty()) {
                            Text(
                                text = debugBits.joinToString(" · "),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.error,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (channel.isFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(end = 4.dp),
                    )
                }
                if (channel.isGeoBlocked()) {
                    ChannelGeoLockBadge(channel = channel)
                }
            }
        }
    }
}
