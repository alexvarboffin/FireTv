package tv.hdonlinetv.besttvchannels.movies.watchfree;





public enum StreamType {
    LIVE("get_live_streams"),
    SERIES("get_series_streams"),
    VOD("get_vod_streams");

    private final String action;

    StreamType(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}

