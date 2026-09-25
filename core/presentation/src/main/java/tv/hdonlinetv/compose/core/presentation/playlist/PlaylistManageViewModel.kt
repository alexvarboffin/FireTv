package tv.hdonlinetv.compose.core.presentation.playlist

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.hdonlinetv.compose.core.domain.model.PlaylistUi
import tv.hdonlinetv.compose.core.domain.repository.PlaylistRepository
import tv.hdonlinetv.compose.core.presentation.error.UiError

enum class PlaylistManageType {
    M3U,
    XTREAM,
}

data class PlaylistManageUiState(
    val type: PlaylistManageType = PlaylistManageType.M3U,
    val title: String = "",
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val useLocalFile: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: UiError? = null,
    val titleError: Boolean = false,
    val urlError: Boolean = false,
    val usernameError: Boolean = false,
    val passwordError: Boolean = false,
)

class PlaylistManageViewModel(
    private val repository: PlaylistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistManageUiState())
    val uiState: StateFlow<PlaylistManageUiState> = _uiState.asStateFlow()

    fun onTypeChange(type: PlaylistManageType) {
        _uiState.update {
            it.copy(
                type = type,
                useLocalFile = if (type == PlaylistManageType.XTREAM) false else it.useLocalFile,
                titleError = false,
                urlError = false,
                usernameError = false,
                passwordError = false,
            )
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, titleError = false) }
    }

    fun onUrlChange(value: String) {
        _uiState.update { it.copy(url = value, urlError = false) }
    }

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, usernameError = false) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = false) }
    }

    fun onLocalFileToggle(enabled: Boolean) {
        _uiState.update { it.copy(useLocalFile = enabled, urlError = false) }
    }

    fun saveFromUrl() {
        saveInternal(fileUri = null, clipboardContent = null)
    }

    fun saveFromFile(uri: Uri) {
        saveInternal(fileUri = uri, clipboardContent = null)
    }

    fun saveFromClipboard(content: String) {
        saveInternal(fileUri = null, clipboardContent = content)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun consumeSaved() {
        _uiState.value = PlaylistManageUiState()
    }

    private fun saveInternal(fileUri: Uri?, clipboardContent: String?) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val id = when {
                    clipboardContent != null -> repository.addFromClipboardM3u(clipboardContent)
                    state.type == PlaylistManageType.XTREAM -> {
                        if (!validateXtream(state)) return@launch
                        repository.addXtream(
                            title = state.title.trim(),
                            serverUrl = state.url.trim(),
                            username = state.username.trim(),
                            password = state.password.trim(),
                        )
                    }
                    fileUri != null -> {
                        if (state.title.isBlank() && fileUri.lastPathSegment.isNullOrBlank()) {
                            _uiState.update {
                                it.copy(
                                    titleError = true,
                                    isSaving = false,
                                    error = UiError.Validation,
                                )
                            }
                            return@launch
                        }
                        repository.addFromFile(state.title.trim(), fileUri)
                    }
                    else -> {
                        if (!validateM3uUrl(state)) return@launch
                        repository.addFromUrl(state.title.trim(), state.url.trim())
                    }
                }
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saved = id != null,
                        error = if (id == null) UiError.Unknown else null,
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isSaving = false, error = UiError.Unknown) }
            }
        }
    }

    private fun validateM3uUrl(state: PlaylistManageUiState): Boolean {
        val urlError = state.url.isBlank()
        if (urlError) {
            _uiState.update {
                it.copy(
                    urlError = true,
                    isSaving = false,
                    error = UiError.Validation,
                )
            }
            return false
        }
        return true
    }

    private fun validateXtream(state: PlaylistManageUiState): Boolean {
        val urlError = state.url.isBlank()
        val usernameError = state.username.isBlank()
        val passwordError = state.password.isBlank()
        val invalid = urlError || usernameError || passwordError
        if (invalid) {
            _uiState.update {
                it.copy(
                    urlError = urlError,
                    usernameError = usernameError,
                    passwordError = passwordError,
                    isSaving = false,
                    error = UiError.Validation,
                )
            }
            return false
        }
        return true
    }
}

class PlaylistManageViewModelFactory(
    private val repository: PlaylistRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaylistManageViewModel::class.java)) {
            return PlaylistManageViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
