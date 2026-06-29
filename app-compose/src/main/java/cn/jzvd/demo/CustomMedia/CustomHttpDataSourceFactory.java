//package cn.jzvd.demo.CustomMedia;
//
//
//import static androidx.media3.common.util.Util.getUserAgent;
//
//import androidx.media3.common.util.UnstableApi;
//import androidx.media3.datasource.DataSpec;
//import androidx.media3.datasource.DefaultHttpDataSource;
//import androidx.media3.datasource.HttpDataSource;
//
//import java.io.IOException;
//import java.net.HttpURLConnection;
//import java.net.URL;
//
//@UnstableApi
//public class CustomHttpDataSourceFactory extends DefaultHttpDataSource.Factory {
//
//    @Override
//    protected DefaultHttpDataSource createDataSourceInternal(
//            HttpDataSource.RequestProperties defaultRequestProperties) {
//        return new MyDefaultHttpDataSource(getUserAgent(), defaultRequestProperties);
//    }
//}
