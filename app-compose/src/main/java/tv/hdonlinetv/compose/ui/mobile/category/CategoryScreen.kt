package tv.hdonlinetv.compose.ui.mobile.category

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.CategoryUi
import tv.hdonlinetv.compose.core.presentation.category.CategoryViewModel
import tv.hdonlinetv.compose.core.presentation.category.CategoryViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalCategoryRepository
import tv.hdonlinetv.compose.phone.LocalPhoneNavController
import tv.hdonlinetv.compose.ui.mobile.components.CategoryCard
import tv.hdonlinetv.compose.ui.mobile.components.LegacyEmptyState

/** Material medium width; also covers most phones in landscape. */
private const val TABLET_MIN_WIDTH_DP = 600

@Composable
fun CategoryScreen() {
    val repository = LocalCategoryRepository.current
    val viewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(repository),
    )
    val state by viewModel.uiState.collectAsState()
    val navController = LocalPhoneNavController.current
    CategoryScreenBody(
        categories = state.categories,
        isLoading = state.isLoading,
        onCategoryClick = { categoryName ->
            navController.navigate(Routes.Channels.build(categoryName))
        },
    )
}

@Composable
fun CategoryScreenBody(
    categories: List<CategoryUi>,
    isLoading: Boolean,
    onCategoryClick: (String) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val wide = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE ||
        configuration.screenWidthDp >= TABLET_MIN_WIDTH_DP
    val columns = if (wide) 3 else 2

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.bgMain)),
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            categories.isEmpty() -> {
                LegacyEmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                )
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(0.dp),
                ) {
                    itemsIndexed(categories, key = { _, c -> c.id }) { index, category ->
                        CategoryCard(
                            category = category,
                            index = index,
                            onClick = { onCategoryClick(category.name) },
                        )
                    }
                }
            }
        }
    }
}
