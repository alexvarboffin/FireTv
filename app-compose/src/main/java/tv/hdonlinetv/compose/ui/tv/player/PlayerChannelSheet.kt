package tv.hdonlinetv.compose.ui.tv.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.ui.components.RemoteImage

/**
 * Cinema-style in-player channel curtain (left): categories as section headers,
 * channels as focusable rows — zap without leaving the player route.
 *
 * Click selects a channel but keeps the sheet open for further browsing.
 * Left / Right / Home dismiss the sheet.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerChannelSheet(
    visible: Boolean,
    channels: List<ChannelUi>,
    currentChannelId: Long,
    onChannelSelect: (ChannelUi) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val grouped = remember(channels) {
        channels
            .groupBy { it.category?.takeIf { c -> c.isNotBlank() } ?: "—" }
            .toList()
            .sortedBy { it.first.lowercase() }
    }
    val listState = rememberLazyListState()
    val firstFocus = remember { FocusRequester() }

    // Scroll/focus to current only when the sheet opens — not on every zap
    // (otherwise focus jumps while the user is still browsing the list).
    LaunchedEffect(visible) {
        if (!visible || grouped.isEmpty()) return@LaunchedEffect
        var flatIndex = 0
        var target = 0
        var found = false
        for ((_, list) in grouped) {
            flatIndex++ // header
            val i = list.indexOfFirst { it.id == currentChannelId }
            if (i >= 0) {
                target = flatIndex + i
                found = true
                break
            }
            flatIndex += list.size
        }
        if (found) {
            listState.scrollToItem(target.coerceAtLeast(0))
        }
        firstFocus.requestFocus()
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally { -it } + fadeIn(),
        exit = slideOutHorizontally { -it } + fadeOut(),
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (!visible || event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.DirectionLeft, Key.DirectionRight,
                    Key.Home, Key.MoveHome, Key.Escape,
                    -> {
                        onDismiss()
                        true
                    }
                    else -> false
                }
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.22f)),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .width(380.dp)
                    .background(
                        Color.Black.copy(alpha = 0.45f),
                        RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                    )
                    .padding(vertical = 12.dp)
                    .focusGroup(),
            ) {
                Text(
                    text = stringResource(R.string.menu_home),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                val focusChannelId = when {
                    channels.any { it.id == currentChannelId } -> currentChannelId
                    else -> channels.firstOrNull()?.id
                }
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    grouped.forEach { (category, list) ->
                        item(key = "h_$category") {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.65f),
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 10.dp,
                                ),
                            )
                        }
                        itemsIndexed(list, key = { _, ch -> ch.id }) { _, channel ->
                            val isCurrent = channel.id == currentChannelId
                            ChannelSheetRow(
                                channel = channel,
                                isCurrent = isCurrent,
                                onClick = { onChannelSelect(channel) },
                                onDismissSheet = onDismiss,
                                modifier = if (channel.id == focusChannelId) {
                                    Modifier.focusRequester(firstFocus)
                                } else {
                                    Modifier
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ChannelSheetRow(
    channel: ChannelUi,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onDismissSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.DirectionLeft, Key.DirectionRight,
                    Key.Home, Key.MoveHome, Key.Escape,
                    -> {
                        onDismissSheet()
                        true
                    }
                    else -> false
                }
            },
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isCurrent) {
                colors.primary.copy(alpha = 0.28f)
            } else {
                Color.White.copy(alpha = 0.10f)
            },
            focusedContainerColor = colors.primary.copy(alpha = 0.72f),
            pressedContainerColor = colors.primary.copy(alpha = 0.85f),
        ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RemoteImage(
                url = channel.cover,
                contentDescription = channel.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(48.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White,
                )
                channel.category?.takeIf { it.isNotBlank() }?.let { cat ->
                    Text(
                        text = cat,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}
