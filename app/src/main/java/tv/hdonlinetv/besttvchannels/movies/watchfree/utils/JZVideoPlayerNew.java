package tv.hdonlinetv.besttvchannels.movies.watchfree.utils;

import android.content.Context;

import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Keep;

import com.walhalla.ui.DLog;

import cn.jzvd.JZDataSource;
import cn.jzvd.JZMediaSystem;
import cn.jzvd.JzvdStd;

@Keep
public class JZVideoPlayerNew extends JzvdStd {


    private static final String TAG = "@@@";

    public JZVideoPlayerNew(Context context) {
        super(context);
    }

    public JZVideoPlayerNew(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onPrepared() {
        super.onPrepared();
        // Ваш код при готовности видео
        Log.d(TAG, "Видео готово к воспроизведению");
    }

    private String videoUrl;

    @Override
    public void setUp(String url, String title, int screen) {
        this.videoUrl = url;
        DLog.d("@@" + videoUrl + "@@" + title);
        super.setUp(url, title);
//        bottomProgressBar.setVisibility(View.GONE);
//        bottomProgressBar.setBackgroundColor(Color.YELLOW);
    }

    @Override
    public void setUp(JZDataSource jzDataSource, int screen) {
        this.videoUrl = jzDataSource.getCurrentUrl().toString();
        DLog.d("@@" + videoUrl + "@@");
        super.setUp(jzDataSource, screen);
//        bottomProgressBar.setVisibility(View.GONE);
//        bottomProgressBar.setBackgroundColor(Color.YELLOW);
    }

    @Override
    public void setAllControlsVisiblity(int topCon, int bottomCon, int startBtn, int loadingPro,
                                        int posterImg, int bottomPro, int retryLayout) {
        topContainer.setVisibility(topCon);
        bottomContainer.setVisibility(bottomCon);
        startButton.setVisibility(startBtn);
        loadingProgressBar.setVisibility(loadingPro);
        posterImageView.setVisibility(posterImg);
        //bottomProgressBar.setVisibility(bottomPro);

        bottomProgressBar.setVisibility(View.GONE);
//        bottomProgressBar.setBackgroundColor(Color.YELLOW);

        mRetryLayout.setVisibility(retryLayout);
    }

    @Override
    public void setBufferProgress(int bufferProgress) {
        super.setBufferProgress(bufferProgress);
        bottomProgressBar.setVisibility(GONE);
    }

//    @Override
//    public void setUp(String url, String title) {
//        this.videoUrl = url;
//        DLog.d("@@" + videoUrl);
//        super.setUp(url, title);
//    }

    // Вызывается при ошибке воспроизведения
    @Override
    public void onError(int what, int extra) {
        super.onError(what, extra);

        String whatDescription;
        if (what == 1) {
            whatDescription = "MEDIA_ERROR_UNKNOWN";
        } else if (what == 100) {
            whatDescription = "MEDIA_ERROR_SERVER_DIED";
        } else {
            whatDescription = "UNKNOWN_ERROR";
        }

        String extraDescription;
        if (extra == -1004) {
            extraDescription = "MEDIA_ERROR_IO";
        } else if (extra == -1007) {
            extraDescription = "MEDIA_ERROR_MALFORMED";
        } else if (extra == -1010) {
            extraDescription = "MEDIA_ERROR_UNSUPPORTED";
        } else if (extra == -110) {
            extraDescription = "MEDIA_ERROR_TIMED_OUT";
        } else {
            extraDescription = "UNKNOWN_EXTRA_INFO";
        }

        Log.d(TAG, "Ошибка воспроизведения: what = "
                + whatDescription + ", extra = "
                + extraDescription
                + " " + this.videoUrl);

    }

    // Вызывается, когда видео завершено
    @Override
    public void onCompletion() {
        super.onCompletion();
        // Ваш код при завершении видео
        Log.d(TAG, "Воспроизведение завершено");
    }
}
