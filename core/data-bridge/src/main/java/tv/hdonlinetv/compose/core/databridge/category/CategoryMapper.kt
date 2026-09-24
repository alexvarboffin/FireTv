package tv.hdonlinetv.compose.core.databridge.category

import com.walhalla.data.model.Category
import tv.hdonlinetv.compose.core.domain.model.CategoryUi

object CategoryMapper {

    fun toUi(category: Category, count: Int = 0): CategoryUi = CategoryUi(
        id = category._id.toString(),
        name = category.name.orEmpty(),
        thumb = category.thumb,
        desc = category.desc.orEmpty(),
        count = count,
    )

    fun toUiList(
        categories: List<Category>,
        countsByName: Map<String, Int> = emptyMap(),
    ): List<CategoryUi> =
        categories.map { category ->
            val name = category.name.orEmpty()
            val count = countsByName[name]
                ?: countsByName.entries.firstOrNull { (key, _) ->
                    key.equals(name, ignoreCase = true)
                }?.value
                ?: 0
            toUi(category, count)
        }

    fun toData(ui: CategoryUi): Category = Category(
        ui.name,
        ui.desc,
        ui.thumb,
    )
}
