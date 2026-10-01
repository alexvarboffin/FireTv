package tv.hdonlinetv.compose.core.epg.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "epg_source",
    indices = [Index(value = ["url"], unique = true)],
)
data class EpgSourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val lastSyncAt: Long = 0,
    val lastStatus: String? = null,
    val programmeCount: Int = 0,
    /** Latest programme stop stored from this source; the cache is usable until then. */
    val coverageUntil: Long = 0,
    /** Hash of the channel keys the last sync kept; a different set forces a re-download. */
    val wantedHash: Int = 0,
)

@Entity(
    tableName = "programme",
    foreignKeys = [
        ForeignKey(
            entity = EpgSourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["channelKey", "stop"]),
        Index(value = ["sourceId"]),
    ],
)
data class ProgrammeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceId: Long,
    /** [tv.hdonlinetv.compose.core.epg.EpgKey.normalize]d channel id. */
    val channelKey: String,
    val start: Long,
    val stop: Long,
    val title: String,
    val desc: String?,
)

/**
 * Playlist channel → guide channel and the guide file it is read from.
 * [appKey] is `i:<EpgKey.normalize(tvgId)>` or `n:<EpgKey.nameKey(name)>`.
 */
@Entity(tableName = "channel_map")
data class ChannelMapEntity(
    @PrimaryKey val appKey: String,
    /** [tv.hdonlinetv.compose.core.epg.EpgKey.normalize]d guide channel id, matches [ProgrammeEntity.channelKey]. */
    val guideKey: String,
    val icon: String?,
    /** [EpgSourceEntity.id] holding this channel; null = any source with [guideKey]. */
    val sourceId: Long? = null,
    val origin: String = ORIGIN_INDEX,
) {
    companion object {
        const val ORIGIN_INDEX = "index"
        /** From the playlist's own `url-tvg`: never looked up in the epg-index. */
        const val ORIGIN_PLAYLIST = "playlist"
    }
}

/** `url-tvg` of an imported playlist and the `tvg-id`s it is expected to cover. */
@Entity(
    tableName = "guide_binding",
    primaryKeys = ["playlistId", "url", "channelKey"],
    indices = [Index(value = ["url"])],
)
data class GuideBindingEntity(
    val playlistId: Long,
    val url: String,
    /** [tv.hdonlinetv.compose.core.epg.EpgKey.normalize]d `tvg-id`. */
    val channelKey: String,
)
