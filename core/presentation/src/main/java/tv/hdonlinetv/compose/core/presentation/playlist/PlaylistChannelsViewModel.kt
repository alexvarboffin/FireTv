package tv.hdonlinetv.compose.core.presentation.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.domain.model.PlaylistRefreshResult
import tv.hdonlinetv.compose.core.domain.repository.ChannelRepository
import tv.hdonlinetv.compose.core.domain.repository.PlaylistRepository
import tv.hdonlinetv.compose.core.domain.repository.SettingsRepository
import tv.hdonlinetv.compose.core.presentation.error.UiError

data class PlaylistChannelsUiState(
    val channels: List<ChannelUi> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val refreshResult: PlaylistRefreshResult? = null,
    val error: UiError? = null,
    val title: String = "",
)

class PlaylistChannelsViewModel(
    private val channelRepository: ChannelRepository,
    private val settingsRepository: SettingsRepository,
    private val playlistRepository: PlaylistRepository,
    private val playlistId: Long,
    private val playlistTitle: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PlaylistChannelsUiState(isLoading = true, title = playlistTitle),
    )
    val uiState: StateFlow<PlaylistChannelsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val sort = settingsRepository.getSettings().sortOption
                val channels = channelRepository.getChannelsInPlaylist(playlistId, sort)
                _uiState.update { it.copy(isLoading = false, channels = channels) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, error = UiError.Unknown) }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, refreshResult = null, error = null) }
            try {
                val playlist = playlistRepository.getPlaylistById(playlistId)
                if (playlist == null) {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            refreshResult = PlaylistRefreshResult.Failed,
                        )
                    }
                    return@launch
                }
                val result = playlistRepository.refreshFromUrl(playlist)
                when (result) {
                    is PlaylistRefreshResult.Success -> {
                        val sort = settingsRepository.getSettings().sortOption
                        val channels = channelRepository.getChannelsInPlaylist(playlistId, sort)
                        _uiState.update {
                            it.copy(
                                isRefreshing = false,
                                channels = channels,
                                refreshResult = result,
                            )
                        }
                    }
                    else -> {
                        // Keep current channels — empty/failed download must not wipe UI.
                        _uiState.update {
                            it.copy(isRefreshing = false, refreshResult = result)
                        }
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        refreshResult = PlaylistRefreshResult.Failed,
                    )
                }
            }
        }
    }

    fun clearRefreshResult() {
        _uiState.update { it.copy(refreshResult = null) }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                playlistRepository.deletePlaylist(playlistId)
                onDeleted()
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, error = UiError.Unknown) }
            }
        }
    }
}

class PlaylistChannelsViewModelFactory(
    private val channelRepository: ChannelRepository,
    private val settingsRepository: SettingsRepository,
    private val playlistRepository: PlaylistRepository,
    private val playlistId: Long,
    private val playlistTitle: String,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaylistChannelsViewModel::class.java)) {
            return PlaylistChannelsViewModel(
                channelRepository,
                settingsRepository,
                playlistRepository,
                playlistId,
                playlistTitle,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
