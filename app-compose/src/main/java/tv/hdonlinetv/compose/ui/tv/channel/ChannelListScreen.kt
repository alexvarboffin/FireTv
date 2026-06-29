package tv.hdonlinetv.compose.ui.tv.channel

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.presentation.channel.ChannelListViewModel
import tv.hdonlinetv.compose.core.presentation.channel.ChannelListViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.navigation.navigateToChannel
import tv.hdonlinetv.compose.phone.LocalChannelRepository
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.tv.components.ChannelGridBody

@Composable
fun ChannelListScreen() {
    val navController = LocalTvNavController.current
    val categoryName = navController.currentBackStackEntry
        ?.arguments
        ?.getString(Routes.Channels.ARG_CATEGORY_NAME)
        .orEmpty()
    val repository = LocalChannelRepository.current
    val settingsRepository = LocalSettingsRepository.current
    val viewModel: ChannelListViewModel = viewModel(
        factory = ChannelListViewModelFactory(repository, settingsRepository, categoryName),
    )
    val state by viewModel.uiState.collectAsState()
    val settings = settingsRepository.getSettings()
    ChannelListScreenBody(
        title = categoryName.ifEmpty { stringResource(R.string.tab_channel) },
        channels = state.channels,
        isLoading = state.isLoading,
        onChannelClick = { channel -> navigateToChannel(navController, channel, settings) },
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ChannelListScreenBody(
    title: String,
    channels: List<ChannelUi>,
    isLoading: Boolean,
    onChannelClick: (ChannelUi) -> Unit,
) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 48.dp, vertical = 16.dp),
    )
    ChannelGridBody(
        channels = channels,
        isLoading = isLoading,
        modifier = Modifier.fillMaxSize(),
        onChannelClick = onChannelClick,
    )
}
