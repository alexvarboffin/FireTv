package tv.hdonlinetv.compose.ui.mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.phone.LocalSettingsRepository

@Composable
fun ChannelGridBody(
    channels: List<ChannelUi>,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onChannelClick: (ChannelUi) -> Unit,
) {
    val columns = LocalSettingsRepository.current.getSettings().gridColumns.coerceAtLeast(1)
    val listMode = columns <= 1
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.bgMain)),
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            channels.isEmpty() -> {
                LegacyEmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                )
            }
            listMode -> {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    itemsIndexed(channels, key = { _, ch -> ch.id }) { _, channel ->
                        ChannelListRow(
                            channel = channel,
                            onClick = { onChannelClick(channel) },
                        )
                    }
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    itemsIndexed(channels, key = { _, ch -> ch.id }) { _, channel ->
                        ChannelCard(
                            channel = channel,
                            onClick = { onChannelClick(channel) },
                        )
                    }
                }
            }
        }
    }
}
