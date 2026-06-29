package tv.hdonlinetv.compose.core.databridge.xtream

import com.m3u.data.database.model.ResponseData
import com.m3u.data.parser.xtream.XtreamChannelInfo
import com.m3u.data.parser.xtream.XtreamInput
import com.m3u.data.parser.xtream.XtreamLive
import com.m3u.data.parser.xtream.XtreamSerial
import com.m3u.data.parser.xtream.XtreamVod
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.domain.model.SeriesDetailUi
import tv.hdonlinetv.compose.core.domain.model.SeriesEpisodeUi
import tv.hdonlinetv.compose.core.domain.model.SeriesInfoUi
import tv.hdonlinetv.compose.core.domain.model.SeriesSeasonUi

internal object XtreamStreamMapper {

    fun parseInput(playlistApiUrl: String): XtreamInput? =
        XtreamInput.decodeFromPlaylistUrlOrNull(playlistApiUrl)

    fun liveToUi(stream: XtreamLive, input: XtreamInput): ChannelUi {
        val baseUrl = input.basicUrl.toHttpUrlOrNull()
        val url = if (baseUrl != null) {
            buildLiveStreamUrl(baseUrl, input.username, input.password, stream.streamId?.toString().orEmpty(), "ts")
        } else {
            ""
        }
        return ChannelUi(
            id = 0,
            name = stream.name.orEmpty(),
            cover = stream.streamIcon,
            category = stream.categoryId?.toString(),
            link = url,
            desc = stream.epgChannelId,
        )
    }

    fun vodToUi(stream: XtreamVod, input: XtreamInput): ChannelUi {
        val baseUrl = input.basicUrl.toHttpUrlOrNull()
        val extension = stream.containerExtension ?: "mp4"
        val url = if (baseUrl != null) {
            buildVodStreamUrl(baseUrl, input.username, input.password, stream.streamId, extension)
        } else {
            ""
        }
        return ChannelUi(
            id = 0,
            name = stream.name.orEmpty(),
            cover = stream.streamIcon,
            category = stream.categoryId?.toString(),
            link = url,
            desc = stream.streamId?.toString(),
        )
    }

    fun seriesToUi(stream: XtreamSerial): ChannelUi = ChannelUi(
        id = 0,
        name = stream.name.orEmpty(),
        cover = stream.cover,
        category = stream.categoryId?.toString(),
        link = null,
        desc = stream.seriesId?.toString(),
    )

    fun seriesDetailToUi(data: ResponseData): SeriesDetailUi {
        val info = data.info
        val seasons = data.seasons.orEmpty().mapNotNull { season ->
            season ?: return@mapNotNull null
            SeriesSeasonUi(
                id = season.id,
                name = season.name,
                seasonNumber = season.seasonNumber,
                episodeCount = season.episodeCount,
                cover = season.cover ?: season.coverBig,
                overview = season.overview,
            )
        }
        val episodesBySeason = mutableMapOf<Int, List<SeriesEpisodeUi>>()
        data.episodes?.forEach { (seasonKey, episodeList) ->
            val seasonNum = seasonKey?.toIntOrNull() ?: return@forEach
            val episodes = episodeList.orEmpty().mapNotNull { episode ->
                episode ?: return@mapNotNull null
                SeriesEpisodeUi(
                    id = episode.id.orEmpty(),
                    title = episode.title,
                    episodeNum = episode.episodeNum,
                    season = episode.season,
                    containerExtension = episode.containerExtension,
                    plot = episode.info?.plot,
                    movieImage = episode.info?.movieImage,
                )
            }
            episodesBySeason[seasonNum] = episodes
        }
        return SeriesDetailUi(
            info = info?.let { mapInfo(it) },
            seasons = seasons,
            episodesBySeason = episodesBySeason,
        )
    }

    fun buildEpisodeStreamUrl(
        input: XtreamInput,
        episodeId: String,
        containerExtension: String,
    ): String? {
        val baseUrl = input.basicUrl.toHttpUrlOrNull() ?: return null
        return HttpUrl.Builder()
            .scheme(baseUrl.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .addPathSegment("series")
            .addPathSegment(input.username)
            .addPathSegment(input.password)
            .addPathSegment("$episodeId.$containerExtension")
            .build()
            .toString()
    }

    private fun mapInfo(info: XtreamChannelInfo.Info): SeriesInfoUi = SeriesInfoUi(
        name = info.name,
        cover = info.cover,
        plot = info.plot,
        cast = info.cast,
        director = info.director,
        genre = info.genre,
        categoryId = info.categoryId,
        releaseDate = info.releaseDate,
        rating = info.rating,
        rating5Based = info.rating5based,
        episodeRunTime = info.episodeRunTime,
        lastModified = info.lastModified,
        youtubeTrailer = info.youtubeTrailer,
    )

    private fun buildLiveStreamUrl(
        baseUrl: HttpUrl,
        username: String,
        password: String,
        streamId: String,
        containerExtension: String,
    ): String = HttpUrl.Builder()
        .scheme(baseUrl.scheme)
        .host(baseUrl.host)
        .port(baseUrl.port)
        .addPathSegment("live")
        .addPathSegment(username)
        .addPathSegment(password)
        .addPathSegment("$streamId.$containerExtension")
        .build()
        .toString()

    private fun buildVodStreamUrl(
        baseUrl: HttpUrl,
        username: String,
        password: String,
        streamId: Int?,
        containerExtension: String,
    ): String = HttpUrl.Builder()
        .scheme(baseUrl.scheme)
        .host(baseUrl.host)
        .port(baseUrl.port)
        .addPathSegment("movie")
        .addPathSegment(username)
        .addPathSegment(password)
        .addPathSegment("$streamId.$containerExtension")
        .build()
        .toString()
}
