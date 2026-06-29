package tv.hdonlinetv.compose.mockScreens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import tv.hdonlinetv.compose.core.domain.model.CategoryUi
import tv.hdonlinetv.compose.ui.mobile.category.CategoryScreenBody
import tv.hdonlinetv.compose.ui.mobile.theme.PhoneTheme

@Composable
fun CategoryScreen() {
    val mockCategories = listOf(
        CategoryUi(id = "1", name = "News", thumb = null),
        CategoryUi(id = "2", name = "Sports", thumb = null),
        CategoryUi(id = "3", name = "Movies", thumb = null),
        CategoryUi(id = "4", name = "Kids", thumb = null),
    )
    CategoryScreenBody(
        categories = mockCategories,
        isLoading = false,
        onCategoryClick = {},
    )
}

@Preview(showBackground = true)
@Composable
fun CategoryScreenPreview() {
    PhoneTheme {
        CategoryScreen()
    }
}
