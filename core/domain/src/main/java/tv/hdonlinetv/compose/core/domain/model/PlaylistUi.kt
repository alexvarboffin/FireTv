package tv.hdonlinetv.compose.core.domain.model

data class PlaylistUi(
    val id: Long,
    val title: String,
    val fileName: String,
    val count: Int,
    val type: Int,
    val autoUpdate: Boolean,
    val importDate: Long = 0,
    val updateDate: Long = 0,
)

object PlaylistType {
    const val M3U_CLOUD = 0
    const val M3U_LOCAL = 1
    const val M3U_BUFFER = 2
    const val XTREAM_URL = 3
}
