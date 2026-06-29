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
import tv.hdonlinetv.compose.core.domain.repository.PlaylistRepository
import tv.hdonlinetv.compose.core.domain.repository.XtreamRepository
import tv.hdonlinetv.compose.core.domain.repository.XtreamStreamType
import tv.hdonlinetv.compose.core.presentation.error.UiError

data class XtreamBrowserUiState(
    val title: String = "",
    val apiUrl: String = "",
    val selectedTab: XtreamStreamType = XtreamStreamType.LIVE,
    val channels: List<ChannelUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiError? = null,
)

class XtreamBrowserViewModel(
    private val playlistRepository: PlaylistRepository,
    private val xtreamRepository: XtreamRepository,
    private val playlistId: Long,
) : ViewModel() {

    private val _uiState = MutableStateFlow(XtreamBrowserUiState(isLoading = true))
    val uiState: StateFlow<XtreamBrowserUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val playlist = playlistRepository.getPlaylistById(playlistId)
            if (playlist == null) {
                _uiState.update { it.copy(isLoading = false, error = UiError.Unknown) }
                return@launch
            }
            _uiState.update {
                it.copy(title = playlist.title, apiUrl = playlist.fileName, isLoading = false)
            }
            loadStreams(XtreamStreamType.LIVE)
        }
    }

    fun selectTab(type: XtreamStreamType) {
        if (_uiState.value.selectedTab == type) return
        _uiState.update { it.copy(selectedTab = type) }
        loadStreams(type)
    }

    private fun loadStreams(type: XtreamStreamType) {
        val apiUrl = _uiState.value.apiUrl
        if (apiUrl.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val channels = xtreamRepository.getStreams(apiUrl, type)
                _uiState.update { it.copy(isLoading = false, channels = channels) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, error = UiError.Unknown, channels = emptyList()) }
            }
        }
    }
}

class XtreamBrowserViewModelFactory(
    private val playlistRepository: PlaylistRepository,
    private val xtreamRepository: XtreamRepository,
    private val playlistId: Long,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(XtreamBrowserViewModel::class.java)) {
            return XtreamBrowserViewModel(playlistRepository, xtreamRepository, playlistId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
