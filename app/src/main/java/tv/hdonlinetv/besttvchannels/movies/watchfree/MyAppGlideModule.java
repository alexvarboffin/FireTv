package tv.hdonlinetv.besttvchannels.movies.watchfree;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

@GlideModule
public final class MyAppGlideModule extends AppGlideModule {
    TrustManager[] trustAll = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
            }
    };

    @Override
    public void applyOptions(@NonNull Context context, GlideBuilder builder) {
        // Glide default Bitmap Format is set to RGB_565 since it
        // consumed just 50% memory footprint compared to ARGB_8888.
        // Increase memory usage for quality with:
        int diskCacheSizeBytes = 1024 * 1024 * 100; // 100 MB
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
        builder.setDefaultRequestOptions(
                new RequestOptions()
                        .format(DecodeFormat.PREFER_ARGB_8888));
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {

//        SSLSocketFactory sslSocketFactory = null;
//        try {
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(null, trustAll, new java.security.SecureRandom());
//            sslSocketFactory = sslContext.getSocketFactory();
//        } catch (NoSuchAlgorithmException | KeyManagementException e) {
//            DLog.handleException(e);
//        }


        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS);

//        if (sslSocketFactory != null) {
//            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAll[0]);
//        }
        OkHttpClient client = builder.build();
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
        registry.replace(GlideUrl.class, InputStream.class, factory);

    }
}