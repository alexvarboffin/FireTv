package tv.hdonlinetv.compose.ui.tv.playlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.hdonlinetv.compose.core.domain.model.PlaylistType
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistViewModel
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalPlaylistRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.mobile.playlist.PlaylistTabScreenBody

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
