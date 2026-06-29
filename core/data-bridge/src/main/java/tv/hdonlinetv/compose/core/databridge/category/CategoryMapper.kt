package tv.hdonlinetv.compose.core.databridge.category

import com.walhalla.data.model.Category
import tv.hdonlinetv.compose.core.domain.model.CategoryUi

object CategoryMapper {

    fun toUi(category: Category): CategoryUi = CategoryUi(
        id = category._id.toString(),
        name = category.name.orEmpty(),
        thumb = category.thumb,
        desc = category.desc.orEmpty(),
    )

    fun toUiList(categories: List<Category>): List<CategoryUi> =
        categories.map(::toUi)

    fun toData(ui: CategoryUi): Category = Category(
        ui.name,
        ui.desc,
        ui.thumb,
    )
}
