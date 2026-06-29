package tv.hdonlinetv.compose.ui.tv.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.hdonlinetv.compose.core.presentation.player.PlayerViewModel
import tv.hdonlinetv.compose.core.presentation.player.PlayerViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalChannelRepository
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.mobile.player.PlayerScreenBody

@Composable
fun PlayerScreen() {
    val navController = LocalTvNavController.current
    val args = navController.currentBackStackEntry?.arguments
    val channelId = args?.getLong(Routes.Player.ARG_CHANNEL_ID) ?: 0L
    val streamUrl = args?.getString(Routes.Player.ARG_STREAM_URL).orEmpty()
    val streamTitle = args?.getString(Routes.Player.ARG_STREAM_TITLE).orEmpty()
    val repository = LocalChannelRepository.current
    val settingsRepository = LocalSettingsRepository.current
    var mediaPlayerOption by remember {
        mutableIntStateOf(settingsRepository.getSettings().mediaPlayerOption)
    }
    val viewModel: PlayerViewModel = viewModel(
        factory = PlayerViewModelFactory(
            repository = repository,
            channelId = channelId,
            directStreamUrl = streamUrl.takeIf { it.isNotBlank() },
            directStreamTitle = streamTitle.takeIf { it.isNotBlank() },
        ),
    )
    val state by viewModel.uiState.collectAsState()
    PlayerScreenBody(
        channel = state.channel,
        isLoading = state.isLoading,
        mediaPlayerOption = mediaPlayerOption,
        onBack = { navController.popBackStack() },
        onToggleFavorite = { viewModel.toggleFavorite() },
        onMediaPlayerOptionChange = { option ->
            settingsRepository.setMediaPlayerOption(option)
            mediaPlayerOption = option
        },
    )
}
