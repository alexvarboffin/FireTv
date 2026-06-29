package tv.hdonlinetv.compose.core.presentation.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.hdonlinetv.compose.core.domain.model.PlaylistUi
import tv.hdonlinetv.compose.core.domain.repository.PlaylistRepository
import tv.hdonlinetv.compose.core.presentation.error.UiError

data class PlaylistListUiState(
    val playlists: List<PlaylistUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiError? = null,
)

class PlaylistViewModel(
    private val repository: PlaylistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistListUiState(isLoading = true))
    val uiState: StateFlow<PlaylistListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAllPlaylists()
                .catch {
                    _uiState.update { state -> state.copy(isLoading = false, error = UiError.Unknown) }
                }
                .collect { playlists ->
                    _uiState.update {
                        it.copy(isLoading = false, playlists = playlists, error = null)
                    }
                }
        }
    }

    fun delete(playlistId: Long) {
        viewModelScope.launch {
            try {
                repository.deletePlaylist(playlistId)
            } catch (_: Exception) {
                _uiState.update { it.copy(error = UiError.Unknown) }
            }
        }
    }

    fun refresh(playlist: PlaylistUi) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.refreshFromUrl(playlist)
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, error = UiError.Unknown) }
            }
        }
    }
}

class PlaylistViewModelFactory(
    private val repository: PlaylistRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaylistViewModel::class.java)) {
            return PlaylistViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
