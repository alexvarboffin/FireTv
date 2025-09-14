package com.m3u.data.database.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.m3u.data.parser.xtream.XtreamChannelInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResponseData implements Serializable{

    @SerializedName("seasons")
    public ArrayList<Season> seasons;

    @SerializedName("info")
    public XtreamChannelInfo.Info info;

    @SerializedName("episodes")
    public Map<String, List<Episode>> episodes;


    public static class Season implements Serializable {
        @SerializedName("air_date")
        private String airDate;

        @SerializedName("episode_count")
        private int episodeCount;

        @SerializedName("id")
        private int id;

        public String getAirDate() {
            return airDate;
        }

        public void setAirDate(String airDate) {
            this.airDate = airDate;
        }

        public int getEpisodeCount() {
            return episodeCount;
        }

        public void setEpisodeCount(int episodeCount) {
            this.episodeCount = episodeCount;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getOverview() {
            return overview;
        }

        public void setOverview(String overview) {
            this.overview = overview;
        }

        public int getSeasonNumber() {
            return seasonNumber;
        }

        public void setSeasonNumber(int seasonNumber) {
            this.seasonNumber = seasonNumber;
        }

        public double getVoteAverage() {
            return voteAverage;
        }

        public void setVoteAverage(double voteAverage) {
            this.voteAverage = voteAverage;
        }

        public String getCover() {
            return cover;
        }

        public void setCover(String cover) {
            this.cover = cover;
        }

        public String getCoverBig() {
            return coverBig;
        }

        public void setCoverBig(String coverBig) {
            this.coverBig = coverBig;
        }

        @SerializedName("name")
        private String name;

        @SerializedName("overview")
        private String overview;

        @SerializedName("season_number")
        private int seasonNumber;

        @SerializedName("vote_average")
        private double voteAverage;

        @SerializedName("cover")
        private String cover;

        @SerializedName("cover_big")
        private String coverBig;

        // Getters and Setters

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


    public static class Episode implements Serializable{
        @SerializedName("id")

        public String id;
        @SerializedName("episode_num")

        public int episodeNum;
        @SerializedName("title")

        public String title;
        @SerializedName("container_extension")

        public String containerExtension;

        @SerializedName("info")
        public EpisodeInfo info;

        @SerializedName("custom_sid")

        public String customSid;
        @SerializedName("added")

        public String added;

        @SerializedName("season")
        public int season;

        @SerializedName("direct_source")
        public String directSource;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getEpisodeNum() {
            return episodeNum;
        }

        public void setEpisodeNum(int episodeNum) {
            this.episodeNum = episodeNum;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContainerExtension() {
            return containerExtension;
        }

        public void setContainerExtension(String containerExtension) {
            this.containerExtension = containerExtension;
        }

        public String getCustomSid() {
            return customSid;
        }

        public void setCustomSid(String customSid) {
            this.customSid = customSid;
        }

        public String getAdded() {
            return added;
        }

        public void setAdded(String added) {
            this.added = added;
        }

        public int getSeason() {
            return season;
        }

        public void setSeason(int season) {
            this.season = season;
        }

        public String getDirectSource() {
            return directSource;
        }

        public void setDirectSource(String directSource) {
            this.directSource = directSource;
        }
    }

    public static class EpisodeInfo {
//        @SerializedName("releasedate")
//        private String releaseDate;
//
        @SerializedName("plot")
        public String plot;
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
        public String releasedate;


        @SerializedName("movie_image")
        public String movieImage;
    }
}
