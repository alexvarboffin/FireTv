package tv.hdonlinetv.compose.core.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.domain.repository.ChannelRepository
import tv.hdonlinetv.compose.core.presentation.error.UiError

data class PlayerUiState(
    val channel: ChannelUi? = null,
    val isLoading: Boolean = false,
    val error: UiError? = null,
)

class PlayerViewModel(
    private val repository: ChannelRepository,
    private val channelId: Long,
    private val directStreamUrl: String? = null,
    private val directStreamTitle: String? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState(isLoading = true))
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        if (!directStreamUrl.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    channel = ChannelUi(
                        id = 0,
                        name = directStreamTitle.orEmpty(),
                        cover = null,
                        category = null,
                        link = directStreamUrl,
                        desc = null,
                    ),
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val channel = repository.getChannelById(channelId)
                _uiState.update { it.copy(isLoading = false, channel = channel) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, error = UiError.Unknown) }
            }
        }
    }

    fun toggleFavorite() {
        val channel = _uiState.value.channel ?: return
        if (channel.id <= 0) return
        viewModelScope.launch {
            try {
                val newFavorite = !channel.isFavorite
                repository.setFavorite(channel.id, newFavorite)
                _uiState.update { it.copy(channel = channel.copy(isFavorite = newFavorite)) }
            } catch (_: Exception) {
                _uiState.update { it.copy(error = UiError.Unknown) }
            }
        }
    }
}

class PlayerViewModelFactory(
    private val repository: ChannelRepository,
    private val channelId: Long,
    private val directStreamUrl: String? = null,
    private val directStreamTitle: String? = null,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            return PlayerViewModel(
                repository,
                channelId,
                directStreamUrl,
                directStreamTitle,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
