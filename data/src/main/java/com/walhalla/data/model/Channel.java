package com.walhalla.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

//10928

@Entity(tableName = "channel", indices = {@Index(value = {"name"}, unique = true)}/**/)
public class Channel implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    public long _id;
    public String extUserAgent;
    public String extReferer;


    public String getTvgId() {
        return tvgId;
    }

    public void setTvgId(String tvgId) {
        this.tvgId = tvgId;
    }

    @ColumnInfo(name = "tvgId")
    String tvgId;

    public String tvgLanguage;
    public String tvgCountry;
    public String tvgUrl;




    @ColumnInfo(name = "image")
    String cover;

    @ColumnInfo(name = "name")
    String name;


    public String getUa() {
        return ua;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    String ua;//user_agent

    String cat;
    String type;
    String lnk;
    String desc;
    String lang;

    @ColumnInfo(name = "liked", defaultValue = "0")
    public int liked;

    public Channel() {
    }

    public Channel(String id, String channelImage, String channelName, String channelCategory, String channelType, String channelLink, String channelDesc, String channelLanguage) {
        this.tvgId = id;
        this.cover = channelImage;
        this.name = channelName;
        this.cat = channelCategory;
        this.type = channelType;
        this.lnk = channelLink;
        this.desc = channelDesc;
        this.lang = channelLanguage;
    }
    public Channel(
            String url,
            String category,
            String title,
            String cover,
            String playlistUrl,
            String originalId
    ) {
        this.lnk = url;
        this.cat = category;
        this.name = title;
        this.cover = cover;
        this.lang = cover;

//        this.playlistUrl = playlistUrl;
        this.tvgId = originalId;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLnk() {
        return lnk;
    }

    public void setLnk(String lnk) {
        this.lnk = lnk;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
