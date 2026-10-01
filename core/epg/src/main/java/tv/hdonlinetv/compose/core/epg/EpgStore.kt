package tv.hdonlinetv.compose.core.epg

import android.content.Context
import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import tv.hdonlinetv.compose.core.epg.db.ChannelMapEntity
import tv.hdonlinetv.compose.core.epg.db.EpgDao
import tv.hdonlinetv.compose.core.epg.db.EpgDatabase
import tv.hdonlinetv.compose.core.epg.db.EpgSourceEntity
import tv.hdonlinetv.compose.core.epg.db.GuideBindingEntity
import tv.hdonlinetv.compose.core.epg.db.ProgrammeEntity
import tv.hdonlinetv.compose.core.epg.index.EpgChannelRef
import tv.hdonlinetv.compose.core.epg.index.EpgIndexClient
import tv.hdonlinetv.compose.core.epg.index.EpgMatchLevel
import tv.hdonlinetv.compose.core.epg.index.EpgSourcePlan
import tv.hdonlinetv.compose.core.epg.xmltv.XmlTvParser
import tv.hdonlinetv.compose.core.epg.xmltv.XmlTvStreams
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Guide download + local cache. Keys are raw `tvg-id` / `epg_channel_id` values;
 * matching goes through [EpgKey.normalize].
 */
class EpgStore private constructor(context: Context) {

    private val _version = MutableStateFlow(0L)

    /** Bumped after every sync that stored programmes — re-query now/next on change. */
    val version: StateFlow<Long> = _version.asStateFlow()

    private val appContext = context.applicationContext
    private val database = EpgDatabase.get(appContext)
    private val dao: EpgDao = database.dao()
    private val parser = XmlTvParser { Xml.newPullParser() }
    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Downloads [url] and replaces its programmes. [tvgIds] limits stored channels (null = all).
     * Returns [EpgSyncResult.Cached] without downloading while the stored copy is fresh for the
     * same channel set (unless [force]).
     * On any failure the previously stored programmes of this source stay untouched.
     */
    suspend fun sync(
        url: String,
        tvgIds: Collection<String?>?,
        now: Long = System.currentTimeMillis(),
        force: Boolean = false,
    ): EpgSyncResult = withContext(Dispatchers.IO) {
        val wanted = tvgIds?.mapNotNullTo(HashSet()) { EpgKey.normalize(it) }
        if (wanted != null && wanted.isEmpty()) return@withContext EpgSyncResult.Failed("no tvg-id")
        val wantedHash = wanted?.sorted()?.hashCode() ?: 0
        val sourceId = sourceIdFor(url)
        if (!force && dao.sourceByUrl(url)?.isFresh(wantedHash, now) == true) {
            return@withContext EpgSyncResult.Cached
        }
        val tmp = File(appContext.cacheDir, "epg_${sourceId}.tmp")
        try {
            download(url, tmp)
            val stats = tmp.inputStream().use { input ->
                XmlTvStreams.reader(input).use { reader ->
                    var result: EpgSyncResult.Success? = null
                    database.runInTransaction {
                        dao.deleteProgrammes(sourceId)
                        val batch = ArrayList<ProgrammeEntity>(BATCH)
                        val s = parser.parse(
                            reader = reader,
                            wantedKeys = wanted,
                            windowStart = now - KEEP_PAST_MS,
                            windowEnd = now + KEEP_AHEAD_MS,
                        ) { p ->
                            batch += ProgrammeEntity(
                                sourceId = sourceId,
                                channelKey = p.channelKey,
                                start = p.start,
                                stop = p.stop,
                                title = p.title,
                                desc = p.desc,
                            )
                            if (batch.size >= BATCH) {
                                dao.insertProgrammes(batch)
                                batch.clear()
                            }
                        }
                        if (batch.isNotEmpty()) dao.insertProgrammes(batch)
                        result = EpgSyncResult.Success(s.channels, s.programmesSeen, s.programmesKept)
                    }
                    result!!
                }
            }
            dao.updateSourceStatus(
                id = sourceId,
                at = now,
                status = STATUS_OK,
                count = stats.programmesKept,
                coverageUntil = dao.maxStop(sourceId) ?: 0,
                wantedHash = wantedHash,
            )
            dao.deleteEndedBefore(now - KEEP_PAST_MS)
            _version.update { it + 1 }
            stats
        } catch (e: Exception) {
            val reason = e.message ?: e.javaClass.simpleName
            dao.markSourceError(sourceId, now, "error: $reason")
            EpgSyncResult.Failed(reason)
        } finally {
            tmp.delete()
        }
    }

    /**
     * Looks [channels] up in the epg-index (by `tvg-id`, then by name), remembers the
     * channel → guide mapping and syncs only the needed XMLTV files, keeping only matched channels.
     * Each guide file is replaced as a whole, so pass every channel of the active playlist at once.
     * Throws [java.io.IOException] when the index itself is unreachable (nothing is changed then).
     * Only the [maxFiles] guide files covering the most channels are downloaded.
     */
    suspend fun syncFromIndex(
        channels: List<EpgChannelRef>,
        index: EpgIndexClient = EpgIndexClient.http(http),
        maxFiles: Int = DEFAULT_MAX_FILES,
        now: Long = System.currentTimeMillis(),
    ): EpgIndexSyncResult = withContext(Dispatchers.IO) {
        val resolution = index.resolve(channels)
        val plan = EpgSourcePlan.plan(resolution.matches.values).entries.take(maxFiles)
        val sourceByGuideKey = HashMap<String, Long>()
        plan.forEach { (url, guideIds) ->
            val sourceId = sourceIdFor(url)
            guideIds.forEach { id -> EpgKey.normalize(id)?.let { sourceByGuideKey.putIfAbsent(it, sourceId) } }
        }
        val mapRows = resolution.matches.mapNotNull { (i, m) ->
            val guideKey = EpgKey.normalize(m.guideChannelId) ?: return@mapNotNull null
            val appKey = when (m.level) {
                EpgMatchLevel.ID -> idAppKey(channels[i])
                EpgMatchLevel.NAME -> nameAppKey(channels[i])
            } ?: return@mapNotNull null
            ChannelMapEntity(appKey, guideKey, m.icon, sourceByGuideKey[guideKey])
        }
        if (mapRows.isNotEmpty()) {
            dao.upsertChannelMap(mapRows)
            _version.update { it + 1 }
        }
        val files = LinkedHashMap<String, EpgSyncResult>()
        plan.forEach { (url, guideIds) ->
            files[url] = sync(url, guideIds, now)
        }
        EpgIndexSyncResult(
            channels = channels.size,
            matchedById = resolution.matches.values.count { it.level == EpgMatchLevel.ID },
            matchedByName = resolution.matches.values.count { it.level == EpgMatchLevel.NAME },
            files = files,
            icons = resolution.matches.mapNotNull { (i, m) -> m.icon?.let { i to it } }.toMap(),
        )
    }

    /**
     * Remembers the `url-tvg` guides of an imported playlist and the `tvg-id`s they should cover,
     * replacing the previous binding of [playlistId]. Empty [urls] just clears it.
     */
    suspend fun bindPlaylistGuides(
        playlistId: Long,
        urls: Collection<String>,
        tvgIds: Collection<String?>,
    ) = withContext(Dispatchers.IO) {
        val keys = tvgIds.mapNotNullTo(HashSet()) { EpgKey.normalize(it) }
        database.runInTransaction {
            dao.deleteBindings(playlistId)
            if (keys.isNotEmpty()) {
                dao.insertBindings(urls.flatMap { url -> keys.map { GuideBindingEntity(playlistId, url, it) } })
            }
        }
    }

    suspend fun unbindPlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        dao.deleteBindings(playlistId)
    }

    /** Changes whenever a playlist's `url-tvg` binding changes — part of the sync throttle key. */
    suspend fun bindingsHash(): Int = withContext(Dispatchers.IO) {
        dao.bindings().map { "${it.url} ${it.channelKey}" }.sorted().hashCode()
    }

    /**
     * Playlist `url-tvg` guides first: channels found there are bound to that guide and never
     * looked up in the epg-index. The rest go through [syncFromIndex].
     */
    suspend fun syncAll(
        channels: List<EpgChannelRef>,
        index: EpgIndexClient = EpgIndexClient.http(http),
        maxFiles: Int = DEFAULT_MAX_FILES,
        now: Long = System.currentTimeMillis(),
    ): EpgFullSyncResult = withContext(Dispatchers.IO) {
        val guides = LinkedHashMap<String, EpgSyncResult>()
        val bound = dao.bindings().groupBy({ it.url }, { it.channelKey })
        dropUnboundPlaylistChannels(bound.keys)
        bound.forEach { (url, keys) ->
            val result = sync(url, keys, now)
            guides[url] = result
            if (result is EpgSyncResult.Success) bindFoundChannels(url, keys.toSet())
        }
        val playlistBound = dao.mappedAppKeys(ChannelMapEntity.ORIGIN_PLAYLIST).toHashSet()
        val rest = channels.filter { idAppKey(it) !in playlistBound }
        EpgFullSyncResult(
            playlistGuides = guides,
            matchedByPlaylist = channels.size - rest.size,
            index = syncFromIndex(rest, index, maxFiles, now),
        )
    }

    /** Channels of deleted playlists' guides must fall back to the epg-index. */
    private fun dropUnboundPlaylistChannels(urls: Collection<String>) {
        val keep = urls.mapNotNull { dao.sourceByUrl(it)?.id }
        if (keep.isEmpty()) {
            dao.deleteChannelMap(ChannelMapEntity.ORIGIN_PLAYLIST)
        } else {
            dao.deleteChannelMapExcept(ChannelMapEntity.ORIGIN_PLAYLIST, keep)
        }
    }

    private fun bindFoundChannels(url: String, keys: Set<String>) {
        val sourceId = sourceIdFor(url)
        val found = dao.channelKeys(sourceId).filter { it in keys }
        database.runInTransaction {
            dao.deleteChannelMap(sourceId, ChannelMapEntity.ORIGIN_PLAYLIST)
            dao.upsertChannelMap(
                found.map { ChannelMapEntity("i:$it", it, null, sourceId, ChannelMapEntity.ORIGIN_PLAYLIST) },
            )
        }
        _version.update { it + 1 }
    }

    /** Now/next per raw id from [tvgIds]; ids without guide data are absent. */
    suspend fun nowNext(
        tvgIds: Collection<String?>,
        now: Long = System.currentTimeMillis(),
    ): Map<String, EpgNowNext> = withContext(Dispatchers.IO) {
        val keyByRaw = HashMap<String, String>()
        tvgIds.forEach { raw -> EpgKey.normalize(raw)?.let { keyByRaw[raw!!] = it } }
        val byKey = nowNextByGuideKey(keyByRaw.values.toSet(), now)
        keyByRaw.mapNotNull { (raw, key) -> byKey[key]?.let { raw to it } }.toMap()
    }

    /**
     * Now/next for playlist channels: guide mapping from [syncFromIndex] first,
     * then the plain `tvg-id` (guides from the playlist's own `url-tvg`).
     */
    suspend fun nowNextFor(
        channels: Collection<EpgChannelRef>,
        now: Long = System.currentTimeMillis(),
    ): Map<EpgChannelRef, EpgNowNext> = withContext(Dispatchers.IO) {
        val guideByRef = guideRefs(channels)
        val keys = guideByRef.values.mapTo(HashSet()) { it.key }
        if (keys.isEmpty()) return@withContext emptyMap()
        val rowsByKey = keys.toList().chunked(SQL_IN_LIMIT)
            .flatMap { dao.upcoming(it, now) }
            .groupBy { it.channelKey }
        guideByRef.mapNotNull { (ref, guide) ->
            val rows = rowsByKey[guide.key]?.let(guide::select)?.sortedBy { it.start }
            if (rows.isNullOrEmpty()) return@mapNotNull null
            ref to EpgNowNext(
                now = rows.firstOrNull { it.start <= now }?.toModel(),
                next = rows.firstOrNull { it.start > now }?.toModel(),
            )
        }.toMap()
    }

    suspend fun schedule(
        tvgId: String,
        from: Long,
        to: Long,
    ): List<EpgProgramme> = withContext(Dispatchers.IO) {
        val key = EpgKey.normalize(tvgId) ?: return@withContext emptyList()
        dao.schedule(key, from, to).map { it.toModel() }
    }

    suspend fun schedule(
        channel: EpgChannelRef,
        from: Long,
        to: Long,
    ): List<EpgProgramme> = withContext(Dispatchers.IO) {
        val guide = guideRefs(listOf(channel))[channel] ?: return@withContext emptyList()
        guide.select(dao.schedule(guide.key, from, to)).map { it.toModel() }
    }

    /** Guide channel of a playlist channel; [sourceId] pins it to one file (ids collide across guides). */
    private class GuideRef(val key: String, val sourceId: Long?) {
        fun select(rows: List<ProgrammeEntity>): List<ProgrammeEntity> =
            if (sourceId == null) rows else rows.filter { it.sourceId == sourceId }
    }

    private fun guideRefs(channels: Collection<EpgChannelRef>): Map<EpgChannelRef, GuideRef> {
        val appKeys = channels.flatMapTo(HashSet()) { listOfNotNull(idAppKey(it), nameAppKey(it)) }
        val mapped = appKeys.toList().chunked(SQL_IN_LIMIT)
            .flatMap { dao.channelMap(it) }
            .associateBy { it.appKey }
        val out = HashMap<EpgChannelRef, GuideRef>()
        channels.forEach { ref ->
            val row = idAppKey(ref)?.let(mapped::get) ?: nameAppKey(ref)?.let(mapped::get)
            val guide = row?.let { GuideRef(it.guideKey, it.sourceId) }
                ?: EpgKey.normalize(ref.tvgId)?.let { GuideRef(it, null) }
            if (guide != null) out[ref] = guide
        }
        return out
    }

    private fun EpgSourceEntity.isFresh(wantedHash: Int, now: Long): Boolean =
        lastStatus == STATUS_OK &&
            this.wantedHash == wantedHash &&
            now - lastSyncAt < FRESH_MS &&
            coverageUntil - now > MIN_AHEAD_MS

    private fun nowNextByGuideKey(keys: Set<String>, now: Long): Map<String, EpgNowNext> {
        if (keys.isEmpty()) return emptyMap()
        val rows = keys.toList().chunked(SQL_IN_LIMIT).flatMap { dao.upcoming(it, now) }
        return rows.groupBy { it.channelKey }.mapValues { (_, list) ->
            val sorted = list.sortedBy { it.start }
            EpgNowNext(
                now = sorted.firstOrNull { it.start <= now }?.toModel(),
                next = sorted.firstOrNull { it.start > now }?.toModel(),
            )
        }
    }

    private fun idAppKey(ref: EpgChannelRef) = EpgKey.normalize(ref.tvgId)?.let { "i:$it" }

    private fun nameAppKey(ref: EpgChannelRef) = EpgKey.nameKey(ref.name)?.let { "n:$it" }

    suspend fun sources(): List<EpgSourceEntity> = withContext(Dispatchers.IO) { dao.sources() }

    private fun sourceIdFor(url: String): Long {
        dao.sourceByUrl(url)?.let { return it.id }
        val id = dao.insertSource(EpgSourceEntity(url = url))
        return if (id > 0) id else requireNotNull(dao.sourceByUrl(url)).id
    }

    private fun download(url: String, target: File) {
        val request = Request.Builder().url(url).build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code}")
            val body = response.body ?: error("empty body")
            target.outputStream().use { out -> body.byteStream().copyTo(out) }
        }
    }

    private fun ProgrammeEntity.toModel() = EpgProgramme(title, desc, start, stop)

    companion object {
        /** Guide files are 1–20 MB gzip each; a big playlist would otherwise pull ~80 of them. */
        const val DEFAULT_MAX_FILES = 12

        @Volatile
        private var instance: EpgStore? = null

        fun get(context: Context): EpgStore =
            instance ?: synchronized(this) {
                instance ?: EpgStore(context.applicationContext).also { instance = it }
            }

        private const val BATCH = 500
        private const val SQL_IN_LIMIT = 900
        private const val KEEP_PAST_MS = 2 * 60 * 60_000L
        private const val KEEP_AHEAD_MS = 36 * 60 * 60_000L
        private const val FRESH_MS = 12 * 60 * 60_000L
        /** Re-download before the stored guide runs out, not when the screen is already empty. */
        private const val MIN_AHEAD_MS = 12 * 60 * 60_000L
        private const val STATUS_OK = "ok"
    }
}
