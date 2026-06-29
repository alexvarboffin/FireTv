package com.m3u.data.database.model

import com.google.gson.annotations.SerializedName
import com.m3u.data.parser.xtream.XtreamChannelInfo
import java.io.Serializable

class ResponseData : Serializable {
    @SerializedName("seasons")
    var seasons: ArrayList<Season?>? = null

    @SerializedName("info")
    var info: XtreamChannelInfo.Info? = null

    @SerializedName("episodes")
    var episodes: MutableMap<String?, MutableList<Episode?>?>? = null


    class Season : Serializable {
        @SerializedName("air_date")
        var airDate: String? = null

        @SerializedName("episode_count")
        var episodeCount: Int = 0

        @SerializedName("id")
        var id: Int = 0

        @SerializedName("name")
        var name: String? = null

        @SerializedName("overview")
        var overview: String? = null

        @SerializedName("season_number")
        var seasonNumber: Int = 0

        @SerializedName("vote_average")
        var voteAverage: Double = 0.0

        @SerializedName("cover")
        var cover: String? = null

        @SerializedName("cover_big")
        var coverBig: String? = null // Getters and Setters
        // ... (аналогично другим полям)
    }


    //    public static class Info {
    //        @SerializedName("name")
    //        private String name;
    //
    //        @SerializedName("cover")
    //        private String cover;
    //
    //        @SerializedName("plot")
    //        private String plot;
    //
    //        @SerializedName("cast")
    //        private String cast;
    //
    //        @SerializedName("director")
    //        private String director;
    //
    //        @SerializedName("genre")
    //        private String genre;
    //
    //        @SerializedName("releaseDate")
    //        private String releaseDate;
    //
    //        @SerializedName("last_modified")
    //        private String lastModified;
    //
    //        @SerializedName("rating")
    //        private String rating;
    //
    //        @SerializedName("rating_5based")
    //        private int rating5Based;
    //
    //        @SerializedName("backdrop_path")
    //        private List<String> backdropPath;
    //
    //        @SerializedName("youtube_trailer")
    //        private String youtubeTrailer;
    //
    //        @SerializedName("episode_run_time")
    //        private String episodeRunTime;
    //
    //        @SerializedName("category_id")
    //        private String categoryId;
    //
    //        // Getters and Setters
    //
    //        // ... (аналогично другим полям)
    //    }
    class Episode : Serializable {
        @SerializedName("id")
        var id: String? = null

        
        @SerializedName("episode_num")
        var episodeNum: Int = 0

        
        @SerializedName("title")
        var title: String? = null

        @SerializedName("container_extension")
        var containerExtension: String? = null

        
        @SerializedName("info")
        var info: EpisodeInfo? = null

        @SerializedName("custom_sid")
        var customSid: String? = null

        @SerializedName("added")
        var added: String? = null

        @SerializedName("season")
        var season: Int = 0

        @SerializedName("direct_source")
        var directSource: String? = null
    }

    class EpisodeInfo {
        //        @SerializedName("releasedate")
        //        private String releaseDate;
        //
        @SerializedName("plot")
        var plot: String? = null

        //
        //        @SerializedName("duration_secs")
        //        private int durationSecs;
        //
        //        @SerializedName("duration")
        //        private String duration;
        //
        //        @SerializedName("video")
        //        private List<String> video;
        //
        //        @SerializedName("audio")
        //        private List<String> audio;
        //
        //        @SerializedName("bitrate")
        //        private int bitrate;
        //
        //        @SerializedName("rating")
        //        private String rating;
        //
        //        @SerializedName("season")
        //        private String season;
        //
        //        @SerializedName("tmdb_id")
        //        private String tmdbId;
        //
        //
        //        //
        
        @SerializedName("releasedate")
        var releasedate: String? = null


        
        @SerializedName("movie_image")
        var movieImage: String? = null
    }
}
