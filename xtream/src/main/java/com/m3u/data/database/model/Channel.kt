package com.m3u.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
class Channel {
    @ColumnInfo(name = "url")
    var url: String?

    @ColumnInfo(name = "group")
    var category: String?

    @ColumnInfo(name = "title")
    var title: String?

    @ColumnInfo(name = "cover")
    var cover: String?

    @ColumnInfo(name = "playlistUrl", index = true)
    var playlistUrl: String?

    @ColumnInfo(name = "license_type", defaultValue = "NULL")
    var licenseType: String? = null

    @ColumnInfo(name = "license_key", defaultValue = "NULL")
    var licenseKey: String? = null

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0

    @ColumnInfo(name = "favourite", index = true)
    var favourite: Boolean = false

    @ColumnInfo(name = "hidden", defaultValue = "0")
    var hidden: Boolean = false

    @ColumnInfo(name = "seen", defaultValue = "0")
    var seen: Long = 0

    @ColumnInfo(name = "channel_id", defaultValue = "NULL")
    var originalId: String?

    // Constructor
    constructor(
        url: String?, category: String?, title: String?, cover: String?, playlistUrl: String?,
        licenseType: String?, licenseKey: String?, id: Int, favourite: Boolean, hidden: Boolean,
        seen: Long, originalId: String?
    ) {
        this.url = url
        this.category = category
        this.title = title
        this.cover = cover
        this.playlistUrl = playlistUrl
        this.licenseType = licenseType
        this.licenseKey = licenseKey
        this.id = id
        this.favourite = favourite
        this.hidden = hidden
        this.seen = seen
        this.originalId = originalId
    }

    constructor(
        url: String?,
        category: String?,
        title: String?,
        cover: String?,
        playlistUrl: String?,
        originalId: String?
    ) {
        this.url = url
        this.category = category
        this.title = title
        this.cover = cover
        this.playlistUrl = playlistUrl
        this.originalId = originalId
    }

    companion object {
        // Static constants for license types
        const val LICENSE_TYPE_WIDEVINE: String = "com.widevine.alpha"
        const val LICENSE_TYPE_CLEAR_KEY: String = "clearkey"
        const val LICENSE_TYPE_CLEAR_KEY_2: String = "org.w3.clearkey"
        const val LICENSE_TYPE_PLAY_READY: String = "com.microsoft.playready"
    }
}
