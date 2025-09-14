package tv.hdonlinetv.besttvchannels.movies.watchfree.model;

public class CategoryUI {
    public String name, desc, thumb;

    public CategoryUI() {
    }

    public CategoryUI(String name, String desc, String thumb) {
        this.name = name;
        this.desc = desc;
        this.thumb = thumb;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }
}
