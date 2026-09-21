package tv.hdonlinetv.compose.ui.tv.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import tv.hdonlinetv.compose.tv.LocalTvDrawerFocusRequester

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ChannelGridBody(
    channels: List<ChannelUi>,
    isLoading: Boolean,
    columns: Int = tv.hdonlinetv.compose.phone.LocalGridColumns.current,
    modifier: Modifier = Modifier,
    onChannelClick: (ChannelUi) -> Unit,
) {
    val drawerFocus = LocalTvDrawerFocusRequester.current
    Box(modifier = modifier.fillMaxSize()) {
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
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(channels, key = { _, ch -> ch.id }) { index, channel ->
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
