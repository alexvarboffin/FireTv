package tv.hdonlinetv.compose.recent

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import tv.hdonlinetv.compose.core.domain.model.ChannelUi

/** One "Recently watched" entry. VOD = has a known duration (> 0). */
data class RecentItem(
    val key: String,
    val channelId: Long,
    val link: String,
    val title: String,
    val cover: String?,
    val playlistId: Long,
    val lastWatchedAt: Long,
    val positionMs: Long,
    val durationMs: Long,
) {
    val isVod: Boolean get() = durationMs > 0
    val progress: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

/**
 * Watch history in SharedPreferences (JSON). Deliberately NOT in Room:
 * no schema change -> no migration needed for FavoriteDatabase.
 */
object RecentHistory {
    private const val PREFS = "recent_history"
    private const val KEY_ITEMS = "items"
    private const val MAX_ITEMS = 20
    private const val SAVE_EVERY_MS = 5_000L

    private var prefs: SharedPreferences? = null
    private val _items = MutableStateFlow<List<RecentItem>>(emptyList())
    val items: StateFlow<List<RecentItem>> = _items.asStateFlow()

    private var lastSaveAt = 0L
    private val resumeDone = HashSet<String>()

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = p
        _items.value = parse(p.getString(KEY_ITEMS, null))
    }

    fun keyOf(channel: ChannelUi): String =
        if (channel.id > 0) "c:${channel.id}" else "u:${channel.link.orEmpty()}"

    /** Playback start: move item to top, keep saved position. */
    fun onStart(channel: ChannelUi, playlistId: Long = 0L) {
        if (channel.link.isNullOrBlank() && channel.id <= 0) return
        val key = keyOf(channel)
        resumeDone.remove(key)
        val old = _items.value.firstOrNull { it.key == key }
        val item = RecentItem(
            key = key,
            channelId = channel.id,
            link = channel.link.orEmpty(),
            title = channel.name.orEmpty(),
            cover = channel.cover ?: old?.cover,
            playlistId = if (playlistId != 0L) playlistId else old?.playlistId ?: 0L,
            lastWatchedAt = System.currentTimeMillis(),
            positionMs = old?.positionMs ?: 0L,
            durationMs = old?.durationMs ?: 0L,
        )
        put(item)
    }

    /**
     * Progress tick from the player. Saves (throttled) and, once per start,
     * returns a position to seek to if there is a saved resume point.
     */
    fun onProgress(channel: ChannelUi, positionMs: Long, durationMs: Long, force: Boolean = false): Long? {
        val key = keyOf(channel)
        val old = _items.value.firstOrNull { it.key == key } ?: return null
        var seekTo: Long? = null
        if (durationMs > 0 && key !in resumeDone) {
            resumeDone.add(key)
            val saved = old.positionMs
            if (saved > 5_000L && saved < durationMs - 10_000L && positionMs < saved - 3_000L) {
                seekTo = saved
            }
        }
        if (seekTo != null) return seekTo
        if (durationMs <= 0 && !force) return null
        val now = System.currentTimeMillis()
        if (!force && now - lastSaveAt < SAVE_EVERY_MS) return null
        if (positionMs <= 0 && old.positionMs > 0 && !force) return null
        lastSaveAt = now
        // Finished -> restart next time.
        val pos = if (durationMs > 0 && positionMs >= durationMs - 10_000L) 0L else positionMs
        put(old.copy(positionMs = pos, durationMs = durationMs.coerceAtLeast(old.durationMs), lastWatchedAt = now), moveTop = false)
        return null
    }

    fun remove(key: String) {
        _items.value = _items.value.filterNot { it.key == key }
        persist()
    }

    private fun put(item: RecentItem, moveTop: Boolean = true) {
        val rest = _items.value.filterNot { it.key == item.key }
        _items.value = if (moveTop) {
            (listOf(item) + rest).take(MAX_ITEMS)
        } else {
            _items.value.map { if (it.key == item.key) item else it }
        }
        persist()
    }

    private fun persist() {
        val arr = JSONArray()
        _items.value.forEach { i ->
            arr.put(
                JSONObject()
                    .put("key", i.key).put("id", i.channelId).put("link", i.link)
                    .put("title", i.title).put("cover", i.cover ?: "")
                    .put("pl", i.playlistId).put("at", i.lastWatchedAt)
                    .put("pos", i.positionMs).put("dur", i.durationMs),
            )
        }
        prefs?.edit()?.putString(KEY_ITEMS, arr.toString())?.apply()
    }

    private fun parse(raw: String?): List<RecentItem> = runCatching {
        if (raw.isNullOrBlank()) return emptyList()
        val arr = JSONArray(raw)
        (0 until arr.length()).map { n ->
            val o = arr.getJSONObject(n)
            RecentItem(
                key = o.getString("key"),
                channelId = o.optLong("id"),
                link = o.optString("link"),
                title = o.optString("title"),
                cover = o.optString("cover").ifBlank { null },
                playlistId = o.optLong("pl"),
                lastWatchedAt = o.optLong("at"),
                positionMs = o.optLong("pos"),
                durationMs = o.optLong("dur"),
            )
        }
    }.getOrDefault(emptyList())
}
