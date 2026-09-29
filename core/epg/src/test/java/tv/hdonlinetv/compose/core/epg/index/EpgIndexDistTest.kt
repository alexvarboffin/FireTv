package tv.hdonlinetv.compose.core.epg.index

import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/**
 * Real index + real playlist, compare with `python resolve.py <m3u> --local <dist>`:
 * EPG_INDEX_DIST=…/epg-index/dist EPG_LIVE_M3U=es.m3u ./gradlew :core:epg:testDebugUnitTest
 */
class EpgIndexDistTest {

    @Test
    fun resolveRealPlaylist() {
        val dist = System.getenv("EPG_INDEX_DIST")?.let(::File)
        val m3u = System.getenv("EPG_LIVE_M3U")?.let(::File)
        assumeTrue(dist?.isDirectory == true && m3u?.isFile == true)
        val refs = EXTINF.findAll(m3u!!.readText()).map { m ->
            EpgChannelRef(TVG_ID.find(m.groupValues[1])?.groupValues?.get(1), m.groupValues[2].trim())
        }.toList()
        val r = EpgIndexClient { path -> File(dist, path).inputStream() }.resolve(refs)
        val byId = r.matches.values.count { it.level == EpgMatchLevel.ID }
        val byName = r.matches.values.count { it.level == EpgMatchLevel.NAME }
        println(
            "channels=${refs.size} matched id=$byId name=$byName mode=${r.mode} " +
                "requests=${r.requests.size} files=${EpgSourcePlan.plan(r.matches.values).size}",
        )
    }

    private companion object {
        val EXTINF = Regex("#EXTINF:((?:[^,\"\\n]|\"[^\"]*\")*),([^\\n]*)")
        val TVG_ID = Regex("tvg-id=\"([^\"]*)\"")
    }
}
