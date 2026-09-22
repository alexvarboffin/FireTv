package tv.hdonlinetv.compose.navigation

import android.net.Uri

sealed class Routes(val route: String) {
    data object Splash : Routes("splash")

    data object Onboarding : Routes("onboarding")

    data object Main : Routes("main")

    data object Channels : Routes("channels/{categoryName}") {
        const val ARG_CATEGORY_NAME = "categoryName"

        fun build(categoryName: String): String =
            "channels/${Uri.encode(categoryName)}"
    }

    data object Details : Routes("details/{channelId}?scope={scope}&scopeKey={scopeKey}") {
        const val ARG_CHANNEL_ID = "channelId"
        const val ARG_SCOPE = "scope"
        const val ARG_SCOPE_KEY = "scopeKey"

        fun build(
            channelId: Long,
            scope: PlayerBrowseScope = PlayerBrowseScope.InferPlaylist,
        ): String {
            val scopeEnc = Uri.encode(scope.typeWire())
            val keyEnc = Uri.encode(scope.keyWire())
            return "details/$channelId?scope=$scopeEnc&scopeKey=$keyEnc"
        }
    }

    data object Player : Routes(
        "player/{channelId}?streamUrl={streamUrl}&streamTitle={streamTitle}" +
            "&scope={scope}&scopeKey={scopeKey}",
    ) {
        const val ARG_CHANNEL_ID = "channelId"
        const val ARG_STREAM_URL = "streamUrl"
        const val ARG_STREAM_TITLE = "streamTitle"
        const val ARG_SCOPE = "scope"
        const val ARG_SCOPE_KEY = "scopeKey"

        fun build(
            channelId: Long,
            scope: PlayerBrowseScope = PlayerBrowseScope.InferPlaylist,
        ): String {
            val scopeEnc = Uri.encode(scope.typeWire())
            val keyEnc = Uri.encode(scope.keyWire())
            return "player/$channelId?scope=$scopeEnc&scopeKey=$keyEnc"
        }

        fun buildStream(
            url: String,
            title: String,
            scope: PlayerBrowseScope = PlayerBrowseScope.None,
        ): String =
            "player/0?streamUrl=${Uri.encode(url)}" +
                "&streamTitle=${Uri.encode(title)}" +
                "&scope=${Uri.encode(scope.typeWire())}" +
                "&scopeKey=${Uri.encode(scope.keyWire())}"
    }

    data object Search : Routes("search")

    data object Settings : Routes("settings")

    data object PlaylistManage : Routes("playlist/manage")

    data object PlaylistChannels : Routes("playlist/{playlistId}/{playlistTitle}") {
        const val ARG_PLAYLIST_ID = "playlistId"
        const val ARG_PLAYLIST_TITLE = "playlistTitle"

        fun build(playlistId: Long, title: String): String =
            "playlist/$playlistId/${Uri.encode(title)}"
    }

    data object XtreamBrowser : Routes("xtream/{playlistId}") {
        const val ARG_PLAYLIST_ID = "playlistId"

        fun build(playlistId: Long): String = "xtream/$playlistId"
    }

    data object SerialDetail : Routes("serial/{playlistId}/{seriesId}/{seriesTitle}") {
        const val ARG_PLAYLIST_ID = "playlistId"
        const val ARG_SERIES_ID = "seriesId"
        const val ARG_SERIES_TITLE = "seriesTitle"

        fun build(playlistId: Long, seriesId: Int, seriesTitle: String): String =
            "serial/$playlistId/$seriesId/${Uri.encode(seriesTitle)}"
    }

    data object Tutorial : Routes("tutorial")

    data object InfoWeb : Routes("info?url={url}&title={title}") {
        const val ARG_URL = "url"
        const val ARG_TITLE = "title"

        fun build(url: String, title: String): String =
            "info?url=${Uri.encode(url)}&title=${Uri.encode(title)}"
    }
}
