package tv.hdonlinetv.besttvchannels.movies.watchfree.tutorial;

public class HeaderItem implements FAQItem {
    private final String headerTitle;

    public HeaderItem(String headerTitle) {
        this.headerTitle = headerTitle;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    @Override
    public int getType() {
        return FAQAdapter.VIEW_TYPE_HEADER;
    }
}