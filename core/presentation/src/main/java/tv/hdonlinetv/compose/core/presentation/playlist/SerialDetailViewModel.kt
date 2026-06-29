package tv.hdonlinetv.compose.core.presentation.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.hdonlinetv.compose.core.domain.model.SeriesDetailUi
import tv.hdonlinetv.compose.core.domain.model.SeriesEpisodeUi
import tv.hdonlinetv.compose.core.domain.repository.PlaylistRepository
import tv.hdonlinetv.compose.core.domain.repository.XtreamRepository
import tv.hdonlinetv.compose.core.presentation.error.UiError

data class SerialDetailUiState(
    val title: String = "",
    val apiUrl: String = "",
    val detail: SeriesDetailUi? = null,
    val selectedSeason: Int? = null,
    val isLoading: Boolean = false,
    val error: UiError? = null,
)

class SerialDetailViewModel(
    private val playlistRepository: PlaylistRepository,
    private val xtreamRepository: XtreamRepository,
    private val playlistId: Long,
    private val seriesId: Int,
    private val seriesTitle: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SerialDetailUiState(title = seriesTitle, isLoading = true),
    )
    val uiState: StateFlow<SerialDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val playlist = playlistRepository.getPlaylistById(playlistId)
            if (playlist == null) {
                _uiState.update { it.copy(isLoading = false, error = UiError.Unknown) }
                return@launch
            }
            val apiUrl = playlist.fileName
            _uiState.update { it.copy(apiUrl = apiUrl) }
            loadSeriesInfo(apiUrl)
        }
    }

    fun selectSeason(seasonNumber: Int) {
        _uiState.update { it.copy(selectedSeason = seasonNumber) }
    }

    fun buildEpisodeUrl(episode: SeriesEpisodeUi): String? {
        val apiUrl = _uiState.value.apiUrl
        if (apiUrl.isBlank() || episode.id.isBlank()) return null
        val extension = episode.containerExtension?.takeIf { it.isNotBlank() } ?: "mp4"
        return xtreamRepository.buildEpisodeStreamUrl(apiUrl, episode.id, extension)
    }

    private suspend fun loadSeriesInfo(apiUrl: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            val detail = xtreamRepository.getSeriesInfo(apiUrl, seriesId)
            val title = detail?.info?.name?.takeIf { it.isNotBlank() } ?: seriesTitle
            val firstSeason = detail?.seasons?.firstOrNull()?.seasonNumber
                ?: detail?.episodesBySeason?.keys?.minOrNull()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    detail = detail,
                    title = title,
                    selectedSeason = firstSeason,
                    error = if (detail == null) UiError.Unknown else null,
                )
            }
        } catch (_: Exception) {
            _uiState.update { it.copy(isLoading = false, error = UiError.Unknown) }
        }
    }
}

class SerialDetailViewModelFactory(
    private val playlistRepository: PlaylistRepository,
    private val xtreamRepository: XtreamRepository,
    private val playlistId: Long,
    private val seriesId: Int,
    private val seriesTitle: String,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SerialDetailViewModel::class.java)) {
            return SerialDetailViewModel(
                playlistRepository,
                xtreamRepository,
                playlistId,
                seriesId,
                seriesTitle,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
