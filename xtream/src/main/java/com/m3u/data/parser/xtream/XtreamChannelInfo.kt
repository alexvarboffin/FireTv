@file:Suppress("unused")

package com.m3u.data.parser.xtream

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.Serial

@Serializable
data class XtreamChannelInfo(
    @SerializedName("episodes")
    val episodes: Map<String, List<Episode>> = emptyMap(),
//    @SerializedName("info")
//    val info: Info?,
//    @SerializedName("seasons")
//    val seasons: List<String?> = emptyList()
) : java.io.Serializable {
    @Serializable
    data class Episode(
//        @SerializedName("added")
//        val added: String?,
        @SerializedName("container_extension")
        val containerExtension: String?,
//        @SerializedName("custom_sid")
//        val customSid: String?,
//        @SerializedName("direct_source")
//        val directSource: String?,
        @SerializedName("episode_num")
        val episodeNum: String?,
        @SerializedName("id")
        val id: String?,
//        @SerializedName("info")
//        val info: Info?,
//        @SerializedName("season")
//        val season: String?,
        @SerializedName("title")
        val title: String?
    ) {
        @Serializable
        data class Info(
            @SerializedName("audio")
            val audio: Audio?,
            @SerializedName("bitrate")
            val bitrate: String?,
            @SerializedName("duration")
            val duration: String?,
            @SerializedName("duration_secs")
            val durationSecs: String?,
            @SerializedName("video")
            val video: Video?
        ) {
            @Serializable
            data class Audio(
                @SerializedName("avg_frame_rate")
                val avgFrameRate: String?,
                @SerializedName("bits_per_sample")
                val bitsPerSample: String?,
                @SerializedName("channels")
                val channels: String?,
                @SerializedName("codec_long_name")
                val codecLongName: String?,
                @SerializedName("codec_name")
                val codecName: String?,
                @SerializedName("codec_tag")
                val codecTag: String?,
                @SerializedName("codec_tag_string")
                val codecTagString: String?,
                @SerializedName("codec_time_base")
                val codecTimeBase: String?,
                @SerializedName("codec_type")
                val codecType: String?,
                @SerializedName("disposition")
                val disposition: Disposition?,
                @SerializedName("dmix_mode")
                val dmixMode: String?,
                @SerializedName("index")
                val index: String?,
                @SerializedName("loro_cmixlev")
                val loroCmixlev: String?,
                @SerializedName("loro_surmixlev")
                val loroSurmixlev: String?,
                @SerializedName("ltrt_cmixlev")
                val ltrtCmixlev: String?,
                @SerializedName("ltrt_surmixlev")
                val ltrtSurmixlev: String?,
                @SerializedName("r_frame_rate")
                val rFrameRate: String?,
                @SerializedName("sample_fmt")
                val sampleFmt: String?,
                @SerializedName("sample_rate")
                val sampleRate: String?,
                @SerializedName("start_pts")
                val startPts: String?,
                @SerializedName("start_time")
                val startTime: String?,
                @SerializedName("tags")
                val tags: Map<String, String> = emptyMap(),
                @SerializedName("time_base")
                val timeBase: String?
            )

            @Serializable
            data class Video(
                @SerializedName("avg_frame_rate")
                val avgFrameRate: String?,
                @SerializedName("bits_per_raw_sample")
                val bitsPerRawSample: String?,
                @SerializedName("chroma_location")
                val chromaLocation: String?,
                @SerializedName("codec_long_name")
                val codecLongName: String?,
                @SerializedName("codec_name")
                val codecName: String?,
                @SerializedName("codec_tag")
                val codecTag: String?,
                @SerializedName("codec_tag_string")
                val codecTagString: String?,
                @SerializedName("codec_time_base")
                val codecTimeBase: String?,
                @SerializedName("codec_type")
                val codecType: String?,
                @SerializedName("coded_height")
                val codedHeight: String?,
                @SerializedName("coded_width")
                val codedWidth: String?,
                @SerializedName("display_aspect_ratio")
                val displayAspectRatio: String?,
                @SerializedName("disposition")
                val disposition: Disposition?,
                @SerializedName("field_order")
                val fieldOrder: String?,
                @SerializedName("has_b_frames")
                val hasBFrames: String?,
                @SerializedName("height")
                val height: String?,
                @SerializedName("index")
                val index: String?,
                @SerializedName("is_avc")
                val isAvc: Boolean = false,
                @SerializedName("level")
                val level: String?,
                @SerializedName("nal_length_size")
                val nalLengthSize: String?,
                @SerializedName("pix_fmt")
                val pixFmt: String?,
                @SerializedName("profile")
                val profile: String?,
                @SerializedName("r_frame_rate")
                val rFrameRate: String?,
                @SerializedName("refs")
                val refs: String?,
                @SerializedName("sample_aspect_ratio")
                val sampleAspectRatio: String?,
                @SerializedName("start_pts")
                val startPts: String?,
                @SerializedName("start_time")
                val startTime: String?,
                @SerializedName("tags")
                val tags: Map<String, String> = emptyMap(),
                @SerializedName("time_base")
                val timeBase: String?,
                @SerializedName("width")
                val width: String?
            )

            @Serializable
            data class Disposition(
                @SerializedName("attached_pic")
                val attachedPic: String?,
                @SerializedName("clean_effects")
                val cleanEffects: String?,
                @SerializedName("comment")
                val comment: String?,
                @SerializedName("default")
                val default: String?,
                @SerializedName("dub")
                val dub: String?,
                @SerializedName("forced")
                val forced: String?,
                @SerializedName("hearing_impaired")
                val hearingImpaired: String?,
                @SerializedName("karaoke")
                val karaoke: String?,
                @SerializedName("lyrics")
                val lyrics: String?,
                @SerializedName("original")
                val original: String?,
                @SerializedName("timed_thumbnails")
                val timedThumbnails: String?,
                @SerializedName("visual_impaired")
                val visualImpaired: String?
            )
        }
    }

    @Serializable
    data class Info(
        @SerializedName("backdrop_path")
        val backdropPath: List<String> = emptyList(),
        @SerializedName("cast")
        val cast: String?,
        @SerializedName("category_id")
        val categoryId: String?,
        @SerializedName("cover")
        val cover: String?,
        @SerializedName("director")
        val director: String?,
        @SerializedName("episode_run_time")
        val episodeRunTime: String?,
        @SerializedName("genre")
        val genre: String?,
        @SerializedName("last_modified")
        val lastModified: String?,
        @SerializedName("name")
        val name: String?,
        @SerializedName("plot")
        val plot: String?,
        @SerializedName("rating")
        val rating: String?,
        @SerializedName("rating_5based")
        val rating5based: String?,
        @SerializedName("releaseDate")
        val releaseDate: String?,
        @SerializedName("youtube_trailer")
        val youtubeTrailer: String?
    ) : java.io.Serializable
}