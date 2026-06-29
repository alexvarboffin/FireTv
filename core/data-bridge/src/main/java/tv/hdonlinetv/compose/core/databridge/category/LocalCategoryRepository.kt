package tv.hdonlinetv.compose.core.databridge.category

import android.content.Context
import com.walhalla.data.repository.LocalDatabaseRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import tv.hdonlinetv.compose.core.domain.model.CategoryUi
import tv.hdonlinetv.compose.core.domain.repository.CategoryRepository

class LocalCategoryRepository(
    context: Context,
) : CategoryRepository {

    private val database = LocalDatabaseRepo.getStoreInfoDatabase(context.applicationContext)

    override suspend fun getAllCategories(): List<CategoryUi> = withContext(Dispatchers.IO) {
        CategoryMapper.toUiList(database.allCategories)
    }

    override fun observeAllCategories(): Flow<List<CategoryUi>> =
        database.observeAllCategories()
            .map { CategoryMapper.toUiList(it) }
            .flowOn(Dispatchers.IO)
}
