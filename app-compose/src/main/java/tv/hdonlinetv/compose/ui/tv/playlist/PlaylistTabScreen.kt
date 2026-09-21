package tv.hdonlinetv.compose.ui.tv.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.PlaylistType
import tv.hdonlinetv.compose.core.domain.model.PlaylistUi
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistViewModel
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalPlaylistRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController

@Composable
fun PlaylistTabScreen() {
    val repository = LocalPlaylistRepository.current
    val viewModel: PlaylistViewModel = viewModel(factory = PlaylistViewModelFactory(repository))
    val state by viewModel.uiState.collectAsState()
    val navController = LocalTvNavController.current
    PlaylistTabScreenBody(
        playlists = state.playlists,
        isLoading = state.isLoading,
        onPlaylistClick = { playlist ->
            if (playlist.type == PlaylistType.XTREAM_URL) {
                navController.navigate(Routes.XtreamBrowser.build(playlist.id))
            } else {
                navController.navigate(Routes.PlaylistChannels.build(playlist.id, playlist.title))
            }
        },
        onDelete = viewModel::delete,
        onRefresh = viewModel::refresh,
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlaylistTabScreenBody(
    playlists: List<PlaylistUi>,
    isLoading: Boolean,
    onPlaylistClick: (PlaylistUi) -> Unit,
    onDelete: (Long) -> Unit,
    onRefresh: (PlaylistUi) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                Text(
                    text = stringResource(R.string.loading),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            playlists.isEmpty() -> {
                Text(
                    text = stringResource(R.string.no_item),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 48.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        PlaylistCardTv(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist) },
                            onRefresh = { onRefresh(playlist) },
                            onDelete = { onDelete(playlist.id) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PlaylistCardTv(
    playlist: PlaylistUi,
    onClick: () -> Unit,
    onRefresh: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = playlist.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${playlist.count}",
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = onRefresh) {
                    Text(text = stringResource(R.string.refresh))
                }
                Button(onClick = onDelete) {
                    Text(text = stringResource(R.string.delete))
                }
            }
        }
    }
}
