package com.walhalla.data.repository;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.m3u.data.database.model.ResponseData;
import com.m3u.data.parser.xtream.XtreamInput;
import com.m3u.data.parser.xtream.XtreamLive;
import com.m3u.data.parser.xtream.XtreamSerial;
import com.m3u.data.parser.xtream.XtreamVod;
import com.walhalla.data.model.Channel;
import com.walhalla.ui.DLog;
import com.walhalla.xtream.api.XtreamApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.player.PlrActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.playlist.SerialInfoActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.RetrofitClient;

public class XtreamPresenter extends BasePresenter {


    private final String liveContainerExtension;
    private final HttpUrl baseUrl;
    private final XtreamInput xtreamInput;
    private final XtreamApi xtreamApi;


    String[] allowedOutputFormats = new String[]{
            "m3u8", "ts", "rtmp"
    };


    public XtreamPresenter(Handler handler, Context context, XtreamInput input) {
        super(handler, context);
        this.xtreamInput = input;
        baseUrl = HttpUrl.parse(xtreamInput.getBasicUrl());
        DLog.d("@@@@@@@@@@@@" + baseUrl);
        Retrofit retrofit = RetrofitClient.getClient(xtreamInput.getBasicUrl());
        xtreamApi = retrofit.create(XtreamApi.class);
        // Мы предпочитаем ts, но не m3u8.
        liveContainerExtension = Arrays.asList(allowedOutputFormats).contains("ts")
                ? "ts"
                : (allowedOutputFormats.length > 0 ? allowedOutputFormats[0] : "ts");

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

    public void getSeriesStreams(XtreamInput input, RepoCallback<List<Channel>> callback) {
        executeInBackground(() -> {
//            try {
//                List<Channel> channels = db_repo.getFavorite(playlistId);
//                postToMainThread(() -> callback.successResult(channels));
//            } catch (Exception e) {
//                postToMainThread(() -> callback.errorResult(e.getMessage()));
//            }

            try {

                Call<List<XtreamSerial>> liveCall = xtreamApi.getSerialStreams(input.getUsername()
                        , input.getPassword(), Action.get_series);

                liveCall.enqueue(new Callback<List<XtreamSerial>>() {
                    @Override
                    public void onResponse(Call<List<XtreamSerial>> call, @NonNull Response<List<XtreamSerial>> response) {


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


                        List<Channel> channels = new ArrayList<>();

                        if (response.isSuccessful()) {
                            List<XtreamSerial> liveStreams = response.body();

                            // Обработка списка потоков
                            for (XtreamSerial stream : liveStreams) {
                                DLog.d("Icon: " + stream);

//                                XtreamDataKt.toChannel(
//                                        stream.url,
//                                        stream.category,
//                                        stream.title,
//                                        stream.xtreamLive.getStreamIcon(),
//                                        stream.playlistUrl,
//                                        stream.xtreamLive.getEpgChannelId()
//                                );
                                channels.add(new Channel(
                                        "stream.url",
                                        "stream.category",
                                        stream.getName(),
                                        stream.getCover(),
                                        "stream.playlistUrl",
                                        "" + stream.getSeriesId()
                                ));
                            }


                            postToMainThread(() -> callback.successResult(channels));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<XtreamSerial>> call, Throwable t) {
                        DLog.d(t.getMessage());
                        postToMainThread(() -> callback.errorResult(t.getMessage()));
                    }
                });

            } catch (Exception e) {
                postToMainThread(() -> callback.errorResult(e.getMessage()));
            }
        });
    }

    public void getLiveStreams(XtreamInput input, RepoCallback<List<Channel>> callback) {
        executeInBackground(() -> {
//            try {
//                List<Channel> channels = db_repo.getFavorite(input);
//                postToMainThread(() -> callback.successResult(channels));
//            } catch (Exception e) {
//                postToMainThread(() -> callback.errorResult(e.getMessage()));
//            }

            try {

                Call<List<XtreamLive>> liveCall = xtreamApi.getLiveStreams(
                        input.getUsername()
                        , input.getPassword()
                        , Action.get_live_streams);

                liveCall.enqueue(new Callback<List<XtreamLive>>() {
                    @Override
                    public void onResponse(Call<List<XtreamLive>> call, Response<List<XtreamLive>> response) {

                        List<Channel> channels = new ArrayList<>();

                        if (response.isSuccessful()) {
                            List<XtreamLive> liveStreams = response.body();

                            // Обработка списка потоков
                            for (XtreamLive stream : liveStreams) {
                                DLog.d("Icon: " + stream);

                                String url = buildLiveStreamUrl(
                                        baseUrl
                                        , xtreamInput.getUsername()
                                        , xtreamInput.getPassword()
                                        , "" + stream.getStreamId(),
                                        liveContainerExtension);

                                DLog.d(url);

//                                XtreamDataKt.toChannel(
//                                        stream.url,
//                                        stream.category,
//                                        stream.title,
//                                        stream.xtreamLive.getStreamIcon(),
//                                        stream.playlistUrl,
//                                        stream.xtreamLive.getEpgChannelId()
//                                );
                                channels.add(new Channel(
                                        url,
                                        "stream.category",
                                        stream.getName(),
                                        stream.getStreamIcon(),
                                        "stream.playlistUrl",
                                        stream.getEpgChannelId()
                                ));
                            }


                            postToMainThread(() -> callback.successResult(channels));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<XtreamLive>> call, Throwable t) {
                        DLog.d(t.getMessage());
                        postToMainThread(() -> callback.errorResult(t.getMessage()));
                    }
                });

            } catch (Exception e) {
                postToMainThread(() -> callback.errorResult(e.getMessage()));
            }
        });
    }

    //https://iptv.icsnleb.com:25463/player_api.php?username=12&password=12&xtream_type=vod
    public void getVodStreams(XtreamInput input, RepoCallback<List<Channel>> callback) {
        executeInBackground(() -> {
//            try {
//                List<Channel> channels = db_repo.getFavorite(playlistId);
//                postToMainThread(() -> callback.successResult(channels));
//            } catch (Exception e) {
//                postToMainThread(() -> callback.errorResult(e.getMessage()));
//            }
            DLog.d("@@@@@@@@okokok");
            try {
                DLog.d("@@@@@@@@okokok");
                Call<List<XtreamVod>> liveCall = xtreamApi.getVodStreams(input.getUsername()
                        , input.getPassword(),
                        Action.get_vod_streams);

                liveCall.enqueue(new Callback<List<XtreamVod>>() {
                    @Override
                    public void onResponse(Call<List<XtreamVod>> call, Response<List<XtreamVod>> response) {

                        List<Channel> channels = new ArrayList<>();

                        if (response.isSuccessful()) {
                            List<XtreamVod> liveStreams = response.body();
                            // Обработка списка потоков
                            for (XtreamVod stream : liveStreams) {
                                DLog.d("Icon: " + stream + "@@@" + stream.getContainerExtension());

                                String containerExtension = stream.getContainerExtension();
                                String url = buildVodStreamUrl(
                                        xtreamInput.getUsername()
                                        , xtreamInput.getPassword()
                                        , stream.getStreamId(), containerExtension);
                                DLog.d(url);

//                                XtreamDataKt.toChannel(
//                                        stream.url,
//                                        stream.category,
//                                        stream.title,
//                                        stream.xtreamLive.getStreamIcon(),
//                                        stream.playlistUrl,
//                                        stream.xtreamLive.getEpgChannelId()
//                                );
                                channels.add(new Channel(
                                        url,
                                        "stream.category",
                                        stream.getName(),
                                        stream.getStreamIcon(),
                                        "stream.playlistUrl",
                                        "" + stream.getStreamId()
                                ));
                            }


                            postToMainThread(() -> callback.successResult(channels));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<XtreamVod>> call, Throwable t) {
                        DLog.d(t.getMessage());
                        postToMainThread(() -> callback.errorResult(t.getMessage()));
                    }

                });

            } catch (Exception e) {
                postToMainThread(() -> callback.errorResult(e.getMessage()));
            }
        });
    }

    public static com.m3u.data.database.model.Channel toChannel(
            XtreamLive xtreamLive,
            String basicUrl,
            String username,
            String password,
            String playlistUrl,
            String category,
            String containerExtension
    ) {
        String url = basicUrl + "/live/" + username + "/" + password + "/" + xtreamLive.getStreamId() + "." + containerExtension;
        String title = xtreamLive.getName() != null ? xtreamLive.getName() : "";
        return new com.m3u.data.database.model.Channel(
                url,
                category,
                title,
                xtreamLive.getStreamIcon(),
                playlistUrl,
                xtreamLive.getEpgChannelId()
        );
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

    public String buildLiveStreamUrl(HttpUrl baseUrl, String username, String password,
                                     String streamId, String containerExtension) {

        HttpUrl url = new HttpUrl.Builder()
                .scheme(baseUrl.scheme())
                .host(baseUrl.host())
                .port(baseUrl.port())
                .addPathSegment("live")
                .addPathSegment(username)
                .addPathSegment(password)
                .addPathSegment(streamId + "." + containerExtension)
                .build();
        return url.toString();
    }

    public String buildVodStreamUrl(String username, String password, int streamId, String containerExtension) {

        HttpUrl url = new HttpUrl.Builder()
                .scheme(baseUrl.scheme())
                .host(baseUrl.host())
                .port(baseUrl.port())
                .addPathSegment("movie")
                .addPathSegment(username)
                .addPathSegment(password)
                .addPathSegment(streamId + "." + containerExtension)
                .build();
        return url.toString();
    }


    // http://{host}:{port}/series/{username}/{password}/{episode_id}.{episode_container_extension}

    public String buildEpisodeStreamUrl(XtreamInput input, ResponseData.Episode episode) {

        HttpUrl url = new HttpUrl.Builder()
                .scheme(baseUrl.scheme())
                .host(baseUrl.host())
                .port(baseUrl.port())
                .addPathSegment("series")
                .addPathSegment(input.getUsername())
                .addPathSegment(input.getPassword())
                .addPathSegment(episode.id + "." + episode.getContainerExtension())
                .build();
        return url.toString();
    }

    public void openSelectedEpisode(Context context, ResponseData.Episode episode) {
        String url = buildEpisodeStreamUrl(xtreamInput, episode);
        Channel channel = new Channel(
                url, "",
                episode.getTitle(),
                null,
                url,
                episode.getId()
        );
        Intent intents = PlrActivity.playerIntent(context, channel);
        context.startActivity(intents);
    }

    public void onStreamItemClick(@NonNull Channel channel, String action) {
        String streamId = channel.getTvgId();


        DLog.d("@@@@@@@@@" + action + "@" + channel.tvgUrl);
        //http://iptv.icsnleb.com:25461/movie/12/12/1238.mp4

        if (Action.get_series.equals(action)) {


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
            String seriesId = channel.getTvgId();
            Intent intents = SerialInfoActivity.newInstance(context, xtreamInput, Integer.parseInt(seriesId));
            context.startActivity(intents);
//        }
        } else if (Action.get_live_streams.equals(action)) {
            //url = "$basicUrl/live/$username/$password/$streamId.$containerExtension",
//        boolean isDetailsMode = prf.isDetailsMode();
//        if (isDetailsMode) {
//            long channelId = channel._id;
//            Intent intent = DetailsActivity.newInstance(this, channelId);
//            startActivity(intent);
//            showInterstitialAd();
//        } else {
            Intent intents = PlrActivity.playerIntent(context, channel);
            context.startActivity(intents);
//        }
        } else if (Action.get_vod_streams.equals(action)) {
//"$basicUrl/movie/$username/$password/$streamId.${containerExtension}"
            //        boolean isDetailsMode = prf.isDetailsMode();
//        if (isDetailsMode) {
//            long channelId = channel._id;
//            Intent intent = DetailsActivity.newInstance(this, channelId);
//            startActivity(intent);
//            showInterstitialAd();
//        } else {
            Intent intents = PlrActivity.playerIntent(context, channel);
            context.startActivity(intents);
//        }
        }

    }

    public void getSeriesInfo(int seriesId, RepoCallback<ResponseData> callback) {
        executeInBackground(() -> {
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
                Call<ResponseData> liveCall = xtreamApi.getSeriesInfo(xtreamInput.getUsername(), xtreamInput.getPassword()
                        , Action.GET_SERIES_INFO, seriesId);

                liveCall.enqueue(new Callback<ResponseData>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseData> call, @NonNull Response<ResponseData> response) {


                        if (response.isSuccessful() && response.body() != null) {
                            ResponseData responseData = response.body();
                            postToMainThread(() -> callback.successResult(responseData));
                        } else {
                            String errorMessage = "Error: " + response.code() + " - " + response.message();
                            DLog.d("@@@@ " + errorMessage);
                            postToMainThread(() -> callback.errorResult(errorMessage));
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseData> call, Throwable t) {
                        DLog.d("@@@@ ");
                        DLog.d(t.getMessage());
                        postToMainThread(() -> callback.errorResult(t.getMessage()));
                    }
                });

            } catch (Exception e) {
                postToMainThread(() -> callback.errorResult(e.getMessage()));
            }
        });
    }

    public static String createInfoUrl(
            String basicUrl,
            String username,
            String password,
            Pair<String, Object>... params
    ) {
        HttpUrl url = HttpUrl.parse(basicUrl);
        if (url == null) throw new IllegalArgumentException("Invalid URL");

        HttpUrl.Builder builder = new HttpUrl.Builder()
                .scheme(url.scheme())
                .host(url.host())
                .port(url.port())
                .addPathSegment("player_api.php")
                .addQueryParameter("username", username)
                .addQueryParameter("password", password);

        for (Pair<String, Object> param : params) {
            builder.addQueryParameter(param.first, param.second.toString());
        }

        return builder.build().toString();
    }

    public static String createActionUrl(
            String basicUrl,
            String username,
            String password,
            String action,
            Pair<String, Object>... params
    ) {
        return createInfoUrl(basicUrl, username, password, params) + "&action=" + action;
    }


    public static class Action {
        public static String get_series = "get_series";
        public static String get_live_streams = "get_live_streams";
        public static String get_vod_streams = "get_vod_streams";


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
        public static final String GET_SERIES_INFO = "get_series_info";
    }
}
