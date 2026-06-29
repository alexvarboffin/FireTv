package tv.hdonlinetv.compose.core.databridge.xtream

import com.walhalla.xtream.api.XtreamApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.domain.model.SeriesDetailUi
import tv.hdonlinetv.compose.core.domain.repository.XtreamRepository
import tv.hdonlinetv.compose.core.domain.repository.XtreamStreamType
import java.util.concurrent.TimeUnit

class LocalXtreamRepository : XtreamRepository {

    private val client = OkHttpClient.Builder()
        .readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
        .connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
        .build()

    override suspend fun getStreams(
        playlistApiUrl: String,
        type: XtreamStreamType,
    ): List<ChannelUi> = withContext(Dispatchers.IO) {
        val input = XtreamStreamMapper.parseInput(playlistApiUrl) ?: return@withContext emptyList()
        val api = createApi(input.basicUrl)
        when (type) {
            XtreamStreamType.LIVE -> {
                val response = api.getLiveStreams(
                    input.username,
                    input.password,
                    ACTION_LIVE,
                ).execute()
                if (!response.isSuccessful) return@withContext emptyList()
                response.body()?.map { XtreamStreamMapper.liveToUi(it, input) }.orEmpty()
            }
            XtreamStreamType.VOD -> {
                val response = api.getVodStreams(
                    input.username,
                    input.password,
                    ACTION_VOD,
                ).execute()
                if (!response.isSuccessful) return@withContext emptyList()
                response.body()?.map { XtreamStreamMapper.vodToUi(it, input) }.orEmpty()
            }
            XtreamStreamType.SERIES -> {
                val response = api.getSerialStreams(
                    input.username,
                    input.password,
                    ACTION_SERIES,
                ).execute()
                if (!response.isSuccessful) return@withContext emptyList()
                response.body()?.map { XtreamStreamMapper.seriesToUi(it) }.orEmpty()
            }
        }
    }

    override suspend fun getSeriesInfo(
        playlistApiUrl: String,
        seriesId: Int,
    ): SeriesDetailUi? = withContext(Dispatchers.IO) {
        val input = XtreamStreamMapper.parseInput(playlistApiUrl) ?: return@withContext null
        val api = createApi(input.basicUrl)
        val response = api.getSeriesInfo(
            input.username,
            input.password,
            ACTION_SERIES_INFO,
            seriesId,
        ).execute()
        if (!response.isSuccessful) return@withContext null
        val body = response.body() ?: return@withContext null
        XtreamStreamMapper.seriesDetailToUi(body)
    }

    override fun buildEpisodeStreamUrl(
        playlistApiUrl: String,
        episodeId: String,
        containerExtension: String,
    ): String? {
        val input = XtreamStreamMapper.parseInput(playlistApiUrl) ?: return null
        return XtreamStreamMapper.buildEpisodeStreamUrl(input, episodeId, containerExtension)
    }

    private fun createApi(baseUrl: String): XtreamApi {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val retrofit = Retrofit.Builder()
            .baseUrl(normalized)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(XtreamApi::class.java)
    }

    companion object {
        private const val TIMEOUT_SEC = 30L
        private const val ACTION_LIVE = "get_live_streams"
        private const val ACTION_VOD = "get_vod_streams"
        private const val ACTION_SERIES = "get_series"
        private const val ACTION_SERIES_INFO = "get_series_info"
    }
}
