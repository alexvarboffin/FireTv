package tv.hdonlinetv.compose.core.epg

import android.content.Context
import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import tv.hdonlinetv.compose.core.epg.db.ChannelMapEntity
import tv.hdonlinetv.compose.core.epg.db.EpgDao
import tv.hdonlinetv.compose.core.epg.db.EpgDatabase
import tv.hdonlinetv.compose.core.epg.db.EpgSourceEntity
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
class EpgStore(context: Context) {

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
     * On any failure the previously stored programmes of this source stay untouched.
     */
    suspend fun sync(
        url: String,
        tvgIds: Collection<String?>?,
        now: Long = System.currentTimeMillis(),
    ): EpgSyncResult = withContext(Dispatchers.IO) {
        val wanted = tvgIds?.mapNotNullTo(HashSet()) { EpgKey.normalize(it) }
        if (wanted != null && wanted.isEmpty()) return@withContext EpgSyncResult.Failed("no tvg-id")
        val sourceId = sourceIdFor(url)
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
            dao.updateSourceStatus(sourceId, now, "ok", stats.programmesKept)
            dao.deleteEndedBefore(now - KEEP_PAST_MS)
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
     */
    suspend fun syncFromIndex(
        channels: List<EpgChannelRef>,
        index: EpgIndexClient = EpgIndexClient.http(http),
        now: Long = System.currentTimeMillis(),
    ): EpgIndexSyncResult = withContext(Dispatchers.IO) {
        val resolution = index.resolve(channels)
        val mapRows = resolution.matches.mapNotNull { (i, m) ->
            val guideKey = EpgKey.normalize(m.guideChannelId) ?: return@mapNotNull null
            val appKey = when (m.level) {
                EpgMatchLevel.ID -> idAppKey(channels[i])
                EpgMatchLevel.NAME -> nameAppKey(channels[i])
            } ?: return@mapNotNull null
            ChannelMapEntity(appKey, guideKey, m.icon)
        }
        if (mapRows.isNotEmpty()) dao.upsertChannelMap(mapRows)
        val files = LinkedHashMap<String, EpgSyncResult>()
        EpgSourcePlan.plan(resolution.matches.values).forEach { (url, guideIds) ->
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
        val keyByRef = guideKeys(channels)
        val byKey = nowNextByGuideKey(keyByRef.values.toSet(), now)
        keyByRef.mapNotNull { (ref, key) -> byKey[key]?.let { ref to it } }.toMap()
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
        val key = guideKeys(listOf(channel))[channel] ?: return@withContext emptyList()
        dao.schedule(key, from, to).map { it.toModel() }
    }

    private fun guideKeys(channels: Collection<EpgChannelRef>): Map<EpgChannelRef, String> {
        val appKeys = channels.flatMapTo(HashSet()) { listOfNotNull(idAppKey(it), nameAppKey(it)) }
        val mapped = appKeys.toList().chunked(SQL_IN_LIMIT)
            .flatMap { dao.channelMap(it) }
            .associate { it.appKey to it.guideKey }
        val out = HashMap<EpgChannelRef, String>()
        channels.forEach { ref ->
            val key = idAppKey(ref)?.let(mapped::get)
                ?: nameAppKey(ref)?.let(mapped::get)
                ?: EpgKey.normalize(ref.tvgId)
            if (key != null) out[ref] = key
        }
        return out
    }

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

    private companion object {
        const val BATCH = 500
        const val SQL_IN_LIMIT = 900
        const val KEEP_PAST_MS = 2 * 60 * 60_000L
        const val KEEP_AHEAD_MS = 36 * 60 * 60_000L
    }
}
