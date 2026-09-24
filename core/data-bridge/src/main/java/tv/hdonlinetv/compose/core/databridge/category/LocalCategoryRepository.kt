package tv.hdonlinetv.compose.core.databridge.category

import android.content.Context
import com.walhalla.data.repository.LocalDatabaseRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import tv.hdonlinetv.compose.core.domain.model.CategoryUi
import tv.hdonlinetv.compose.core.domain.repository.CategoryRepository

class LocalCategoryRepository(
    context: Context,
) : CategoryRepository {

    private val database = LocalDatabaseRepo.getStoreInfoDatabase(context.applicationContext)

    override suspend fun getAllCategories(): List<CategoryUi> = withContext(Dispatchers.IO) {
        CategoryMapper.toUiList(
            database.allCategories,
            database.getChannelCountsByCategory(),
        )
    }

    override fun observeAllCategories(): Flow<List<CategoryUi>> =
        combine(
            database.observeAllCategories(),
            database.observeChannelCountsByCategory(),
        ) { categories, counts ->
            CategoryMapper.toUiList(categories, counts)
        }.flowOn(Dispatchers.IO)

    override suspend fun deleteEmptyCategories(): Int = withContext(Dispatchers.IO) {
        database.deleteEmptyCategories()
    }
}
