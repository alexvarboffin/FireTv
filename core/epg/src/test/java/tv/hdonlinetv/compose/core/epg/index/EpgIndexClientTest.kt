package tv.hdonlinetv.compose.core.epg.index

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import tv.hdonlinetv.compose.core.epg.EpgKey
import java.io.FileNotFoundException

class EpgIndexClientTest {

    private val sources = listOf("https://g/es.xml.gz", "https://g/pluto.xml.gz", "https://g/es2.xml.gz")

    /** key → [guideId, sources, icon?] */
    private val ids = mapOf(
        "3catinfoes" to Triple("3CatInfo.es", listOf(0, 2), "https://i/3cat.png"),
        "la1es" to Triple("La1.es", listOf(0), null),
        "plutothrillers" to Triple("5dcddf1ed95e740009fef7ab", listOf(1), null),
    )
    private val names = mapOf(
        "canalsurandalucia" to Triple("CanalSur.es", listOf(2), "https://i/sur.png"),
    )

    private fun fixture(fullGzip: Long = 1_000_000, shardAvg: Long = 1_000, cost: Long = 0): Map<String, String> {
        val shards = 4
        fun table(field: String, map: Map<String, Triple<String, List<Int>, String?>>) =
            map.entries.joinToString(",", "{\"$field\":{", "}}") { (k, v) ->
                val icon = v.third?.let { ",\"$it\"" }.orEmpty()
                "\"$k\":[\"${v.first}\",${v.second}$icon]"
            }
        val files = HashMap<String, String>()
        files["index/api.json"] = """
            {"version":2,"hash":"crc32-utf8","shards":$shards,
             "idsFull":"index/ids.json","idsShard":"index/ids/{shard}.json",
             "namesFull":"index/names.json","namesShard":"index/names/{shard}.json",
             "idsFullGzip":$fullGzip,"idsShardGzipAvg":$shardAvg,
             "namesFullGzip":$fullGzip,"namesShardGzipAvg":$shardAvg,
             "requestCostBytes":$cost,
             "sources":[${sources.joinToString(",") { "{\"p\":\"x\",\"u\":\"$it\"}" }}]}
        """.trimIndent()
        files["index/ids.json"] = table("ids", ids)
        files["index/names.json"] = table("names", names)
        for (n in 0 until shards) {
            val p = n.toString().padStart(3, '0')
            files["index/ids/$p.json"] = table("ids", ids.filterKeys { EpgKey.shardOf(it, shards) == n })
            files["index/names/$p.json"] = table("names", names.filterKeys { EpgKey.shardOf(it, shards) == n })
        }
        return files
    }

    private fun client(files: Map<String, String>) = EpgIndexClient { path ->
        (files[path] ?: throw FileNotFoundException(path)).byteInputStream()
    }

    @Test
    fun fullPlaylistMatchesByIdWithCaseAndFeed() {
        val r = client(fixture()).resolve(
            listOf(EpgChannelRef("3catinfo.es@HD", "3Cat Info"), EpgChannelRef("La1.es", "La 1")),
        )
        val m = r.matches.getValue(0)
        assertEquals(EpgMatchLevel.ID, m.level)
        assertEquals("3CatInfo.es", m.guideChannelId)
        assertEquals(listOf("https://g/es.xml.gz", "https://g/es2.xml.gz"), m.sourceUrls)
        assertEquals("https://i/3cat.png", m.icon)
        assertNull(r.matches.getValue(1).icon)
        assertEquals("shards+none", r.mode)
    }

    @Test
    fun idOnlyChannelGetsHashGuideId() {
        val r = client(fixture()).resolve(listOf(EpgChannelRef("PlutoThrillers", null)))
        assertEquals("5dcddf1ed95e740009fef7ab", r.matches.getValue(0).guideChannelId)
        assertEquals(listOf("https://g/pluto.xml.gz"), r.matches.getValue(0).sourceUrls)
    }

    @Test
    fun unknownIdFallsBackToName() {
        val r = client(fixture()).resolve(
            listOf(EpgChannelRef("Nope.es", "Canal Sur Andalucía (1080p) [Geo-blocked]")),
        )
        val m = r.matches.getValue(0)
        assertEquals(EpgMatchLevel.NAME, m.level)
        assertEquals("CanalSur.es", m.guideChannelId)
        assertEquals("shards+shards", r.mode)
    }

    @Test
    fun nameOnlyAndUnknownChannels() {
        val r = client(fixture()).resolve(
            listOf(EpgChannelRef(null, "CANAL SUR ANDALUCIA HD"), EpgChannelRef("", "Something Else")),
        )
        assertEquals(setOf(0), r.matches.keys)
        assertEquals("none+shards", r.mode)
    }

    @Test
    fun emptyPlaylistFetchesOnlyApi() {
        val r = client(fixture()).resolve(emptyList())
        assertEquals(listOf("index/api.json"), r.requests)
        assertEquals("none+none", r.mode)
    }

    @Test
    fun switchesToFullFileWhenShardsCostMore() {
        val refs = listOf(EpgChannelRef("3CatInfo.es", null), EpgChannelRef("La1.es", null))
        val r = client(fixture(fullGzip = 10_000, shardAvg = 1_000, cost = 16_384)).resolve(refs)
        assertEquals(listOf("index/api.json", "index/ids.json"), r.requests)
        assertEquals("full+none", r.mode)
        assertEquals(2, r.matches.size)
    }

    @Test
    fun shardRequestsAreOnePerNeededShard() {
        val refs = ids.keys.map { EpgChannelRef(it, null) }
        val r = client(fixture()).resolve(refs)
        val shards = ids.keys.map { EpgKey.shardOf(it, 4) }.toSet()
        assertEquals(1 + shards.size, r.requests.size)
        assertTrue(r.requests.drop(1).all { it.startsWith("index/ids/") })
    }

    @Test
    fun planPrefersSharedFile() {
        val r = client(fixture()).resolve(
            listOf(EpgChannelRef("3CatInfo.es", null), EpgChannelRef("La1.es", null)),
        )
        assertEquals(
            mapOf("https://g/es.xml.gz" to setOf("3CatInfo.es", "La1.es")),
            EpgSourcePlan.plan(r.matches.values),
        )
    }
}
