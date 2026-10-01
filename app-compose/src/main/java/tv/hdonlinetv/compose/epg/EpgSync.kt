package tv.hdonlinetv.compose.epg

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.epg.EpgStore
import tv.hdonlinetv.compose.core.epg.EpgSyncResult
import tv.hdonlinetv.compose.core.epg.db.EpgDatabase
import tv.hdonlinetv.compose.core.epg.index.EpgChannelRef

/** Wait for the channel list to settle (playlist import emits many times). */
const val EPG_SYNC_SETTLE_MS = 5_000L

/**
 * Background guide sync for the channels in the database (epg-index lookup → XMLTV).
 * Runs in an app-wide scope so leaving a screen does not cancel it; at most once per
 * [MIN_INTERVAL_MS] for the same channel set.
 */
object EpgSync {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null
    /** Latest request that arrived while [job] was running; replayed when it finishes. */
    private var pending: List<ChannelUi>? = null

    @Synchronized
    fun request(context: Context, channels: List<ChannelUi>) {
        if (job?.isActive == true) {
            pending = channels
            return
        }
        val refs = channels
            .map { EpgChannelRef(it.tvgId?.trim()?.ifEmpty { null }, it.name) }
            .filter { it.tvgId != null || !it.name.isNullOrBlank() }
            .distinct()
        if (refs.isEmpty()) return
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val store = EpgStore.get(context)
        job = scope.launch {
            val hash = (refs.hashCode() * 31 + store.bindingsHash()) * 31 + EpgDatabase.VERSION
            val now = System.currentTimeMillis()
            if (prefs.getInt(KEY_HASH, 0) == hash && now - prefs.getLong(KEY_AT, 0) < MIN_INTERVAL_MS) {
                return@launch
            }
            runCatching { store.syncAll(refs) }
                .onSuccess { r ->
                    val results = r.playlistGuides.values + r.index.files.values
                    val partial = results.any { it is EpgSyncResult.Failed }
                    val syncedAt = if (partial) now - MIN_INTERVAL_MS + RETRY_PARTIAL_MS else now
                    prefs.edit().putInt(KEY_HASH, hash).putLong(KEY_AT, syncedAt).apply()
                    Log.i(
                        TAG,
                        "channels=${r.index.channels + r.matchedByPlaylist} playlist=${r.matchedByPlaylist} " +
                            "id=${r.index.matchedById} name=${r.index.matchedByName} " +
                            "guides=${r.playlistGuides} files=${r.index.files}",
                    )
                }
                .onFailure { Log.w(TAG, "sync failed", it) }
        }.also { started ->
            started.invokeOnCompletion { replayPending(context, started) }
        }
    }

    @Synchronized
    private fun replayPending(context: Context, finished: Job) {
        if (job !== finished) return
        job = null
        val next = pending ?: return
        pending = null
        request(context, next)
    }

    private const val TAG = "EpgSync"
    private const val PREFS = "epg_sync"
    private const val KEY_HASH = "channels_hash"
    private const val KEY_AT = "synced_at"
    private const val MIN_INTERVAL_MS = 12 * 60 * 60_000L
    private const val RETRY_PARTIAL_MS = 60 * 60_000L
}
