package tv.hdonlinetv.besttvchannels.movies.watchfree.utils

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.Keep
import cn.jzvd.JZDataSource
import cn.jzvd.JzvdStd
import com.walhalla.ui.DLog.d

@Keep
class JZVideoPlayerNew : JzvdStd {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onPrepared() {
        super.onPrepared()
        // Ваш код при готовности видео
        Log.d(TAG, "Видео готово к воспроизведению")
    }

    private var videoUrl: String? = null

    override fun setUp(url: String, title: String, screen: Int) {
        this.videoUrl = url
        d("@DATADATA@$videoUrl@@$title")
        super.setUp(url, title)
        //        bottomProgressBar.setVisibility(View.GONE);
        //        bottomProgressBar.setBackgroundColor(Color.YELLOW);
    }

    override fun setUp(jzDataSource: JZDataSource, screen: Int) {
        this.videoUrl = jzDataSource.currentUrl.toString()
        d("@DATADATA@$jzDataSource@@")
        super.setUp(jzDataSource, screen)
//        bottomProgressBar.setVisibility(View.GONE);
//        bottomProgressBar.setBackgroundColor(Color.YELLOW);
    }

    override fun setAllControlsVisiblity(
        topCon: Int, bottomCon: Int, startBtn: Int, loadingPro: Int,
        posterImg: Int, bottomPro: Int, retryLayout: Int
    ) {
        topContainer.visibility = topCon
        bottomContainer.visibility = bottomCon
        startButton.visibility = startBtn
        loadingProgressBar.visibility = loadingPro
        posterImageView.visibility = posterImg

        //bottomProgressBar.setVisibility(bottomPro);
        bottomProgressBar.visibility = GONE

        //        bottomProgressBar.setBackgroundColor(Color.YELLOW);
        mRetryLayout.visibility = retryLayout
    }

    override fun setBufferProgress(bufferProgress: Int) {
        super.setBufferProgress(bufferProgress)
        bottomProgressBar.visibility = GONE
    }

    //    @Override
    //    public void setUp(String url, String title) {
    //        this.videoUrl = url;
    //        DLog.d("@@" + videoUrl);
    //        super.setUp(url, title);
    //    }
    // Вызывается при ошибке воспроизведения
    override fun onError(what: Int, extra: Int) {
        super.onError(what, extra)
        val whatDescription = if (what == 1) {
            "MEDIA_ERROR_UNKNOWN"
        } else if (what == 100) {
            "MEDIA_ERROR_SERVER_DIED"
        } else {
            "UNKNOWN_ERROR"
        }
        val extraDescription = if (extra == -1004) {
            "MEDIA_ERROR_IO"
        } else if (extra == -1007) {
            "MEDIA_ERROR_MALFORMED"
        } else if (extra == -1010) {
            "MEDIA_ERROR_UNSUPPORTED"
        } else if (extra == -110) {
            "MEDIA_ERROR_TIMED_OUT"
        } else {
            "UNKNOWN_EXTRA_INFO"
        }

        Log.d(
            TAG, ("Ошибка воспроизведения: what = "
                    + whatDescription + ", extra = "
                    + extraDescription
                    + " " + this.videoUrl)
        )
    }

    // Вызывается, когда видео завершено
    override fun onCompletion() {
        super.onCompletion()
        // Ваш код при завершении видео
        Log.d(TAG, "Воспроизведение завершено")
    }

    companion object {
        private const val TAG = "@@@"
    }
}
