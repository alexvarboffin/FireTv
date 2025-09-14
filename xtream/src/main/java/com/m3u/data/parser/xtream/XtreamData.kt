package com.m3u.data.parser.xtream

import com.google.gson.annotations.SerializedName
import com.m3u.data.database.model.Channel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface XtreamData

@Serializable
data class XtreamLive(
//    @SerializedName("added")
//    val added: String?,
    @SerializedName("category_id")
    val categoryId: Int?,
//    @SerializedName("custom_sid")
//    val customSid: String?,
//    @SerializedName("direct_source")
//    val directSource: String?,
    @SerializedName("epg_channel_id")
    val epgChannelId: String?,
    @SerializedName("name")
    val name: String?,
//    @SerializedName("num")
//    val num: Int?,
    @SerializedName("stream_icon")
    val streamIcon: String?,
    @SerializedName("stream_id")
    val streamId: Int?,
    @SerializedName("stream_type")
    val streamType: String?,
//    @SerializedName("tv_archive")
//    val tvArchive: Int?,
//    @SerializedName("tv_archive_duration")
//    val tvArchiveDuration: Int?,
) : XtreamData

@Serializable
data class XtreamVod(
//    @SerializedName("added")
//    val added: String? = null,
    @SerializedName("category_id")
    val categoryId: Int? = null,
    @SerializedName("container_extension")
    val containerExtension: String? = null,
//    @SerializedName("custom_sid")
//    val customSid: String? = null,
//    @SerializedName("direct_source")
//    val directSource: String? = null,
    @SerializedName("name")
    val name: String? = null,
//    @SerializedName("num")
//    val num: String? = null,
//    @SerializedName("rating")
//    val rating: String? = null,
//    @SerializedName("rating_5based")
//    val rating5based: String? = null,
    @SerializedName("stream_icon")
    val streamIcon: String? = null,
    @SerializedName("stream_id")
    val streamId: Int? = null,
    @SerializedName("stream_type")
    val streamType: String? = null
) : XtreamData

@Serializable
data class XtreamSerial(
//    @SerializedName("cast")
//    val cast: String? = null,
    @SerializedName("category_id")
    val categoryId: Int? = null,
    @SerializedName("cover")
    val cover: String? = null,
//    @SerializedName("director")
//    val director: String? = null,
//    @SerializedName("episode_run_time")
//    val episodeRunTime: String? = null,
//    @SerializedName("genre")
//    val genre: String? = null,
//    @SerializedName("last_modified")
//    val lastModified: String? = null,
    @SerializedName("name")
    val name: String? = null,
//    @SerializedName("num")
//    val num: String? = null,
//    @SerializedName("plot")
//    val plot: String? = null,
//    @SerializedName("rating")
//    val rating: String? = null,
//    @SerializedName("rating_5based")
//    val rating5based: String? = null,
//    @SerializedName("releaseDate")
//    val releaseDate: String? = null,
    @SerializedName("series_id")
    val seriesId: Int? = null,
//    @SerializedName("youtube_trailer")
//    val youtubeTrailer: String? = null
) : XtreamData

//fun XtreamLive.toChannel(
//    basicUrl: String,
//    username: String,
//    password: String,
//    playlistUrl: String,
//    category: String,
//    // one of "allowed_output_formats"
//    containerExtension: String
//): Channel = Channel(
//    url = "$basicUrl/live/$username/$password/$streamId.$containerExtension",
//    category = category,
//    title = name.orEmpty(),
//    cover = streamIcon,
//    playlistUrl = playlistUrl,
//    originalId = epgChannelId
//)
//
//fun XtreamVod.toChannel(
//    basicUrl: String,
//    username: String,
//    password: String,
//    playlistUrl: String,
//    category: String
//): Channel = Channel(
//    url = "$basicUrl/movie/$username/$password/$streamId.${containerExtension}",
//    category = category,
//    title = name.orEmpty(),
//    cover = streamIcon,
//    playlistUrl = playlistUrl,
//    originalId = streamId?.toString()
//)
//
//fun XtreamSerial.asChannel(
//    basicUrl: String,
//    username: String,
//    password: String,
//    playlistUrl: String,
//    category: String
//): Channel = Channel(
//    url = "$basicUrl/series/$username/$password/$seriesId",
//    category = category,
//    title = name.orEmpty(),
//    cover = cover,
//    playlistUrl = playlistUrl,
//    originalId = seriesId?.toString()
//)
