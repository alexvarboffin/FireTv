package tv.hdonlinetv.compose.core.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.hdonlinetv.compose.core.domain.model.CategoryUi
import tv.hdonlinetv.compose.core.domain.repository.CategoryRepository
import tv.hdonlinetv.compose.core.presentation.error.UiError

data class CategoryUiState(
    val categories: List<CategoryUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiError? = null,
)

class CategoryViewModel(
    private val repository: CategoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState(isLoading = true))
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAllCategories()
                .catch {
                    _uiState.update { state -> state.copy(isLoading = false, error = UiError.Unknown) }
                }
                .collect { categories ->
                    _uiState.update {
                        it.copy(isLoading = false, categories = categories, error = null)
                    }
                }
        }
    }

    fun onCategoryClick(categoryId: String) {
        // navigation — handled via callback from Screen when wired to NavHost
    }
}

class CategoryViewModelFactory(
    private val repository: CategoryRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            return CategoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
