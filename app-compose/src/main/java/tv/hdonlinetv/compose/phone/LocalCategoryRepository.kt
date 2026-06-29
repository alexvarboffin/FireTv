package tv.hdonlinetv.compose.phone

import androidx.compose.runtime.staticCompositionLocalOf
import tv.hdonlinetv.compose.core.domain.repository.CategoryRepository

val LocalCategoryRepository = staticCompositionLocalOf<CategoryRepository> {
    error("CategoryRepository not provided")
}
