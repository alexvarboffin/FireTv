package tv.hdonlinetv.compose.ui.mobile.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.ui.components.RemoteImage

/**
 * Touch channel list for the phone player — same browse-scope order as TV curtain,
 * presented as a Material3 bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerChannelSheet(
    visible: Boolean,
    channels: List<ChannelUi>,
    currentChannelId: Long,
    onChannelSelect: (ChannelUi) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    val grouped = remember(channels) {
        channels
            .groupBy { it.category?.takeIf { c -> c.isNotBlank() } ?: "—" }
            .toList()
            .sortedBy { it.first.lowercase() }
    }
    val listState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(visible, currentChannelId, grouped) {
        if (!visible || grouped.isEmpty()) return@LaunchedEffect
        var flatIndex = 0
        for ((_, list) in grouped) {
            flatIndex++ // header
            val i = list.indexOfFirst { it.id == currentChannelId }
            if (i >= 0) {
                listState.scrollToItem((flatIndex + i).coerceAtLeast(0))
                return@LaunchedEffect
            }
            flatIndex += list.size
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorResource(R.color.bgMain),
    ) {
        Text(
            text = stringResource(R.string.menu_home),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            color = colorResource(R.color.black),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
        )
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            grouped.forEach { (category, list) ->
                item(key = "h_$category") {
                    Text(
                        text = category,
                        color = colorResource(R.color.lightGray),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(
                            horizontal = 24.dp,
                            vertical = 10.dp,
                        ),
                    )
                }
                items(list, key = { it.id }) { channel ->
                    ChannelSheetRow(
                        channel = channel,
                        isCurrent = channel.id == currentChannelId,
                        onClick = { onChannelSelect(channel) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelSheetRow(
    channel: ChannelUi,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(
                color = if (isCurrent) {
                    colorResource(R.color.colorAccent).copy(alpha = 0.22f)
                } else {
                    colorResource(R.color.colorPrimaryDark).copy(alpha = 0.35f)
                },
                shape = RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RemoteImage(
            url = channel.cover,
            contentDescription = channel.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(44.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = channel.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colorResource(R.color.black),
                fontSize = 15.sp,
                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
            )
            channel.category?.takeIf { it.isNotBlank() }?.let { cat ->
                Text(
                    text = cat,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colorResource(R.color.lightGray),
                    fontSize = 12.sp,
                )
            }
        }
    }
}
