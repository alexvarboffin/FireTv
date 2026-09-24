package tv.hdonlinetv.compose.ui.tv.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.PlaylistType
import tv.hdonlinetv.compose.core.domain.model.PlaylistUi
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistViewModel
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalPlaylistRepository
import tv.hdonlinetv.compose.tv.LocalTvDrawerFocusRequester
import tv.hdonlinetv.compose.util.PlaylistMetaFormat
import java.util.Locale
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.tv.components.TvLoadingOverlay

@Composable
fun PlaylistTabScreen() {
    val repository = LocalPlaylistRepository.current
    val viewModel: PlaylistViewModel = viewModel(factory = PlaylistViewModelFactory(repository))
    val state by viewModel.uiState.collectAsState()
    val navController = LocalTvNavController.current
    var actionsFor by remember { mutableStateOf<PlaylistUi?>(null) }
    var pendingDelete by remember { mutableStateOf<PlaylistUi?>(null) }

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
        onPlaylistLongClick = { playlist -> actionsFor = playlist },
    )

    actionsFor?.let { playlist ->
        TvPlaylistActionsDialog(
            playlistTitle = playlist.title,
            onRefresh = {
                actionsFor = null
                viewModel.refresh(playlist)
            },
            onDelete = {
                actionsFor = null
                pendingDelete = playlist
            },
            onDismiss = { actionsFor = null },
        )
    }

    pendingDelete?.let { playlist ->
        TvConfirmDeletePlaylistDialog(
            playlistTitle = playlist.title,
            onConfirm = {
                pendingDelete = null
                viewModel.delete(playlist.id)
            },
            onDismiss = { pendingDelete = null },
        )
    }

    TvLoadingOverlay(visible = state.isLoading && state.playlists.isNotEmpty())
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlaylistTabScreenBody(
    playlists: List<PlaylistUi>,
    isLoading: Boolean,
    onPlaylistClick: (PlaylistUi) -> Unit,
    onPlaylistLongClick: (PlaylistUi) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading && playlists.isEmpty() -> {
                Text(
                    text = stringResource(R.string.loading),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            playlists.isEmpty() -> {
                Text(
                    text = stringResource(R.string.no_item),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 48.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        PlaylistCardTv(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist) },
                            onLongClick = { onPlaylistLongClick(playlist) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PlaylistCardTv(
    playlist: PlaylistUi,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val drawerFocus = LocalTvDrawerFocusRequester.current
    // Cinema MovieCardTvSimple: long-press only arms a flag; invoke on KeyUp.
    // Otherwise KeyUp after long-press lands on the dialog's focused button and fires it.
    var isKeyDown by remember { mutableStateOf(false) }
    var longPressTriggered by remember { mutableStateOf(false) }
    val longPressThresholdMs = 500L

    LaunchedEffect(isKeyDown) {
        if (isKeyDown && !longPressTriggered) {
            delay(longPressThresholdMs)
            longPressTriggered = true
        }
    }

    Card(
        onClick = {
            // Click path for pointer/mouse; D-pad handled in onPreviewKeyEvent.
            if (!longPressTriggered) onClick()
        },
        onLongClick = {
            // Pointer long-press; D-pad uses KeyUp path below.
            onLongClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (drawerFocus != null) {
                    Modifier.focusProperties { left = drawerFocus }
                } else {
                    Modifier
                },
            )
            .onPreviewKeyEvent { event ->
                when (event.key) {
                    Key.Enter, Key.DirectionCenter -> {
                        when (event.type) {
                            KeyEventType.KeyDown -> {
                                if (!isKeyDown) {
                                    isKeyDown = true
                                    longPressTriggered = false
                                }
                                true
                            }
                            KeyEventType.KeyUp -> {
                                if (isKeyDown) {
                                    if (longPressTriggered) {
                                        onLongClick()
                                    } else {
                                        onClick()
                                    }
                                }
                                isKeyDown = false
                                longPressTriggered = false
                                true
                            }
                            else -> false
                        }
                    }
                    else -> false
                }
            },
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = playlist.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val channelsFmt = stringResource(R.string.channels_format)
            val updatedFmt = stringResource(R.string.playlist_updated_short)
            val meta = remember(playlist, channelsFmt, updatedFmt) {
                PlaylistMetaFormat.buildMeta(
                    playlist = playlist,
                    channelsLabel = { count ->
                        String.format(Locale.getDefault(), channelsFmt, count)
                    },
                    updatedLabel = { date ->
                        String.format(Locale.getDefault(), updatedFmt, date)
                    },
                )
            }
            if (meta.isNotEmpty()) {
                Text(
                    text = meta,
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvPlaylistActionsDialog(
    playlistTitle: String,
    onRefresh: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val cancelFocus = remember { FocusRequester() }
    // Ignore accidental activate for a beat after open (pointer long-press release).
    var actionsArmed by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        LaunchedEffect(Unit) {
            delay(320)
            actionsArmed = true
            cancelFocus.requestFocus()
        }
        Column(
            modifier = Modifier
                .width(480.dp)
                .background(colors.surface)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = playlistTitle)
            Button(
                onClick = { if (actionsArmed) onRefresh() },
                enabled = actionsArmed,
                modifier = Modifier.fillMaxWidth(),
            ) {
                PlaylistDialogActionLabel(
                    icon = Icons.Filled.Refresh,
                    text = stringResource(R.string.refresh),
                )
            }
            Button(
                onClick = { if (actionsArmed) onDelete() },
                enabled = actionsArmed,
                modifier = Modifier.fillMaxWidth(),
            ) {
                PlaylistDialogActionLabel(
                    icon = Icons.Filled.Delete,
                    text = stringResource(R.string.delete),
                )
            }
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(cancelFocus),
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvConfirmDeletePlaylistDialog(
    playlistTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val cancelFocus = remember { FocusRequester() }
    var actionsArmed by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        LaunchedEffect(Unit) {
            delay(320)
            actionsArmed = true
            cancelFocus.requestFocus()
        }
        Column(
            modifier = Modifier
                .width(480.dp)
                .background(colors.surface)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = stringResource(R.string.delete_playlist))
            Text(text = stringResource(R.string.confirm_delete_playlist, playlistTitle))
            Button(
                onClick = { if (actionsArmed) onConfirm() },
                enabled = actionsArmed,
                modifier = Modifier.fillMaxWidth(),
            ) {
                PlaylistDialogActionLabel(
                    icon = Icons.Filled.Delete,
                    text = stringResource(R.string.delete),
                )
            }
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(cancelFocus),
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun RowScope.PlaylistDialogActionLabel(
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
