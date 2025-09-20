package com.walhalla.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

//10928
@Entity(tableName = "channel", indices = [Index(value = ["name"], unique = true)])
class Channel : Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var _id: Long = 0
    var extUserAgent: String? = null
    var extReferer: String? = null


    @ColumnInfo(name = "tvgId")
    var tvgId: String? = null

    var tvgLanguage: String? = null
    var tvgCountry: String? = null
    var tvgUrl: String? = null


    @ColumnInfo(name = "image")
    var cover: String? = null

    @ColumnInfo(name = "name")
    var name: String = ""


    var ua: String? = null //user_agent

    var cat: String? = null
    var type: String? = null
    var lnk: String? = null
    var desc: String? = null
    var lang: String? = null

   @ColumnInfo(name = "liked", defaultValue = "0")
    var liked: Int = 0

    constructor()

    constructor(
        id: String?,
        channelImage: String?,
        channelName: String,
        channelCategory: String?,
        channelType: String?,
        channelLink: String?,
        channelDesc: String?,
        channelLanguage: String?
    ) {
        this.tvgId = id
        this.cover = channelImage
        this.name = channelName
        this.cat = channelCategory
        this.type = channelType
        this.lnk = channelLink
        this.desc = channelDesc
        this.lang = channelLanguage
    }

    constructor(
        url: String?,
        category: String?,
        title: String,
        cover: String?,
        playlistUrl: String?,
        originalId: String?
    ) {
        this.lnk = url
        this.cat = category
        this.name = title
        this.cover = cover
        this.lang = cover

        //        this.playlistUrl = playlistUrl;
        this.tvgId = originalId
    }
}
