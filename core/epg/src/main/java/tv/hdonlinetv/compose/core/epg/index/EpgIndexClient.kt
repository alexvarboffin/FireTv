package tv.hdonlinetv.compose.core.epg.index

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import okhttp3.OkHttpClient
import okhttp3.Request
import tv.hdonlinetv.compose.core.epg.EpgKey
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/** Opens a file of the index by its path relative to the index root (`index/api.json`). */
fun interface EpgIndexFetcher {
    @Throws(IOException::class)
    fun open(path: String): InputStream
}

/**
 * Client for the epg-index lookup API (github.com/ishumakov881/epg-index, "Client contract").
 * Tells which XMLTV file carries each playlist channel and under which id; downloads only the
 * shards it needs, or the full map when that is cheaper. Blocking вЂ” call from IO.
 */
class EpgIndexClient(private val fetcher: EpgIndexFetcher) {

    fun resolve(channels: List<EpgChannelRef>): EpgIndexResolution {
        val requests = ArrayList<String>()
        fun <T> read(path: String, block: (JsonReader) -> T): T {
            requests += path
            return fetcher.open(path).use { input ->
                JsonReader(InputStreamReader(input, Charsets.UTF_8)).use(block)
            }
        }

        val api = read(API_PATH, ::readApi)
        val matches = HashMap<Int, EpgGuideMatch>()

        val idKeys = HashMap<Int, String>()
        channels.forEachIndexed { i, ch -> EpgKey.normalize(ch.tvgId)?.let { idKeys[i] = it } }
        val fetchTable: (String, (JsonReader) -> Map<String, Entry>) -> Map<String, Entry> =
            { path, block -> read(path, block) }
        val (ids, idsMode) = load(api, Kind.IDS, idKeys.values.toSet(), fetchTable)
        idKeys.forEach { (i, key) -> ids[key]?.let { matches[i] = it.toMatch(EpgMatchLevel.ID, api) } }

        val nameKeys = HashMap<Int, String>()
        channels.forEachIndexed { i, ch ->
            if (i !in matches) EpgKey.nameKey(ch.name)?.let { nameKeys[i] = it }
        }
        val (names, namesMode) = load(api, Kind.NAMES, nameKeys.values.toSet(), fetchTable)
        nameKeys.forEach { (i, key) -> names[key]?.let { matches[i] = it.toMatch(EpgMatchLevel.NAME, api) } }

        return EpgIndexResolution(matches, requests, "$idsMode+$namesMode")
    }

    private enum class Kind(val field: String) { IDS("ids"), NAMES("names") }

    private class Entry(val guideId: String, val sources: List<Int>, val icon: String?)

    private fun Entry.toMatch(level: EpgMatchLevel, api: EpgIndexApi) = EpgGuideMatch(
        level = level,
        guideChannelId = guideId,
        sourceUrls = sources.mapNotNull { api.sources.getOrNull(it) },
        icon = icon,
    )

    private fun load(
        api: EpgIndexApi,
        kind: Kind,
        keys: Set<String>,
        read: (String, (JsonReader) -> Map<String, Entry>) -> Map<String, Entry>,
    ): Pair<Map<String, Entry>, String> {
        if (keys.isEmpty()) return emptyMap<String, Entry>() to "none"
        val needed = keys.mapTo(sortedSetOf()) { EpgKey.shardOf(it, api.shards) }
        val (full, shardAvg, fullPath, shardPath) = when (kind) {
            Kind.IDS -> Quad(api.idsFullGzip, api.idsShardGzipAvg, api.idsFull, api.idsShard)
            Kind.NAMES -> Quad(api.namesFullGzip, api.namesShardGzipAvg, api.namesFull, api.namesShard)
        }
        if (needed.size * (shardAvg + api.requestCostBytes) >= full) {
            return read(fullPath) { readTable(it, kind.field, keys) } to "full"
        }
        val table = HashMap<String, Entry>()
        for (n in needed) {
            val path = shardPath.replace("{shard}", n.toString().padStart(3, '0'))
            table.putAll(read(path) { readTable(it, kind.field, keys) })
        }
        return table to "shards"
    }

    private data class Quad(val full: Long, val shardAvg: Long, val fullPath: String, val shardPath: String)

    private fun readApi(r: JsonReader): EpgIndexApi {
        var shards = 0
        val paths = HashMap<String, String>()
        val sizes = HashMap<String, Long>()
        val sources = ArrayList<String>()
        r.beginObject()
        while (r.hasNext()) {
            when (val name = r.nextName()) {
                "shards" -> shards = r.nextInt()
                "idsFull", "idsShard", "namesFull", "namesShard" -> paths[name] = r.nextString()
                "idsFullGzip", "idsShardGzipAvg", "namesFullGzip", "namesShardGzipAvg", "requestCostBytes" ->
                    sizes[name] = r.nextLong()
                "sources" -> {
                    r.beginArray()
                    while (r.hasNext()) sources += readSourceUrl(r)
                    r.endArray()
                }
                else -> r.skipValue()
            }
        }
        r.endObject()
        require(shards > 0) { "api.json: shards" }
        return EpgIndexApi(
            shards = shards,
            idsFull = paths.getValue("idsFull"),
            idsShard = paths.getValue("idsShard"),
            namesFull = paths.getValue("namesFull"),
            namesShard = paths.getValue("namesShard"),
            idsFullGzip = sizes.getValue("idsFullGzip"),
            idsShardGzipAvg = sizes.getValue("idsShardGzipAvg"),
            namesFullGzip = sizes.getValue("namesFullGzip"),
            namesShardGzipAvg = sizes.getValue("namesShardGzipAvg"),
            requestCostBytes = sizes["requestCostBytes"] ?: 0L,
            sources = sources,
        )
    }

    private fun readSourceUrl(r: JsonReader): String {
        var url = ""
        r.beginObject()
        while (r.hasNext()) {
            if (r.nextName() == "u") url = r.nextString() else r.skipValue()
        }
        r.endObject()
        return url
    }

    /** Streams `{"<field>": {key: [guideId, [srcвЂ¦], icon?]}}`, keeping only [wanted] keys. */
    private fun readTable(r: JsonReader, field: String, wanted: Set<String>): Map<String, Entry> {
        val out = HashMap<String, Entry>()
        r.beginObject()
        while (r.hasNext()) {
            if (r.nextName() != field) {
                r.skipValue()
                continue
            }
            r.beginObject()
            while (r.hasNext()) {
                val key = r.nextName()
                if (key in wanted) out[key] = readEntry(r) else r.skipValue()
            }
            r.endObject()
        }
        r.endObject()
        return out
    }

    private fun readEntry(r: JsonReader): Entry {
        r.beginArray()
        val guideId = r.nextString()
        val sources = ArrayList<Int>(2)
        r.beginArray()
        while (r.hasNext()) sources += r.nextInt()
        r.endArray()
        var icon: String? = null
        if (r.hasNext()) {
            if (r.peek() == JsonToken.NULL) r.nextNull() else icon = r.nextString()
        }
        while (r.hasNext()) r.skipValue()
        r.endArray()
        return Entry(guideId, sources, icon)
    }

    companion object {
        const val API_PATH = "index/api.json"

        /** GitHub Pages (primary), then the `data` branch mirror. */
        val DEFAULT_BASES = listOf(
            "https://ishumakov881.github.io/epg-index/",
            "https://raw.githubusercontent.com/ishumakov881/epg-index/data/",
        )

        /** Tries [bases] in order per file. OkHttp negotiates gzip transparently. */
        fun http(client: OkHttpClient, bases: List<String> = DEFAULT_BASES) = EpgIndexClient { path ->
            var last: IOException? = null
            for (base in bases) {
                val response = try {
                    client.newCall(Request.Builder().url(base + path).build()).execute()
                } catch (e: IOException) {
                    last = e
                    continue
                }
                if (response.isSuccessful) return@EpgIndexClient response.body.byteStream()
                response.close()
                last = IOException("HTTP ${response.code} $base$path")
            }
            throw last ?: IOException("no index base")
        }
    }
}
