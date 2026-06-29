package tv.hdonlinetv.compose.core.domain.model

data class ChannelUi(
    val id: Long,
    val name: String,
    val cover: String?,
    val category: String?,
    val link: String?,
    val desc: String?,
    val isFavorite: Boolean = false,
    val extUserAgent: String? = null,
    val extReferer: String? = null,
    val ua: String? = null,
)
