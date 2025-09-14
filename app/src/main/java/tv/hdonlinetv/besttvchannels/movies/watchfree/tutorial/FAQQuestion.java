package tv.hdonlinetv.besttvchannels.movies.watchfree.tutorial;

public class FAQQuestion implements FAQItem {
    private final String question;
    private final String htmlUrl; // URL or path to the HTML file

    public FAQQuestion(String question, String htmlUrl) {
        this.question = question;
        this.htmlUrl = htmlUrl;
    }

    public String getQuestion() {
        return question;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    @Override
    public int getType() {
        return FAQAdapter.VIEW_TYPE_FAQ;
    }
}