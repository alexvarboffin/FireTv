package tv.hdonlinetv.compose.ui.mobile.channel

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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.presentation.channel.ChannelListViewModel
import tv.hdonlinetv.compose.core.presentation.channel.ChannelListViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.navigation.navigateToChannel
import tv.hdonlinetv.compose.phone.LocalChannelRepository
import tv.hdonlinetv.compose.phone.LocalPhoneNavController
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.ui.mobile.components.ChannelGridBody
import tv.hdonlinetv.compose.ui.mobile.components.LegacyTopAppBar

@Composable
fun ChannelListScreen() {
    val navController = LocalPhoneNavController.current
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
        onBack = { navController.popBackStack() },
        onChannelClick = { channel -> navigateToChannel(navController, channel, settings) },
    )
}

@Composable
fun ChannelListScreenBody(
    title: String,
    channels: List<ChannelUi>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onChannelClick: (ChannelUi) -> Unit,
) {
    Scaffold(
        containerColor = colorResource(R.color.bgMain),
        topBar = {
            LegacyTopAppBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
