package tv.hdonlinetv.compose.ui.tv.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistChannelsViewModel
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistChannelsViewModelFactory
import tv.hdonlinetv.compose.navigation.PlayerBrowseScope
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.navigation.navigateToChannel
import tv.hdonlinetv.compose.phone.LocalChannelRepository
import tv.hdonlinetv.compose.phone.LocalPlaylistRepository
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.tv.components.ChannelGridBody
import tv.hdonlinetv.compose.ui.tv.components.TvLoadingOverlay
import tv.hdonlinetv.compose.ui.tv.notifications.LocalTvNotificationManager
import tv.hdonlinetv.compose.ui.tv.notifications.NotificationType
import tv.hdonlinetv.compose.util.PlaylistRefreshFeedback

@Composable
fun PlaylistChannelsScreen() {
    val navController = LocalTvNavController.current
    val args = navController.currentBackStackEntry?.arguments
    val playlistId = args?.getLong(Routes.PlaylistChannels.ARG_PLAYLIST_ID) ?: 0L
    val playlistTitle = args?.getString(Routes.PlaylistChannels.ARG_PLAYLIST_TITLE).orEmpty()
    val channelRepository = LocalChannelRepository.current
    val playlistRepository = LocalPlaylistRepository.current
    val settingsRepository = LocalSettingsRepository.current
    val notifications = LocalTvNotificationManager.current
    val context = LocalContext.current
    val viewModel: PlaylistChannelsViewModel = viewModel(
        factory = PlaylistChannelsViewModelFactory(
            channelRepository,
            settingsRepository,
            playlistRepository,
            playlistId,
            playlistTitle,
        ),
    )
    val state by viewModel.uiState.collectAsState()
    val settings = settingsRepository.getSettings()

    LaunchedEffect(state.refreshResult) {
        val result = state.refreshResult ?: return@LaunchedEffect
        notifications.show(
            title = context.getString(R.string.refresh),
            message = PlaylistRefreshFeedback.message(context, result),
            type = if (PlaylistRefreshFeedback.isSuccess(result)) {
                NotificationType.SUCCESS
            } else {
                NotificationType.ERROR
            },
        )
        viewModel.clearRefreshResult()
    }

    PlaylistChannelsScreenBody(
        title = state.title,
        channels = state.channels,
        isLoading = state.isLoading,
        onBack = { navController.popBackStack() },
        onRefresh = viewModel::refresh,
        onDelete = {
            viewModel.delete {
                navController.popBackStack()
            }
        },
        onChannelClick = { channel ->
            navigateToChannel(
                navController,
                channel,
                settings,
                scope = PlayerBrowseScope.Playlist(playlistId),
            )
        },
    )
    TvLoadingOverlay(visible = state.isLoading || state.isRefreshing)
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlaylistChannelsScreenBody(
    title: String,
    channels: List<ChannelUi>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onDelete: () -> Unit,
    onChannelClick: (ChannelUi) -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 48.dp, vertical = 24.dp),
    ) {
        Button(onClick = onBack) {
            Text(text = stringResource(R.string.ok))
        }
        Text(
            text = title,
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(onClick = onRefresh) {
                PlaylistActionLabel(
                    icon = Icons.Filled.Refresh,
                    text = stringResource(R.string.refresh),
                )
            }
            Button(onClick = { showDeleteConfirm = true }) {
                PlaylistActionLabel(
                    icon = Icons.Filled.Delete,
                    text = stringResource(R.string.delete),
                )
            }
        }
        ChannelGridBody(
            channels = channels,
            isLoading = false,
            onChannelClick = onChannelClick,
        )
    }
    if (showDeleteConfirm) {
        TvConfirmDeletePlaylistDialog(
            playlistTitle = title,
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun RowScope.PlaylistActionLabel(
    icon: ImageVector,
    text: String,
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(22.dp),
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(text = text)
}
