package tv.hdonlinetv.compose.ui.tv.playlist

import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.presentation.error.UiError
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistManageType
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistManageViewModel
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistManageViewModelFactory
import tv.hdonlinetv.compose.phone.LocalPlaylistRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.tv.components.TvEditableField
import tv.hdonlinetv.compose.ui.tv.components.TvLoadingOverlay
import tv.hdonlinetv.compose.ui.tv.notifications.LocalTvNotificationManager
import tv.hdonlinetv.compose.ui.tv.notifications.NotificationType

@Composable
fun PlaylistManageScreen() {
    val navController = LocalTvNavController.current
    val context = LocalContext.current
    val notifications = LocalTvNotificationManager.current
    val repository = LocalPlaylistRepository.current
    val viewModel: PlaylistManageViewModel = viewModel(
        factory = PlaylistManageViewModelFactory(repository),
    )
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.error, state.titleError, state.urlError, state.usernameError, state.passwordError) {
        val error = state.error ?: return@LaunchedEffect
        val messageRes = when (error) {
            UiError.Validation -> when {
                state.titleError -> R.string.error_playlist_name
                state.urlError -> R.string.error_playlist_link
                state.usernameError -> R.string.error_playlist_username
                state.passwordError -> R.string.error_playlist_password
                else -> R.string.error_download_failed
            }
            UiError.Network, UiError.Unknown -> R.string.error_download_failed
        }
        notifications.show(
            title = context.getString(R.string.playlist_management),
            message = context.getString(messageRes),
            type = NotificationType.ERROR,
        )
        viewModel.clearError()
    }

    LaunchedEffect(state.saved) {
        if (!state.saved) return@LaunchedEffect
        val messageRes = if (state.type == PlaylistManageType.XTREAM) {
            R.string.xtream_success_saved
        } else {
            R.string.download_successful
        }
        notifications.show(
            title = context.getString(R.string.playlist_management),
            message = context.getString(messageRes),
            type = NotificationType.SUCCESS,
        )
        navController.popBackStack()
    }

    PlaylistManageScreenBody(
        type = state.type,
        title = state.title,
        url = state.url,
        username = state.username,
        password = state.password,
        useLocalFile = state.useLocalFile,
        isSaving = state.isSaving,
        titleError = state.titleError,
        urlError = state.urlError,
        usernameError = state.usernameError,
        passwordError = state.passwordError,
        onTypeChange = viewModel::onTypeChange,
        onTitleChange = viewModel::onTitleChange,
        onUrlChange = viewModel::onUrlChange,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLocalFileToggle = viewModel::onLocalFileToggle,
        onSave = viewModel::saveFromUrl,
        onSaveFile = viewModel::saveFromFile,
        onParseClipboard = viewModel::saveFromClipboard,
        onBack = { navController.popBackStack() },
    )

    TvLoadingOverlay(visible = state.isSaving)
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlaylistManageScreenBody(
    type: PlaylistManageType,
    title: String,
    url: String,
    username: String,
    password: String,
    useLocalFile: Boolean,
    isSaving: Boolean,
    titleError: Boolean,
    urlError: Boolean,
    usernameError: Boolean,
    passwordError: Boolean,
    onTypeChange: (PlaylistManageType) -> Unit,
    onTitleChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLocalFileToggle: (Boolean) -> Unit,
    onSave: () -> Unit,
    onSaveFile: (Uri) -> Unit,
    onParseClipboard: (String) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val playlistTypes = stringArrayResource(R.array.playlist_types)
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) onSaveFile(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 48.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = onBack) {
            Text(text = stringResource(R.string.ok))
        }
        Text(text = stringResource(R.string.playlist_management))

        Text(text = stringResource(R.string.playlist_name))
        Button(
            onClick = {
                onTypeChange(
                    if (type == PlaylistManageType.M3U) PlaylistManageType.XTREAM
                    else PlaylistManageType.M3U,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            val label = playlistTypes[if (type == PlaylistManageType.M3U) 0 else 1]
            Text(text = label)
        }

        TvEditableField(
            value = title,
            onValueChange = onTitleChange,
            hint = stringResource(R.string.playlist_name),
            isError = titleError,
        )

        if (type == PlaylistManageType.M3U && useLocalFile) {
            Button(
                onClick = { filePicker.launch(arrayOf("*/*")) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.playlist_select_file))
            }
        } else {
            TvEditableField(
                value = url,
                onValueChange = onUrlChange,
                hint = stringResource(R.string.playlist_link),
                isError = urlError,
            )
        }

        if (type == PlaylistManageType.XTREAM) {
            TvEditableField(
                value = username,
                onValueChange = onUsernameChange,
                hint = stringResource(R.string.username_hint),
                isError = usernameError,
            )
            TvEditableField(
                value = password,
                onValueChange = onPasswordChange,
                hint = stringResource(R.string.password_hint),
                isError = passwordError,
            )
        }

        if (type == PlaylistManageType.M3U) {
            Button(
                onClick = { onLocalFileToggle(!useLocalFile) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.local_storage) +
                        if (useLocalFile) ": ON" else ": OFF",
                )
            }
        }

        Button(
            onClick = onSave,
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.subscribe))
        }

        if (type == PlaylistManageType.M3U) {
            Button(
                onClick = { readClipboard(context)?.let(onParseClipboard) },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.parse_clipboard))
            }
        }
    }
}

private fun readClipboard(context: Context): String? {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
    if (text.isNullOrBlank()) {
        Toast.makeText(context, R.string.clipboard_unavailable, Toast.LENGTH_SHORT).show()
        return null
    }
    return text
}
