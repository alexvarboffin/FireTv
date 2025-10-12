package com.walhalla.data.repository

import android.content.Context
import android.os.Handler
import android.util.Pair
import com.m3u.data.database.model.ResponseData
import com.m3u.data.parser.xtream.XtreamInput
import com.m3u.data.parser.xtream.XtreamLive
import com.m3u.data.parser.xtream.XtreamSerial
import com.m3u.data.parser.xtream.XtreamVod
import com.walhalla.data.model.Channel
import com.walhalla.ui.DLog.d
import com.walhalla.xtream.api.XtreamApi
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.player.PlrActivity.Companion.playerIntent
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.playlist.SerialInfoActivity.Companion.newInstance
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.RetrofitClient
import java.util.Arrays

class XtreamPresenter(handler: Handler, context: Context, private val xtreamInput: XtreamInput) :
    BasePresenter(handler, context) {
    private val liveContainerExtension: String?
    private val baseUrl: HttpUrl?
    private val xtreamApi: XtreamApi


    var allowedOutputFormats: Array<String> = arrayOf<String>(
        "m3u8", "ts", "rtmp"
    )


    init {
        baseUrl = xtreamInput.basicUrl.toHttpUrlOrNull()
        d("@@@@@@@@@@@@$baseUrl")
        val retrofit = RetrofitClient.getClient(xtreamInput.basicUrl)
        xtreamApi = retrofit.create<XtreamApi>(XtreamApi::class.java)
        // Мы предпочитаем ts, но не m3u8.
        liveContainerExtension = if (Arrays.asList<String?>(*allowedOutputFormats).contains("ts"))
            "ts"
        else
            (if (allowedOutputFormats.size > 0) allowedOutputFormats[0] else "ts")
    }

    //    public void getXStreams(long playlistId, String action, RepoCallback<List<Channel>> callback) {
    //        if (action.equals(StreamType.LIVE.getAction())) {
    //            getLiveStreams(playlistId, callback);
    //        } else if (action.equals(StreamType.VOD.getAction())) {
    //            getVodStreams(playlistId, callback);
    //        } else if (action.equals(StreamType.SERIES.getAction())) {
    //            getSeriesStreams(playlistId, callback);
    //        }
    //    }
    fun getSeriesStreams(input: XtreamInput, callback: RepoCallback<List<Channel>>) {
        executeInBackground(Runnable {
//            try {
//                List<Channel> channels = db_repo.getFavorite(playlistId);
//                postToMainThread(() -> callback.successResult(channels));
//            } catch (Exception e) {
//                postToMainThread(() -> callback.errorResult(e.getMessage()));
//            }
            try {
                val liveCall = xtreamApi.getSerialStreams(
                    input.username,
                    input.password, Action.get_series
                )

                liveCall.enqueue(object : Callback<MutableList<XtreamSerial>> {
                    override fun onResponse(
                        call: Call<MutableList<XtreamSerial>>,
                        response: Response<MutableList<XtreamSerial>>
                    ) {
                        //                        0
//                        num	1
//                        name	"Al Hayba"
//                        series_id	2
//                        cover	"http://iptv.icsnleb.com:25461/images/233c44d2342ef3f757f372876fde8d7a.jpg"
//                        plot	"In a village by the Lebanon-Syria border, the head of an arms-smuggling clan contends with family conflicts, power struggles and complicated love."
//                        cast	"Tim Hassan, Aimee Sayah, Mona Wassef, Owais Mukhalalti, Abdo Shahin"
//                        director	"Samer Al Barkawi"
//                        genre	"Drama, Action & Adventure"
//                        releaseDate	"2017-05-27"
//                        last_modified	"1713416461"
//                        rating	"6"
//                        rating_5based	3
//                        backdrop_path
//                        0	"http://iptv.icsnleb.com:25461/images/b9e9a45ed53af7521e5609e801db5bdd.jpg"
//                        youtube_trailer	""
//                        episode_run_time	"45"
//                        category_id	"4"


                        val channels: MutableList<Channel> = ArrayList<Channel>()

                        if (response.isSuccessful) {
                            val liveStreams = response.body()

                            // Обработка списка потоков
                            for (stream in liveStreams!!) {
                                d("Icon: " + stream)

                                //                                XtreamDataKt.toChannel(
//                                        stream.url,
//                                        stream.category,
//                                        stream.title,
//                                        stream.xtreamLive.getStreamIcon(),
//                                        stream.playlistUrl,
//                                        stream.xtreamLive.getEpgChannelId()
//                                );
                                channels.add(
                                    Channel(
                                        "stream.url",
                                        "stream.category",
                                        stream.name!!,
                                        stream.cover,
                                        "stream.playlistUrl",
                                        "" + stream.seriesId
                                    )
                                )
                            }


                            postToMainThread(Runnable { callback.successResult(channels) })
                        }
                    }

                    override fun onFailure(call: Call<MutableList<XtreamSerial>>, t: Throwable) {
                        d(t.message)
                        postToMainThread(Runnable { callback.errorResult(t.message!!) })
                    }
                })
            } catch (e: Exception) {
                postToMainThread(Runnable { callback.errorResult(e.message!!) })
            }
        })
    }

    fun getLiveStreams(input: XtreamInput, callback: RepoCallback<List<Channel>>) {
        executeInBackground(Runnable {
//            try {
//                List<Channel> channels = db_repo.getFavorite(input);
//                postToMainThread(() -> callback.successResult(channels));
//            } catch (Exception e) {
//                postToMainThread(() -> callback.errorResult(e.getMessage()));
//            }
            try {
                val liveCall = xtreamApi.getLiveStreams(
                    input.username,
                    input.password,
                    Action.get_live_streams
                )

                liveCall.enqueue(object : Callback<MutableList<XtreamLive>> {
                    override fun onResponse(
                        call: Call<MutableList<XtreamLive>>,
                        response: Response<MutableList<XtreamLive>>
                    ) {
                        val channels: MutableList<Channel> = ArrayList<Channel>()

                        if (response.isSuccessful()) {
                            val liveStreams = response.body()

                            // Обработка списка потоков
                            for (stream in liveStreams!!) {
                                d("Icon: " + stream)

                                val url = buildLiveStreamUrl(
                                    baseUrl!!,
                                    xtreamInput.username,
                                    xtreamInput.password,
                                    "" + stream.streamId,
                                    liveContainerExtension
                                )

                                d(url)

                                //                                XtreamDataKt.toChannel(
//                                        stream.url,
//                                        stream.category,
//                                        stream.title,
//                                        stream.xtreamLive.getStreamIcon(),
//                                        stream.playlistUrl,
//                                        stream.xtreamLive.getEpgChannelId()
//                                );
                                channels.add(
                                    Channel(
                                        url,
                                        "stream.category",
                                        stream.name!!,
                                        stream.streamIcon,
                                        "stream.playlistUrl",
                                        stream.epgChannelId
                                    )
                                )
                            }


                            postToMainThread(Runnable { callback.successResult(channels) })
                        }
                    }

                    override fun onFailure(call: Call<MutableList<XtreamLive>>, t: Throwable) {
                        d(t.message)
                        postToMainThread(Runnable { callback.errorResult(t.message!!) })
                    }
                })
            } catch (e: Exception) {
                postToMainThread { callback.errorResult(e.message!!) }
            }
        })
    }

    //https://iptv.icsnleb.com:25463/player_api.php?username=12&password=12&xtream_type=vod
    fun getVodStreams(input: XtreamInput, callback: RepoCallback<List<Channel>>) {
        executeInBackground(Runnable {
//            try {
//                List<Channel> channels = db_repo.getFavorite(playlistId);
//                postToMainThread(() -> callback.successResult(channels));
//            } catch (Exception e) {
//                postToMainThread(() -> callback.errorResult(e.getMessage()));
//            }
            d("@@@@@@@@okokok")
            try {
                d("@@@@@@@@okokok")
                val liveCall = xtreamApi.getVodStreams(
                    input.username,
                    input.password,
                    Action.get_vod_streams
                )

                liveCall.enqueue(object : Callback<MutableList<XtreamVod>> {
                    override fun onResponse(call: Call<MutableList<XtreamVod>>, response: Response<MutableList<XtreamVod>>
                    ) {
                        val channels: MutableList<Channel> = ArrayList()

                        if (response.isSuccessful) {
                            val liveStreams = response.body()
                            // Обработка списка потоков
                            for (stream in liveStreams!!) {
                                d("Icon: " + stream + "@@@" + stream.containerExtension)

                                val containerExtension = stream.containerExtension
                                val url = buildVodStreamUrl(
                                    xtreamInput.username,
                                    xtreamInput.password,
                                    stream.streamId!!, containerExtension
                                )
                                d(url)

                                //                                XtreamDataKt.toChannel(
//                                        stream.url,
//                                        stream.category,
//                                        stream.title,
//                                        stream.xtreamLive.getStreamIcon(),
//                                        stream.playlistUrl,
//                                        stream.xtreamLive.getEpgChannelId()
//                                );
                                channels.add(
                                    Channel(
                                        url,
                                        "stream.category",
                                        stream.name!!,
                                        stream.streamIcon,
                                        "stream.playlistUrl",
                                        "" + stream.streamId
                                    )
                                )
                            }


                            postToMainThread(Runnable { callback.successResult(channels) })
                        }
                    }

                    override fun onFailure(call: Call<MutableList<XtreamVod>>, t: Throwable) {
                        d(t.message)
                        postToMainThread(Runnable { callback.errorResult(t.message!!) })
                    }
                })
            } catch (e: Exception) {
                postToMainThread(Runnable { callback.errorResult(e.message!!) })
            }
        })
    }

    //    public String buildLiveStreamUrl(String baseUrlRaw, String username, String password,
    //                                     String streamId, String containerExtension) {
    //        HttpUrl baseUrl = HttpUrl.parse(baseUrlRaw);
    //        HttpUrl url = new HttpUrl.Builder()
    //                .scheme(baseUrl.scheme())
    //                .host(baseUrl.host())
    //                .port(baseUrl.port())
    //                .addPathSegment("live")
    //                .addPathSegment(username)
    //                .addPathSegment(password)
    //                .addPathSegment(streamId + "." + containerExtension)
    //                .build();
    //        return url.toString();
    //    }
    fun buildLiveStreamUrl(
        baseUrl: HttpUrl, username: String, password: String,
        streamId: String?, containerExtension: String?
    ): String {
        val url = HttpUrl.Builder()
            .scheme(baseUrl.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .addPathSegment("live")
            .addPathSegment(username)
            .addPathSegment(password)
            .addPathSegment(streamId + "." + containerExtension)
            .build()
        return url.toString()
    }

    fun buildVodStreamUrl(
        username: String,
        password: String,
        streamId: Int,
        containerExtension: String?
    ): String {
        val url = HttpUrl.Builder()
            .scheme(baseUrl!!.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .addPathSegment("movie")
            .addPathSegment(username)
            .addPathSegment(password)
            .addPathSegment("$streamId.$containerExtension")
            .build()
        return url.toString()
    }


    // http://{host}:{port}/series/{username}/{password}/{episode_id}.{episode_container_extension}
    fun buildEpisodeStreamUrl(input: XtreamInput, episode: ResponseData.Episode): String {
        val url = HttpUrl.Builder()
            .scheme(baseUrl!!.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .addPathSegment("series")
            .addPathSegment(input.username)
            .addPathSegment(input.password)
            .addPathSegment(episode.id + "." + episode.getContainerExtension())
            .build()
        return url.toString()
    }

    fun openSelectedEpisode(context: Context, episode: ResponseData.Episode) {
        val url = buildEpisodeStreamUrl(xtreamInput, episode)
        val channel = Channel(
            url, "",
            episode.getTitle(),
            null,
            url,
            episode.getId()
        )
        val intents = playerIntent(context, channel)
        context.startActivity(intents)
    }

    fun onStreamItemClick(channel: Channel, action: String?) {
        val streamId = channel.tvgId


        d("@@@@@@@@@" + action + "@" + channel.tvgUrl)

        //http://iptv.icsnleb.com:25461/movie/12/12/1238.mp4
        if (Action.get_series == action) {
            //http://iptv.icsnleb.com:25461/player_api.php?username=12&password=12&series_id=5&action=get_series_info
            //http://iptv.icsnleb.com:25461/player_api.php?username=12&password=12&action=get_series&category_id=4

            //http://iptv.icsnleb.com:25461/player_api.php?username=12&password=12&series_id=51&action=get_series_info

            //"$basicUrl/series/$username/$password/$seriesId",

//        boolean isDetailsMode = prf.isDetailsMode();
//        if (isDetailsMode) {
//            long channelId = channel._id;
//            Intent intent = DetailsActivity.newInstance(this, channelId);
//            startActivity(intent);
//            showInterstitialAd();
//        } else {


            val seriesId = channel.tvgId
            val intents = newInstance(context, xtreamInput, seriesId!!.toInt())
            context.startActivity(intents)
            //        }
        } else if (Action.get_live_streams == action) {
            //url = "$basicUrl/live/$username/$password/$streamId.$containerExtension",
//        boolean isDetailsMode = prf.isDetailsMode();
//        if (isDetailsMode) {
//            long channelId = channel._id;
//            Intent intent = DetailsActivity.newInstance(this, channelId);
//            startActivity(intent);
//            showInterstitialAd();
//        } else {
            val intents = playerIntent(context, channel)
            context.startActivity(intents)
            //        }
        } else if (Action.get_vod_streams == action) {
//"$basicUrl/movie/$username/$password/$streamId.${containerExtension}"
            //        boolean isDetailsMode = prf.isDetailsMode();
//        if (isDetailsMode) {
//            long channelId = channel._id;
//            Intent intent = DetailsActivity.newInstance(this, channelId);
//            startActivity(intent);
//            showInterstitialAd();
//        } else {
            val intents = playerIntent(context, channel)
            context.startActivity(intents)
            //        }
        }
    }

    fun getSeriesInfo(seriesId: Int, callback: RepoCallback<ResponseData>) {
        executeInBackground(Runnable {
//            Pair<String, Object> mm = new Pair<>(XtreamParser.GET_SERIES_INFO_PARAM_ID, seriesId);
//            String ll = createActionUrl(
//                    baseUrl.toString(),
//                    xtreamInput.getUsername(),
//                    xtreamInput.getPassword(),
//                    Action.GET_SERIES_INFO,
//                    mm
//            );

//            try {
//                List<Channel> channels = db_repo.getFavorite(input);
//                postToMainThread(() -> callback.successResult(channels));
//            } catch (Exception e) {
//                postToMainThread(() -> callback.errorResult(e.getMessage()));
//            }
            try {
                val liveCall = xtreamApi.getSeriesInfo(
                    xtreamInput.username, xtreamInput.password,
                    Action.GET_SERIES_INFO, seriesId
                )

                liveCall.enqueue(object : Callback<ResponseData> {
                    override fun onResponse(
                        call: Call<ResponseData?>,
                        response: Response<ResponseData>
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            val responseData: ResponseData = response.body()!!
                            postToMainThread(Runnable { callback.successResult(responseData) })
                        } else {
                            val errorMessage =
                                "Error: " + response.code() + " - " + response.message()
                            d("@@@@ " + errorMessage)
                            postToMainThread(Runnable { callback.errorResult(errorMessage) })
                        }
                    }

                    override fun onFailure(call: Call<ResponseData?>, t: Throwable) {
                        d("@@@@ ")
                        d(t.message)
                        postToMainThread(Runnable { callback.errorResult(t.message!!) })
                    }
                })
            } catch (e: Exception) {
                postToMainThread(Runnable { callback.errorResult(e.message!!) })
            }
        })
    }

    object Action {
        @JvmField
        var get_series: String = "get_series"
        @JvmField
        var get_live_streams: String = "get_live_streams"
        @JvmField
        var get_vod_streams: String = "get_vod_streams"


        //        val GET_LIVE_CATEGORIES = Action("get_live_categories")
        //        val GET_VOD_CATEGORIES = Action("get_vod_categories")
        //        val GET_SERIES_CATEGORIES = Action("get_series_categories")
        //
        //        val GET_VOD_INFO = Action("get_vod_info")
        //
        //        // series episode url
        //        http://iptv.icsnleb.com:25461/player_api.php?username=12&password=12&action=series&series_id=5
        //        http://iptv.icsnleb.com:25461/live/12/12/1034.ts
        //        http://iptv.icsnleb.com:25461/movie/12/12/1238.mp4
        //        http://iptv.icsnleb.com:25461/series/12/12/14.ts
        //        // http://{host}:{port}/series/{username}/{password}/{episode_id}.{episode_container_extension}
        const val GET_SERIES_INFO: String = "get_series_info"
    }

    companion object {
        fun toChannel(
            xtreamLive: XtreamLive,
            basicUrl: String?,
            username: String?,
            password: String?,
            playlistUrl: String?,
            category: String?,
            containerExtension: String?
        ): com.m3u.data.database.model.Channel {
            val url =
                basicUrl + "/live/" + username + "/" + password + "/" + xtreamLive.streamId + "." + containerExtension
            val title = if (xtreamLive.name != null) xtreamLive.name else ""
            return com.m3u.data.database.model.Channel(
                url,
                category,
                title,
                xtreamLive.streamIcon,
                playlistUrl,
                xtreamLive.epgChannelId
            )
        }

        fun createInfoUrl(
            basicUrl: String,
            username: String?,
            password: String?,
            vararg params: Pair<String?, Any?>
        ): String {
            val url = basicUrl.toHttpUrlOrNull()
            requireNotNull(url) { "Invalid URL" }

            val builder = HttpUrl.Builder()
                .scheme(url.scheme)
                .host(url.host)
                .port(url.port)
                .addPathSegment("player_api.php")
                .addQueryParameter("username", username)
                .addQueryParameter("password", password)

            for (param in params) {
                builder.addQueryParameter(param.first!!, param.second.toString())
            }

            return builder.build().toString()
        }

        fun createActionUrl(
            basicUrl: String,
            username: String?,
            password: String?,
            action: String?,
            vararg params: Pair<String?, Any?>
        ): String {
            return createInfoUrl(basicUrl, username, password, *params) + "&action=" + action
        }
    }
}
