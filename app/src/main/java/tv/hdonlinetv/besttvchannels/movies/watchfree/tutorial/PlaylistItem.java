package tv.hdonlinetv.besttvchannels.movies.watchfree.tutorial;

public class PlaylistItem implements FAQItem {

    private final String playlistTitle;
    private final String subTitle;

    public String getUrl() {
        return url;
    }

    private final String url;


    public PlaylistItem(String playlistTitle, String title, String url) {
        this.playlistTitle = playlistTitle;
        this.subTitle = title;
        this.url = url;
    }

    public String getPlaylistTitle() {
        return playlistTitle;
    }

    public String getSubTitle() {
        return subTitle;
    }

    @Override
    public int getType() {
        return FAQAdapter.VIEW_TYPE_PLAYLIST;
    }
}