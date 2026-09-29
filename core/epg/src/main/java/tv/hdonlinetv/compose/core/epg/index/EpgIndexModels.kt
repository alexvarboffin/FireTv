package tv.hdonlinetv.compose.core.epg.index

/** Playlist channel as seen by the guide lookup: M3U `tvg-id` / Xtream `epg_channel_id` + display name. */
data class EpgChannelRef(
    val tvgId: String?,
    val name: String?,
)

enum class EpgMatchLevel { ID, NAME }

data class EpgGuideMatch(
    val level: EpgMatchLevel,
    /** Channel id exactly as in the XMLTV `<programme channel>`. */
    val guideChannelId: String,
    /** XMLTV files that carry this channel, best first. */
    val sourceUrls: List<String>,
    /** Use only when the playlist has no `tvg-logo`. */
    val icon: String?,
)

data class EpgIndexResolution(
    /** Index in the input list → match; unmatched channels are absent. */
    val matches: Map<Int, EpgGuideMatch>,
    val requests: List<String>,
    /** `ids+names` lookup strategy, e.g. `shards+full`, `full+none`. */
    val mode: String,
)

/** `index/api.json`. */
internal data class EpgIndexApi(
    val shards: Int,
    val idsFull: String,
    val idsShard: String,
    val namesFull: String,
    val namesShard: String,
    val idsFullGzip: Long,
    val idsShardGzipAvg: Long,
    val namesFullGzip: Long,
    val namesShardGzipAvg: Long,
    val requestCostBytes: Long,
    val sources: List<String>,
)
