package tv.hdonlinetv.compose.ui.mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.PlaylistType
import tv.hdonlinetv.compose.core.domain.model.PlaylistUi
import tv.hdonlinetv.compose.util.PlaylistMetaFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistCard(
    playlist: PlaylistUi,
    onClick: () -> Unit,
    onRefresh: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconRes = when (playlist.type) {
        PlaylistType.M3U_CLOUD -> R.drawable.ic_cloud
        PlaylistType.M3U_LOCAL -> R.drawable.ic_local
        PlaylistType.M3U_BUFFER -> R.drawable.ic_buffer
        PlaylistType.XTREAM_URL -> R.drawable.ic_xtream
        else -> R.drawable.ic_playlist
    }
    val channelsFmt = stringResource(R.string.channels_format)
    val updatedFmt = stringResource(R.string.playlist_updated_short)
    val meta = remember(playlist, channelsFmt, updatedFmt) {
        PlaylistMetaFormat.buildMeta(
            playlist = playlist,
            channelsLabel = { count ->
                String.format(Locale.getDefault(), channelsFmt, count)
            },
            updatedLabel = { date ->
                String.format(Locale.getDefault(), updatedFmt, date)
            },
        )
    }
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.cardBack)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = colorResource(R.color.black),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 4.dp),
            ) {
                Text(
                    text = playlist.title,
                    fontSize = 16.sp,
                    color = colorResource(R.color.MainText),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (playlist.fileName.isNotBlank()) {
                    Text(
                        text = playlist.fileName,
                        fontSize = 12.sp,
                        color = colorResource(R.color.MainSecText),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (meta.isNotEmpty()) {
                    Text(
                        text = meta,
                        fontSize = 11.sp,
                        color = colorResource(R.color.MainSecText),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onRefresh, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_update),
                        contentDescription = null,
                        tint = colorResource(R.color.colorAccent),
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = null,
                        tint = colorResource(R.color.colorAccent),
                    )
                }
            }
        }
    }
}

@Composable
fun LegacySettingsRow(
    iconRes: Int,
    title: String,
    subtitle: String? = null,
    trailing: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(R.color.bgMain))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(35.dp)
                .padding(end = 10.dp),
            tint = colorResource(R.color.black),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = colorResource(R.color.MainText),
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = colorResource(R.color.MainSecText),
                )
            }
        }
        if (!trailing.isNullOrBlank()) {
            Text(
                text = trailing,
                fontSize = 14.sp,
                color = colorResource(R.color.MainText),
            )
        }
    }
}
