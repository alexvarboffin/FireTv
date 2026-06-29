package tv.hdonlinetv.compose.core.domain.repository

import kotlinx.coroutines.flow.Flow
import tv.hdonlinetv.compose.core.domain.model.CategoryUi

interface CategoryRepository {
    suspend fun getAllCategories(): List<CategoryUi>
    fun observeAllCategories(): Flow<List<CategoryUi>>
}
