package tv.hdonlinetv.compose.core.domain.model

data class SeriesInfoUi(
    val name: String?,
    val cover: String?,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?,
    val categoryId: String?,
    val releaseDate: String?,
    val rating: String?,
    val rating5Based: String?,
    val episodeRunTime: String?,
    val lastModified: String?,
    val youtubeTrailer: String?,
)

data class SeriesSeasonUi(
    val id: Int,
    val name: String?,
    val seasonNumber: Int,
    val episodeCount: Int,
    val cover: String?,
    val overview: String?,
)

data class SeriesEpisodeUi(
    val id: String,
    val title: String?,
    val episodeNum: Int,
    val season: Int,
    val containerExtension: String?,
    val plot: String?,
    val movieImage: String?,
)

data class SeriesDetailUi(
    val info: SeriesInfoUi?,
    val seasons: List<SeriesSeasonUi>,
    val episodesBySeason: Map<Int, List<SeriesEpisodeUi>>,
)
