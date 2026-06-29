package tv.hdonlinetv.compose.ui.tv.playlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistManageViewModel
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistManageViewModelFactory
import tv.hdonlinetv.compose.phone.LocalPlaylistRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.mobile.playlist.PlaylistManageScreenBody

@Composable
fun PlaylistManageScreen() {
    val navController = LocalTvNavController.current
    val repository = LocalPlaylistRepository.current
    val viewModel: PlaylistManageViewModel = viewModel(
        factory = PlaylistManageViewModelFactory(repository),
    )
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(state.saved) {
        if (state.saved) navController.popBackStack()
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
    )
}
