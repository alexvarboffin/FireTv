package tv.hdonlinetv.compose.ui.tv.playlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistChannelsViewModel
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistChannelsViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.navigation.navigateToChannel
import tv.hdonlinetv.compose.phone.LocalChannelRepository
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.mobile.playlist.PlaylistChannelsScreenBody

@Composable
fun PlaylistChannelsScreen() {
    val navController = LocalTvNavController.current
    val args = navController.currentBackStackEntry?.arguments
    val playlistId = args?.getLong(Routes.PlaylistChannels.ARG_PLAYLIST_ID) ?: 0L
    val playlistTitle = args?.getString(Routes.PlaylistChannels.ARG_PLAYLIST_TITLE).orEmpty()
    val channelRepository = LocalChannelRepository.current
    val settingsRepository = LocalSettingsRepository.current
    val viewModel: PlaylistChannelsViewModel = viewModel(
        factory = PlaylistChannelsViewModelFactory(
            channelRepository,
            settingsRepository,
            playlistId,
            playlistTitle,
        ),
    )
    val state by viewModel.uiState.collectAsState()
    val settings = settingsRepository.getSettings()
    PlaylistChannelsScreenBody(
        title = state.title,
        channels = state.channels,
        isLoading = state.isLoading,
        onBack = { navController.popBackStack() },
        onChannelClick = { channel -> navigateToChannel(navController, channel, settings) },
    )
}
