package tv.hdonlinetv.besttvchannels.movies.watchfree

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object NetworkUtils {
    private const val TIMEOUT: Long = 30

    fun makeOkhttp(): OkHttpClient {
        val httpClientBuilder = OkHttpClient.Builder()
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
        //.addInterceptor(new SimpleLoggingInterceptor());
        return httpClientBuilder.build()
    }
}
