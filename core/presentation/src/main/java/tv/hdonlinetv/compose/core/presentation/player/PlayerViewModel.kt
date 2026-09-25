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
import tv.hdonlinetv.compose.core.domain.model.PlaylistType
import tv.hdonlinetv.compose.core.domain.repository.ChannelRepository
import tv.hdonlinetv.compose.core.domain.repository.PlaylistRepository
import tv.hdonlinetv.compose.core.domain.repository.XtreamRepository
import tv.hdonlinetv.compose.core.domain.repository.XtreamStreamType
import tv.hdonlinetv.compose.core.presentation.error.UiError

/**
 * Wire form of [tv.hdonlinetv.compose.navigation.PlayerBrowseScope] for :core
 * (presentation must not depend on app-compose navigation).
 */
sealed class PlayerBrowseScopeWire {
    data object Favorites : PlayerBrowseScopeWire()
    data class Playlist(val playlistId: Long) : PlayerBrowseScopeWire()
    data class Xtream(val playlistId: Long, val streamTypeName: String) : PlayerBrowseScopeWire()
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
    private val playlistRepository: PlaylistRepository? = null,
    private val xtreamRepository: XtreamRepository? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState(isLoading = true))
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        if (!directStreamUrl.isNullOrBlank()) {
            val synthetic = ChannelUi(
                id = 0,
                name = directStreamTitle.orEmpty(),
                cover = null,
                category = null,
                link = directStreamUrl,
                desc = null,
            )
            _uiState.update {
                it.copy(isLoading = false, channel = synthetic, siblings = emptyList())
            }
            viewModelScope.launch {
                val siblings = runCatching { loadSiblings(synthetic) }.getOrDefault(emptyList())
                val current = siblings.zapMatch(synthetic) ?: synthetic
                _uiState.update { it.copy(channel = current, siblings = siblings) }
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
            is PlayerBrowseScopeWire.Playlist -> {
                val db = repository.getChannelsInPlaylist(scope.playlistId)
                if (db.isNotEmpty()) db else loadXtreamSiblings(scope.playlistId, inferXtreamType(channel))
            }
            is PlayerBrowseScopeWire.Xtream ->
                loadXtreamSiblings(scope.playlistId, parseXtreamType(scope.streamTypeName))
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

    private suspend fun loadXtreamSiblings(
        playlistId: Long,
        streamType: XtreamStreamType,
    ): List<ChannelUi> {
        val playlist = playlistRepository?.getPlaylistById(playlistId) ?: return emptyList()
        if (playlist.type != PlaylistType.XTREAM_URL) return emptyList()
        val api = playlist.fileName
        if (api.isBlank()) return emptyList()
        return xtreamRepository?.getStreams(api, streamType).orEmpty()
    }

    fun selectChannel(channel: ChannelUi) {
        val current = _uiState.value.channel
        if (current != null && current.sameZap(channel)) return
        if (channel.link.isNullOrBlank() && channel.id <= 0) return
        _uiState.update { it.copy(channel = channel) }
    }

    fun nextChannel() {
        val state = _uiState.value
        val list = state.siblings
        if (list.isEmpty()) return
        val idx = list.indexOfZap(state.channel)
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
        val idx = list.indexOfZap(state.channel)
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
    private val playlistRepository: PlaylistRepository? = null,
    private val xtreamRepository: XtreamRepository? = null,
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
                playlistRepository,
                xtreamRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

private fun parseXtreamType(name: String): XtreamStreamType =
    runCatching { XtreamStreamType.valueOf(name) }.getOrDefault(XtreamStreamType.LIVE)

private fun inferXtreamType(channel: ChannelUi?): XtreamStreamType {
    val link = channel?.link.orEmpty()
    return when {
        "/movie/" in link -> XtreamStreamType.VOD
        "/series/" in link -> XtreamStreamType.SERIES
        else -> XtreamStreamType.LIVE
    }
}

private fun ChannelUi.sameZap(other: ChannelUi): Boolean =
    if (id > 0 && other.id > 0) id == other.id
    else !link.isNullOrBlank() && link == other.link

private fun List<ChannelUi>.indexOfZap(current: ChannelUi?): Int {
    if (current == null) return -1
    if (current.id > 0) {
        val byId = indexOfFirst { it.id == current.id }
        if (byId >= 0) return byId
    }
    val link = current.link
    if (!link.isNullOrBlank()) return indexOfFirst { it.link == link }
    return -1
}

private fun List<ChannelUi>.zapMatch(current: ChannelUi): ChannelUi? {
    val i = indexOfZap(current)
    return if (i >= 0) this[i] else null
}
