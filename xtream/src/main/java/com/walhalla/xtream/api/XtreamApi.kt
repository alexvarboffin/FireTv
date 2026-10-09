package com.walhalla.xtream.api

import com.m3u.data.database.model.ResponseData
import com.m3u.data.parser.xtream.XtreamInfo
import com.m3u.data.parser.xtream.XtreamLive
import com.m3u.data.parser.xtream.XtreamSerial
import com.m3u.data.parser.xtream.XtreamVod
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface XtreamApi {
    // Получение информации о пользователе
    @GET("player_api.php")
    fun getUserInfo(
        @Query("username") username: String?,
        @Query("password") password: String?
    ): Call<XtreamInfo>

    // Получение списка Live TV каналов
    @GET("player_api.php")
    fun getLiveStreams(
        @Query("username") username: String?,
        @Query("password") password: String?,
        @Query("action") action: String?
    ): Call<List<XtreamLive>>

    // Получение списка VOD
    @GET("player_api.php")
    fun getVodStreams(
        @Query("username") username: String?,
        @Query("password") password: String?,
        @Query("action") action: String?
    ): Call<List<XtreamVod>>

    @GET("player_api.php")
    fun getSerialStreams(
        @Query("username") username: String?,
        @Query("password") password: String?,
        @Query("action") action: String?
    ): Call<List<XtreamSerial>>

    //Serials info
    @GET("player_api.php")
    fun getSeriesInfo(
        @Query("username") username: String?,
        @Query("password") password: String?,
        @Query("action") action: String?,
        @Query("series_id") seriesId: Int
    ): Call<ResponseData>


    //action=get_short_epg
    //&limit=1
    @GET("player_api.php?action=get_short_epg&limit=1")
    fun getEpg(
        @Query("username") username: String?,
        @Query("password") password: String?,
        @Query("stream_id") streamId: Int
    ): Call<ResponseData>
}

