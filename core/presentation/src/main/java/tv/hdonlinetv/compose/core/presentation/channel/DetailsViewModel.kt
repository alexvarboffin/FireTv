package tv.hdonlinetv.compose.core.presentation.channel

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

data class DetailsUiState(
    val channel: ChannelUi? = null,
    val isLoading: Boolean = false,
    val error: UiError? = null,
)

class DetailsViewModel(
    private val repository: ChannelRepository,
    private val channelId: Long,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState(isLoading = true))
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
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
        viewModelScope.launch {
            try {
                val newFavorite = !channel.isFavorite
                repository.setFavorite(channel.id, newFavorite)
                _uiState.update {
                    it.copy(channel = channel.copy(isFavorite = newFavorite))
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(error = UiError.Unknown) }
            }
        }
    }
}

class DetailsViewModelFactory(
    private val repository: ChannelRepository,
    private val channelId: Long,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
            return DetailsViewModel(repository, channelId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

data class SearchUiState(
    val query: String = "",
    val results: List<ChannelUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiError? = null,
)

class SearchViewModel(
    private val repository: ChannelRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        if (query.length < 2) {
            _uiState.update { it.copy(results = emptyList(), isLoading = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val results = repository.search(query)
                _uiState.update { it.copy(isLoading = false, results = results) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, error = UiError.Unknown) }
            }
        }
    }
}

class SearchViewModelFactory(
    private val repository: ChannelRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
