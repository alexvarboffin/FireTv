package tv.hdonlinetv.besttvchannels.movies.watchfree;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class NetworkUtils {


    private static final long TIMEOUT = 30;

    public static OkHttpClient makeOkhttp() {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS);
                //.addInterceptor(new SimpleLoggingInterceptor());
        return httpClientBuilder.build();

    }
}
