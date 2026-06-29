package tv.hdonlinetv.compose.player

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

    @Volatile
    var isPlayerReleased: Boolean = false
        private set

    var onFullscreenChange: ((Boolean) -> Unit)? = null

    override fun gotoFullscreen() {
        super.gotoFullscreen()
        onFullscreenChange?.invoke(true)
    }

    override fun gotoNormalScreen() {
        super.gotoNormalScreen()
        onFullscreenChange?.invoke(false)
    }

    override fun clearFloatScreen() {
        super.clearFloatScreen()
        onFullscreenChange?.invoke(false)
    }

    override fun reset() {
        if (isPlayerReleased) {
            return
        }
        isPlayerReleased = true
        try {
            super.reset()
        } catch (e: IllegalArgumentException) {
            // JzvdStd.unregisterWifiListener() may run twice during Compose teardown
        }
    }

    private fun markActive() {
        isPlayerReleased = false
    }

    override fun startVideo() {
        markActive()
        super.startVideo()
    }

    override fun onPrepared() {
        super.onPrepared()
        posterImageView.visibility = GONE
        Log.d(TAG, "Видео готово к воспроизведению")
    }

    private var videoUrl: String? = null

    override fun setUp(url: String, title: String, screen: Int) {
        markActive()
        videoUrl = url
        d("@DATADATA@$videoUrl@@$title")
        super.setUp(url, title)
    }

    override fun setUp(jzDataSource: JZDataSource, screen: Int) {
        markActive()
        videoUrl = jzDataSource.currentUrl.toString()
        d("@DATADATA@$jzDataSource@@")
        super.setUp(jzDataSource, screen)
    }

    override fun setAllControlsVisiblity(
        topCon: Int,
        bottomCon: Int,
        startBtn: Int,
        loadingPro: Int,
        posterImg: Int,
        bottomPro: Int,
        retryLayout: Int,
    ) {
        topContainer.visibility = topCon
        bottomContainer.visibility = bottomCon
        startButton.visibility = startBtn
        loadingProgressBar.visibility = loadingPro
        posterImageView.visibility = posterImg
        bottomProgressBar.visibility = GONE
        mRetryLayout.visibility = retryLayout
    }

    override fun setBufferProgress(bufferProgress: Int) {
        super.setBufferProgress(bufferProgress)
        bottomProgressBar.visibility = GONE
        if (bufferProgress > 0) {
            posterImageView.visibility = GONE
        }
    }

    override fun onError(what: Int, extra: Int) {
        super.onError(what, extra)
        Log.d(TAG, "Ошибка воспроизведения: what=$what extra=$extra url=$videoUrl")
    }

    override fun onCompletion() {
        super.onCompletion()
        Log.d(TAG, "Воспроизведение завершено")
    }

    companion object {
        private const val TAG = "@@@"
    }
}
