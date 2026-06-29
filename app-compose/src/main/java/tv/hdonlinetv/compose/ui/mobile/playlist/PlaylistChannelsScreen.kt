package tv.hdonlinetv.compose.ui.mobile.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistChannelsViewModel
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistChannelsViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.navigation.navigateToChannel
import tv.hdonlinetv.compose.phone.LocalChannelRepository
import tv.hdonlinetv.compose.phone.LocalPhoneNavController
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.ui.mobile.components.ChannelGridBody
import tv.hdonlinetv.compose.ui.mobile.components.LegacyTopAppBar

@Composable
fun PlaylistChannelsScreen() {
    val navController = LocalPhoneNavController.current
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

@Composable
fun PlaylistChannelsScreenBody(
    title: String,
    channels: List<tv.hdonlinetv.compose.core.domain.model.ChannelUi>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onChannelClick: (tv.hdonlinetv.compose.core.domain.model.ChannelUi) -> Unit,
) {
    Scaffold(
        containerColor = colorResource(R.color.bgMain),
        topBar = {
            LegacyTopAppBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = colorResource(R.color.black),
                        )
                    }
                },
            )
        },
    ) { padding ->
        ChannelGridBody(
            channels = channels,
            isLoading = isLoading,
            modifier = Modifier
                .padding(padding)
                .background(colorResource(R.color.bgMain)),
            onChannelClick = onChannelClick,
        )
    }
}
