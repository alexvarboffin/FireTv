package com.walhalla.xtream.api;

import com.m3u.data.database.model.ResponseData;
import com.m3u.data.parser.xtream.XtreamInfo;
import com.m3u.data.parser.xtream.XtreamLive;
import com.m3u.data.parser.xtream.XtreamSerial;
import com.m3u.data.parser.xtream.XtreamVod;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface XtreamApi {
    // Получение информации о пользователе
    @GET("player_api.php")
    Call<XtreamInfo> getUserInfo(
            @Query("username") String username,
            @Query("password") String password
    );

    // Получение списка Live TV каналов
    @GET("player_api.php")
    Call<List<XtreamLive>> getLiveStreams(
            @Query("username") String username,
            @Query("password") String password,
            @Query("action") String action
    );

    // Получение списка VOD
    @GET("player_api.php")
    Call<List<XtreamVod>> getVodStreams(
            @Query("username") String username,
            @Query("password") String password,
            @Query("action") String action
    );

    @GET("player_api.php")
    Call<List<XtreamSerial>> getSerialStreams(
            @Query("username") String username,
            @Query("password") String password,
            @Query("action") String action
    );

    //Serials info
    @GET("player_api.php")
    Call<ResponseData> getSeriesInfo(
            @Query("username") String username,
            @Query("password") String password,
            @Query("action") String action,
            @Query("series_id") int seriesId
    );


    //action=get_short_epg
    //&limit=1
    @GET("player_api.php?action=get_short_epg&limit=1")
    Call<ResponseData> getEpg(
            @Query("username") String username,
            @Query("password") String password,
            @Query("stream_id") int stream_id
    );

}

