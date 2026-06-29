package tv.hdonlinetv.compose.ui.tv.favorites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.hdonlinetv.compose.core.presentation.channel.FavoritesViewModel
import tv.hdonlinetv.compose.core.presentation.channel.FavoritesViewModelFactory
import tv.hdonlinetv.compose.navigation.navigateToChannel
import tv.hdonlinetv.compose.phone.LocalChannelRepository
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.tv.components.ChannelGridBody

@Composable
fun FavoritesScreen() {
    val repository = LocalChannelRepository.current
    val settingsRepository = LocalSettingsRepository.current
    val viewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModelFactory(repository),
    )
    val state by viewModel.uiState.collectAsState()
    val navController = LocalTvNavController.current
    val settings = settingsRepository.getSettings()
    ChannelGridBody(
        channels = state.channels,
        isLoading = state.isLoading,
        onChannelClick = { channel -> navigateToChannel(navController, channel, settings) },
    )
}
