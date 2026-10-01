package tv.hdonlinetv.compose.ui.tv.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import kotlinx.coroutines.launch
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.tv.LocalTvDrawerFocusRequester
import tv.hdonlinetv.compose.ui.components.TvChannelGridMinCell

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ChannelGridBody(
    channels: List<ChannelUi>,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    /** Scrolls with the items (first full-width item). */
    header: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues? = null,
    onChannelClick: (ChannelUi) -> Unit,
) {
    val drawerFocus = LocalTvDrawerFocusRequester.current
    val listRestorer = rememberTvLazyFocusRestorer()
    val listMode = LocalSettingsRepository.current.getSettings().gridColumns <= 1
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val itemCount = if (isLoading) 0 else channels.size
    val offset = if (header != null) 1 else 0
    val scope = rememberCoroutineScope()
    if (listMode) {
        listRestorer.RestoreFocusOnReturn(listState, itemCount, offset)
    } else {
        listRestorer.RestoreFocusOnReturn(gridState, itemCount, offset)
    }

    when {
        isLoading || channels.isEmpty() -> Column(modifier.fillMaxSize()) {
            if (header != null) {
                Box(Modifier.padding(start = 48.dp, end = 48.dp, top = HeaderTopPadding)) { header() }
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(if (isLoading) R.string.loading else R.string.no_item))
            }
        }
        listMode -> LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize().tvLazyFocusGroup(listRestorer),
            contentPadding = contentPadding ?: PaddingValues(
                start = 48.dp,
                end = 48.dp,
                top = if (header != null) HeaderTopPadding else 24.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (header != null) item(key = HEADER_KEY) { header() }
            itemsIndexed(channels, key = { index, ch -> ch.gridKey(index) }) { index, channel ->
                ChannelListRow(
                    channel = channel,
                    onClick = { onChannelClick(channel) },
                    modifier = listRestorer.itemModifier(index, channels.size).onFocusChanged {
                        if (it.isFocused && header != null && index == 0) {
                            scope.launch { revealHeader { listState.animateScrollToItem(0) } }
                        }
                    }.then(
                        if (drawerFocus != null) {
                            Modifier.focusProperties { left = drawerFocus }
                        } else {
                            Modifier
                        },
                    ),
                )
            }
        }
        else -> LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = TvChannelGridMinCell),
            state = gridState,
            modifier = modifier.fillMaxSize().tvLazyFocusGroup(listRestorer),
            contentPadding = contentPadding ?: PaddingValues(
                start = 48.dp,
                end = 48.dp,
                top = if (header != null) HeaderTopPadding else 48.dp,
                bottom = 48.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (header != null) {
                item(key = HEADER_KEY, span = { GridItemSpan(maxLineSpan) }) { header() }
            }
            itemsIndexed(channels, key = { index, ch -> ch.gridKey(index) }) { index, channel ->
                ChannelCard(
                    channel = channel,
                    onClick = { onChannelClick(channel) },
                    modifier = listRestorer.itemModifier(index, channels.size)
                        .leftFromFirstColumn(gridState, index + offset, drawerFocus)
                        .onFocusChanged {
                            val firstRow = header != null && gridState.layoutInfo.visibleItemsInfo
                                .firstOrNull { info -> info.index == index + offset }?.row == 1
                            if (it.isFocused && firstRow) {
                                scope.launch { revealHeader { gridState.animateScrollToItem(0) } }
                            }
                        },
                )
            }
        }
    }
}

/** Focus bring-into-view starts its own scroll right after the focus change; start ours after it so it wins. */
private suspend fun revealHeader(scrollToTop: suspend () -> Unit) {
    withFrameNanos { }
    scrollToTop()
}

private const val HEADER_KEY = "header"
private val HeaderTopPadding = 16.dp

private fun ChannelUi.gridKey(index: Int): Any =
    if (id != 0L) id else "i$index|$name|$link|$desc"
