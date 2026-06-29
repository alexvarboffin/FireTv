package tv.hdonlinetv.besttvchannels.movies.watchfree


enum class StreamType(val action: String) {
    LIVE("get_live_streams"),
    SERIES("get_series_streams"),
    VOD("get_vod_streams");

}

