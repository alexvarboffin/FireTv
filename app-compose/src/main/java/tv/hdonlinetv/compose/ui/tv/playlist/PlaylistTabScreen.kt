package tv.hdonlinetv.compose.ui.tv.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.PlaylistType
import tv.hdonlinetv.compose.core.domain.model.PlaylistUi
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistViewModel
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalPlaylistRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.tv.components.TvLoadingOverlay

@Composable
fun PlaylistTabScreen() {
    val repository = LocalPlaylistRepository.current
    val viewModel: PlaylistViewModel = viewModel(factory = PlaylistViewModelFactory(repository))
    val state by viewModel.uiState.collectAsState()
    val navController = LocalTvNavController.current
    var actionsFor by remember { mutableStateOf<PlaylistUi?>(null) }

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
        onPlaylistLongClick = { playlist -> actionsFor = playlist },
    )

    actionsFor?.let { playlist ->
        TvPlaylistActionsDialog(
            playlistTitle = playlist.title,
            onRefresh = {
                actionsFor = null
                viewModel.refresh(playlist)
            },
            onDelete = {
                actionsFor = null
                viewModel.delete(playlist.id)
            },
            onDismiss = { actionsFor = null },
        )
    }

    TvLoadingOverlay(visible = state.isLoading && state.playlists.isNotEmpty())
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlaylistTabScreenBody(
    playlists: List<PlaylistUi>,
    isLoading: Boolean,
    onPlaylistClick: (PlaylistUi) -> Unit,
    onPlaylistLongClick: (PlaylistUi) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading && playlists.isEmpty() -> {
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
                            onLongClick = { onPlaylistLongClick(playlist) },
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
    onLongClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        onLongClick = onLongClick,
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
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvPlaylistActionsDialog(
    playlistTitle: String,
    onRefresh: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val firstFocus = remember { FocusRequester() }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        LaunchedEffect(Unit) {
            firstFocus.requestFocus()
        }
        Column(
            modifier = Modifier
                .width(480.dp)
                .background(colors.surface)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = playlistTitle)
            Button(
                onClick = onRefresh,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(firstFocus),
            ) {
                Text(text = stringResource(R.string.refresh))
            }
            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.delete))
            }
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    }
}
