package tv.hdonlinetv.compose.ui.mobile.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.PlaylistType
import tv.hdonlinetv.compose.core.domain.model.PlaylistUi
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistViewModel
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalPhoneNavController
import tv.hdonlinetv.compose.phone.LocalPlaylistRepository
import tv.hdonlinetv.compose.ui.mobile.components.LegacyEmptyState
import tv.hdonlinetv.compose.ui.mobile.components.PlaylistCard

@Composable
fun PlaylistTabScreen() {
    val repository = LocalPlaylistRepository.current
    val viewModel: PlaylistViewModel = viewModel(factory = PlaylistViewModelFactory(repository))
    val state by viewModel.uiState.collectAsState()
    val navController = LocalPhoneNavController.current
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

@Composable
fun PlaylistTabScreenBody(
    playlists: List<PlaylistUi>,
    isLoading: Boolean,
    onPlaylistClick: (PlaylistUi) -> Unit,
    onDelete: (Long) -> Unit,
    onRefresh: (PlaylistUi) -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<PlaylistUi?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.bgMain)),
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            playlists.isEmpty() -> {
                LegacyEmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    message = stringResource(R.string.no_item),
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(4.dp),
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist) },
                            onRefresh = { onRefresh(playlist) },
                            onDelete = { pendingDelete = playlist },
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { playlist ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.delete_playlist)) },
            text = {
                Text(stringResource(R.string.confirm_delete_playlist, playlist.title))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDelete = null
                        onDelete(playlist.id)
                    },
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}
