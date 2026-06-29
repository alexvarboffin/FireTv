package tv.hdonlinetv.compose.core.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import tv.hdonlinetv.compose.core.domain.model.AppSettingsUi
import tv.hdonlinetv.compose.core.domain.repository.SettingsRepository

class SettingsViewModel(
    private val repository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(repository.getSettings())
    val uiState: StateFlow<AppSettingsUi> = _uiState.asStateFlow()

    fun setGridColumns(columns: Int) {
        repository.setGridColumns(columns)
        _uiState.update { it.copy(gridColumns = columns) }
    }

    fun setSortOption(option: Int) {
        repository.setSortOption(option)
        _uiState.update { it.copy(sortOption = option) }
    }

    fun setDetailsMode(detailsMode: Boolean) {
        repository.setDetailsMode(detailsMode)
        _uiState.update { it.copy(detailsMode = detailsMode) }
    }

    fun setNightMode(enabled: Boolean) {
        repository.setNightMode(enabled)
        _uiState.update { it.copy(nightMode = enabled) }
    }

    fun setMediaPlayerOption(option: Int) {
        repository.setMediaPlayerOption(option)
        _uiState.update { it.copy(mediaPlayerOption = option) }
    }
}

class SettingsViewModelFactory(
    private val repository: SettingsRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
