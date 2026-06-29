package tv.hdonlinetv.compose.ui.mobile.channel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.hdonlinetv.compose.core.presentation.channel.ChannelListViewModel
import tv.hdonlinetv.compose.core.presentation.channel.ChannelListViewModelFactory
import tv.hdonlinetv.compose.navigation.navigateToChannel
import tv.hdonlinetv.compose.phone.LocalChannelRepository
import tv.hdonlinetv.compose.phone.LocalPhoneNavController
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.ui.mobile.components.ChannelGridBody

@Composable
fun AllChannelsScreen() {
    val repository = LocalChannelRepository.current
    val settingsRepository = LocalSettingsRepository.current
    val viewModel: ChannelListViewModel = viewModel(
        factory = ChannelListViewModelFactory(repository, settingsRepository, categoryName = null),
    )
    val state by viewModel.uiState.collectAsState()
    val navController = LocalPhoneNavController.current
    val settings = settingsRepository.getSettings()
    ChannelGridBody(
        channels = state.channels,
        isLoading = state.isLoading,
        onChannelClick = { channel -> navigateToChannel(navController, channel, settings) },
    )
}
