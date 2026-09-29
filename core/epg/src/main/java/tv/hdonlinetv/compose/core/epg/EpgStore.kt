package tv.hdonlinetv.compose.core.epg

import android.content.Context
import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import tv.hdonlinetv.compose.core.epg.db.EpgDao
import tv.hdonlinetv.compose.core.epg.db.EpgDatabase
import tv.hdonlinetv.compose.core.epg.db.EpgSourceEntity
import tv.hdonlinetv.compose.core.epg.db.ProgrammeEntity
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

    /** Now/next per raw id from [tvgIds]; ids without guide data are absent. */
    suspend fun nowNext(
        tvgIds: Collection<String?>,
        now: Long = System.currentTimeMillis(),
    ): Map<String, EpgNowNext> = withContext(Dispatchers.IO) {
        val rawByKey = HashMap<String, MutableList<String>>()
        tvgIds.forEach { raw ->
            val key = EpgKey.normalize(raw) ?: return@forEach
            rawByKey.getOrPut(key) { ArrayList(1) }.add(raw!!)
        }
        if (rawByKey.isEmpty()) return@withContext emptyMap()
        val rows = rawByKey.keys.chunked(SQL_IN_LIMIT).flatMap { dao.upcoming(it, now) }
        val out = HashMap<String, EpgNowNext>()
        rows.groupBy { it.channelKey }.forEach { (key, list) ->
            val sorted = list.sortedBy { it.start }
            val current = sorted.firstOrNull { it.start <= now }
            val next = sorted.firstOrNull { it.start > now }
            val value = EpgNowNext(current?.toModel(), next?.toModel())
            rawByKey[key]?.forEach { out[it] = value }
        }
        out
    }

    suspend fun schedule(
        tvgId: String,
        from: Long,
        to: Long,
    ): List<EpgProgramme> = withContext(Dispatchers.IO) {
        val key = EpgKey.normalize(tvgId) ?: return@withContext emptyList()
        dao.schedule(key, from, to).map { it.toModel() }
    }

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
