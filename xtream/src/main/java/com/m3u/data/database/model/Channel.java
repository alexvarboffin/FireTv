package com.m3u.data.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "channels")
public class Channel {

    @ColumnInfo(name = "url")
    public String url;

    @ColumnInfo(name = "group")
    public String category;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "cover")
    public String cover;

    @ColumnInfo(name = "playlistUrl", index = true)
    public String playlistUrl;

    @ColumnInfo(name = "license_type", defaultValue = "NULL")
    public String licenseType;

    @ColumnInfo(name = "license_key", defaultValue = "NULL")
    public String licenseKey;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "favourite", index = true)
    public boolean favourite;

    @ColumnInfo(name = "hidden", defaultValue = "0")
    public boolean hidden;

    @ColumnInfo(name = "seen", defaultValue = "0")
    public long seen;

    @ColumnInfo(name = "channel_id", defaultValue = "NULL")
    public String originalId;

    // Static constants for license types
    public static final String LICENSE_TYPE_WIDEVINE = "com.widevine.alpha";
    public static final String LICENSE_TYPE_CLEAR_KEY = "clearkey";
    public static final String LICENSE_TYPE_CLEAR_KEY_2 = "org.w3.clearkey";
    public static final String LICENSE_TYPE_PLAY_READY = "com.microsoft.playready";

    // Constructor
    public Channel(String url, String category, String title, String cover, String playlistUrl,
                   String licenseType, String licenseKey, int id, boolean favourite, boolean hidden,
                   long seen, String originalId) {
        this.url = url;
        this.category = category;
        this.title = title;
        this.cover = cover;
        this.playlistUrl = playlistUrl;
        this.licenseType = licenseType;
        this.licenseKey = licenseKey;
        this.id = id;
        this.favourite = favourite;
        this.hidden = hidden;
        this.seen = seen;
        this.originalId = originalId;
    }

    public Channel(
            String url,
            String category,
            String title,
            String cover,
            String playlistUrl,
            String originalId
    ) {
        this.url = url;
        this.category = category;
        this.title = title;
        this.cover = cover;
        this.playlistUrl = playlistUrl;
        this.originalId = originalId;
    }

}
