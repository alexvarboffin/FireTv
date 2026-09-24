package tv.hdonlinetv.compose.ui.mobile.components

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.hdonlinetv.compose.BuildConfig
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.ui.components.ChannelGeoLockBadge
import tv.hdonlinetv.compose.ui.components.RemoteImage
import tv.hdonlinetv.compose.ui.components.isGeoBlocked

/** Legacy [item_channel_list] parity — name, category, desc, geo, favorite. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelListRow(
    channel: ChannelUi,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.cardBack)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        // Legacy RelativeLayout: thumb | texts, geolock top-end of text block.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
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
                        .padding(5.dp)
                        .size(70.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp, end = 36.dp, top = 4.dp, bottom = 4.dp),
                ) {
                    Text(
                        text = channel.name,
                        fontSize = 18.sp,
                        color = colorResource(R.color.black),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 5.dp, end = 5.dp),
                    )
                    channel.category?.takeIf { it.isNotBlank() }?.let { cat ->
                        Text(
                            text = cat,
                            fontSize = 14.sp,
                            color = colorResource(R.color.lightGray),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 10.dp, top = 2.dp),
                        )
                    }
                    channel.desc?.takeIf { it.isNotBlank() }?.let { desc ->
                        Text(
                            text = desc,
                            fontSize = 12.sp,
                            color = colorResource(R.color.MainSecText),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 10.dp, top = 4.dp, end = 4.dp),
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
                                fontSize = 11.sp,
                                color = colorResource(R.color.colorAccent),
                                modifier = Modifier.padding(start = 10.dp, top = 2.dp),
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (channel.isFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = colorResource(R.color.colorAccent),
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
