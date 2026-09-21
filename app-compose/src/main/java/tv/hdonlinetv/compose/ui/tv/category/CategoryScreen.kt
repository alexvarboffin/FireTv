package tv.hdonlinetv.compose.ui.tv.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.CategoryUi
import tv.hdonlinetv.compose.core.presentation.category.CategoryViewModel
import tv.hdonlinetv.compose.core.presentation.category.CategoryViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalCategoryRepository
import tv.hdonlinetv.compose.tv.LocalTvDrawerFocusRequester
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.tv.components.CategoryCard

@Composable
fun CategoryScreen() {
    val repository = LocalCategoryRepository.current
    val viewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(repository),
    )
    val state by viewModel.uiState.collectAsState()
    val navController = LocalTvNavController.current
    CategoryScreenBody(
        categories = state.categories,
        isLoading = state.isLoading,
        onCategoryClick = { categoryName ->
            navController.navigate(Routes.Channels.build(categoryName))
        },
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategoryScreenBody(
    categories: List<CategoryUi>,
    isLoading: Boolean,
    onCategoryClick: (String) -> Unit,
) {
    val drawerFocus = LocalTvDrawerFocusRequester.current
    val columns = 4
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                Text(
                    text = stringResource(R.string.loading),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            categories.isEmpty() -> {
                Text(
                    text = stringResource(R.string.tab_category),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(categories, key = { _, c -> c.id }) { index, category ->
                        CategoryCard(
                            category = category,
                            index = index,
                            onClick = { onCategoryClick(category.name) },
                            modifier = if (drawerFocus != null && index % columns == 0) {
                                Modifier.focusProperties { left = drawerFocus }
                            } else {
                                Modifier
                            },
                        )
                    }
                }
            }
        }
    }
}
