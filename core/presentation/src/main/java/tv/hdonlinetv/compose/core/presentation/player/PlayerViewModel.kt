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

/**
 * Wire form of [tv.hdonlinetv.compose.navigation.PlayerBrowseScope] for :core
 * (presentation must not depend on app-compose navigation).
 */
sealed class PlayerBrowseScopeWire {
    data object Favorites : PlayerBrowseScopeWire()
    data class Playlist(val playlistId: Long) : PlayerBrowseScopeWire()
    data class Category(val name: String) : PlayerBrowseScopeWire()
    data object All : PlayerBrowseScopeWire()
    data object None : PlayerBrowseScopeWire()
    data object InferPlaylist : PlayerBrowseScopeWire()
}

data class PlayerUiState(
    val channel: ChannelUi? = null,
    /** Flat zap order within the active browse scope. */
    val siblings: List<ChannelUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiError? = null,
)

class PlayerViewModel(
    private val repository: ChannelRepository,
    private val channelId: Long,
    private val browseScope: PlayerBrowseScopeWire = PlayerBrowseScopeWire.InferPlaylist,
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
                    siblings = emptyList(),
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val channel = repository.getChannelById(channelId)
                val siblings = loadSiblings(channel)
                _uiState.update {
                    it.copy(isLoading = false, channel = channel, siblings = siblings)
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, error = UiError.Unknown) }
            }
        }
    }

    private suspend fun loadSiblings(channel: ChannelUi?): List<ChannelUi> {
        return when (val scope = browseScope) {
            PlayerBrowseScopeWire.Favorites -> repository.getFavorites()
            is PlayerBrowseScopeWire.Playlist ->
                repository.getChannelsInPlaylist(scope.playlistId)
            is PlayerBrowseScopeWire.Category ->
                repository.getChannelsInCategory(scope.name)
            PlayerBrowseScopeWire.All -> repository.getAllChannels()
            PlayerBrowseScopeWire.None -> emptyList()
            PlayerBrowseScopeWire.InferPlaylist -> {
                val id = channel?.id?.takeIf { it > 0 } ?: channelId
                if (id > 0) repository.getSiblingChannels(id) else emptyList()
            }
        }
    }

    fun selectChannel(channel: ChannelUi) {
        if (channel.id <= 0) return
        if (_uiState.value.channel?.id == channel.id) return
        _uiState.update { it.copy(channel = channel) }
    }

    fun nextChannel() {
        val state = _uiState.value
        val list = state.siblings
        if (list.isEmpty()) return
        val idx = list.indexOfFirst { it.id == state.channel?.id }
        val next = when {
            idx < 0 -> list.first()
            idx >= list.lastIndex -> list.first()
            else -> list[idx + 1]
        }
        selectChannel(next)
    }

    fun previousChannel() {
        val state = _uiState.value
        val list = state.siblings
        if (list.isEmpty()) return
        val idx = list.indexOfFirst { it.id == state.channel?.id }
        val prev = when {
            idx < 0 -> list.last()
            idx == 0 -> list.last()
            else -> list[idx - 1]
        }
        selectChannel(prev)
    }

    fun toggleFavorite() {
        val channel = _uiState.value.channel ?: return
        if (channel.id <= 0) return
        viewModelScope.launch {
            try {
                val newFavorite = !channel.isFavorite
                repository.setFavorite(channel.id, newFavorite)
                val updated = channel.copy(isFavorite = newFavorite)
                _uiState.update { state ->
                    val siblings = when (browseScope) {
                        PlayerBrowseScopeWire.Favorites ->
                            if (newFavorite) {
                                if (state.siblings.any { it.id == updated.id }) {
                                    state.siblings.map { if (it.id == updated.id) updated else it }
                                } else {
                                    state.siblings + updated
                                }
                            } else {
                                state.siblings.filterNot { it.id == updated.id }
                            }
                        else -> state.siblings.map {
                            if (it.id == updated.id) updated else it
                        }
                    }
                    state.copy(channel = updated, siblings = siblings)
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(error = UiError.Unknown) }
            }
        }
    }
}

class PlayerViewModelFactory(
    private val repository: ChannelRepository,
    private val channelId: Long,
    private val browseScope: PlayerBrowseScopeWire = PlayerBrowseScopeWire.InferPlaylist,
    private val directStreamUrl: String? = null,
    private val directStreamTitle: String? = null,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            return PlayerViewModel(
                repository,
                channelId,
                browseScope,
                directStreamUrl,
                directStreamTitle,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
