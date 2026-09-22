package tv.hdonlinetv.compose.navigation

import android.net.Uri

/**
 * Browse / zap context for the player: the list the user came from.
 * Sheet + CH± operate only inside this scope (not “guess from channelId”).
 *
 * See Documentation/ARCHITECTURE/player-browse-scope.md
 */
sealed class PlayerBrowseScope {
    /** Favorites grid / list. */
    data object Favorites : PlayerBrowseScope()

    /** Channels of a single M3U/Xtream playlist. */
    data class Playlist(val playlistId: Long) : PlayerBrowseScope()

    /** Category browser. */
    data class Category(val name: String) : PlayerBrowseScope()

    /**
     * Entire library — usually too large for the in-player sheet;
     * still supported if an entry point opts in.
     */
    data object All : PlayerBrowseScope()

    /** Single stream / search hit / unknown — no siblings, no sheet zap. */
    data object None : PlayerBrowseScope()

    /**
     * Legacy fallback: resolve playlist (or category) from [channelId]
     * via join table — used when caller did not pass a scope.
     */
    data object InferPlaylist : PlayerBrowseScope()

    fun typeWire(): String = when (this) {
        Favorites -> TYPE_FAVORITES
        is Playlist -> TYPE_PLAYLIST
        is Category -> TYPE_CATEGORY
        All -> TYPE_ALL
        None -> TYPE_NONE
        InferPlaylist -> TYPE_INFER
    }

    fun keyWire(): String = when (this) {
        is Playlist -> playlistId.toString()
        is Category -> name
        else -> ""
    }

    companion object {
        const val TYPE_FAVORITES = "favorites"
        const val TYPE_PLAYLIST = "playlist"
        const val TYPE_CATEGORY = "category"
        const val TYPE_ALL = "all"
        const val TYPE_NONE = "none"
        const val TYPE_INFER = "infer"

        fun parse(type: String?, key: String?): PlayerBrowseScope {
            val t = type?.trim().orEmpty().ifEmpty { TYPE_INFER }
            val k = key?.trim().orEmpty()
            return when (t) {
                TYPE_FAVORITES -> Favorites
                TYPE_PLAYLIST -> {
                    val id = k.toLongOrNull()
                    if (id != null && id > 0) Playlist(id) else InferPlaylist
                }
                TYPE_CATEGORY -> {
                    if (k.isNotEmpty()) Category(Uri.decode(k)) else InferPlaylist
                }
                TYPE_ALL -> All
                TYPE_NONE -> None
                else -> InferPlaylist
            }
        }
    }
}
