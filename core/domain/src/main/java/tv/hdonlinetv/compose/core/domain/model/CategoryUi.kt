package tv.hdonlinetv.compose.core.domain.model

data class CategoryUi(
    val id: String,
    val name: String,
    val thumb: String?,
    val desc: String = "",
)
