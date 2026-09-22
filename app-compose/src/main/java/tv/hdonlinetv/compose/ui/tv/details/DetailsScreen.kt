package tv.hdonlinetv.compose.ui.tv.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.ui.components.RemoteImage
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.presentation.channel.DetailsViewModel
import tv.hdonlinetv.compose.core.presentation.channel.DetailsViewModelFactory
import tv.hdonlinetv.compose.navigation.PlayerBrowseScope
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalChannelRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController

@Composable
fun DetailsScreen() {
    val navController = LocalTvNavController.current
    val channelId = navController.currentBackStackEntry
        ?.arguments
        ?.getLong(Routes.Details.ARG_CHANNEL_ID) ?: 0L
    val browseScope = PlayerBrowseScope.parse(
        type = navController.currentBackStackEntry?.arguments?.getString(Routes.Details.ARG_SCOPE),
        key = navController.currentBackStackEntry?.arguments?.getString(Routes.Details.ARG_SCOPE_KEY),
    )
    val repository = LocalChannelRepository.current
    val viewModel: DetailsViewModel = viewModel(
        factory = DetailsViewModelFactory(repository, channelId),
    )
    val state by viewModel.uiState.collectAsState()
    DetailsScreenBody(
        channel = state.channel,
        isLoading = state.isLoading,
        onPlay = { navController.navigate(Routes.Player.build(channelId, browseScope)) },
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DetailsScreenBody(
    channel: ChannelUi?,
    isLoading: Boolean,
    onPlay: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                Text(
                    text = stringResource(R.string.loading),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            channel == null -> {
                Text(
                    text = stringResource(R.string.no_item),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(48.dp),
                ) {
                    RemoteImage(
                        url = channel.cover,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .aspectRatio(16f / 9f),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = channel.name)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = channel.desc.orEmpty())
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onPlay) {
                        Text(text = stringResource(R.string.play))
                    }
                }
            }
        }
    }
}
