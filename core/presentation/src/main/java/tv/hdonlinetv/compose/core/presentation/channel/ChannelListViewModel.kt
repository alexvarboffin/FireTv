package tv.hdonlinetv.compose.core.presentation.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.domain.repository.ChannelRepository
import tv.hdonlinetv.compose.core.domain.repository.SettingsRepository
import tv.hdonlinetv.compose.core.presentation.error.UiError

data class ChannelListUiState(
    val channels: List<ChannelUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiError? = null,
    val title: String = "",
)

class ChannelListViewModel(
    private val repository: ChannelRepository,
    private val settingsRepository: SettingsRepository,
    private val categoryName: String?,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChannelListUiState(isLoading = true, title = categoryName.orEmpty()))
    val uiState: StateFlow<ChannelListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val sort = settingsRepository.getSettings().sortOption
            repository.observeAllChannels(sort)
                .map { channels ->
                    if (categoryName.isNullOrBlank()) {
                        channels
                    } else {
                        channels.filter { channel ->
                            channel.category?.contains(categoryName, ignoreCase = true) == true
                        }
                    }
                }
                .catch {
                    _uiState.update { state -> state.copy(isLoading = false, error = UiError.Unknown) }
                }
                .collect { channels ->
                    _uiState.update {
                        it.copy(isLoading = false, channels = channels, error = null)
                    }
                }
        }
    }
}

class ChannelListViewModelFactory(
    private val repository: ChannelRepository,
    private val settingsRepository: SettingsRepository,
    private val categoryName: String?,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChannelListViewModel::class.java)) {
            return ChannelListViewModel(repository, settingsRepository, categoryName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class FavoritesViewModel(
    private val repository: ChannelRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChannelListUiState(isLoading = true))
    val uiState: StateFlow<ChannelListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeFavorites()
                .catch {
                    _uiState.update { state -> state.copy(isLoading = false, error = UiError.Unknown) }
                }
                .collect { channels ->
                    _uiState.update {
                        it.copy(isLoading = false, channels = channels, error = null)
                    }
                }
        }
    }
}

class FavoritesViewModelFactory(
    private val repository: ChannelRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            return FavoritesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
