package tv.hdonlinetv.compose.ui.tv.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.tv.LocalTvDrawerFocusRequester
import tv.hdonlinetv.compose.ui.components.TvChannelGridHPadding
import tv.hdonlinetv.compose.ui.components.TvChannelGridMinCell
import tv.hdonlinetv.compose.ui.components.TvChannelGridSpacing
import tv.hdonlinetv.compose.ui.components.adaptiveColumnCount

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ChannelGridBody(
    channels: List<ChannelUi>,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onChannelClick: (ChannelUi) -> Unit,
) {
    val drawerFocus = LocalTvDrawerFocusRequester.current
    val listMode = LocalSettingsRepository.current.getSettings().gridColumns <= 1

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columns = adaptiveColumnCount(
            availableWidth = maxWidth,
            minCell = TvChannelGridMinCell,
            horizontalPadding = TvChannelGridHPadding,
            spacing = TvChannelGridSpacing,
        )
        when {
            isLoading -> {
                Text(
                    text = stringResource(R.string.loading),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            channels.isEmpty() -> {
                Text(
                    text = stringResource(R.string.no_item),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            listMode -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 48.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(channels, key = { index, ch -> ch.gridKey(index) }) { _, channel ->
                        ChannelListRow(
                            channel = channel,
                            onClick = { onChannelClick(channel) },
                            modifier = if (drawerFocus != null) {
                                Modifier.focusProperties { left = drawerFocus }
                            } else {
                                Modifier
                            },
                        )
                    }
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = TvChannelGridMinCell),
                    contentPadding = PaddingValues(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(channels, key = { index, ch -> ch.gridKey(index) }) { index, channel ->
                        ChannelCard(
                            channel = channel,
                            onClick = { onChannelClick(channel) },
                            modifier = if (drawerFocus != null && index % columns == 0) {
                                Modifier.focusProperties { left = drawerFocus }
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

private fun ChannelUi.gridKey(index: Int): Any =
    if (id != 0L) id else "i$index|$name|$link|$desc"
