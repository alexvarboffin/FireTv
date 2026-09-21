package tv.hdonlinetv.compose.player

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
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

    /**
     * When true, hide JZ XML chrome (seek/replay/top/bottom) so Compose owns controls.
     * Used on TV; phone keeps native JZ UI.
     */
    var suppressNativeChrome: Boolean = false
        set(value) {
            field = value
            if (value) {
                applyNativeChromeSuppression()
            }
        }

    var onPlaybackProgress: ((positionMs: Long, durationMs: Long) -> Unit)? = null

    /** Mirrors JZ [state] for Compose chrome (play / pause / replay). */
    var onPlaybackStateChanged: ((state: Int) -> Unit)? = null

    override fun init(context: Context?) {
        super.init(context)
        if (suppressNativeChrome) {
            applyNativeChromeSuppression()
        }
    }

    private fun applyNativeChromeSuppression() {
        // Keep D-pad in Compose overlay; do not enter XML SeekBar / buttons.
        descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        isFocusable = false
        isFocusableInTouchMode = false
        progressBar?.isFocusable = false
        progressBar?.isFocusableInTouchMode = false
        startButton?.isFocusable = false
        backButton?.isFocusable = false
        fullscreenButton?.isFocusable = false
        hideNativeChrome(keepLoading = true)
    }

    private fun hideNativeChrome(keepLoading: Boolean) {
        topContainer?.visibility = GONE
        bottomContainer?.visibility = GONE
        startButton?.visibility = GONE
        bottomProgressBar?.visibility = GONE
        replayTextView?.visibility = GONE
        mRetryLayout?.visibility = GONE
        if (!keepLoading) {
            loadingProgressBar?.visibility = GONE
        }
    }

    fun seekToMs(positionMs: Long) {
        val media = mediaInterface ?: return
        val duration = media.duration.coerceAtLeast(0L)
        val target = if (duration > 0) {
            positionMs.coerceIn(0L, duration)
        } else {
            positionMs.coerceAtLeast(0L)
        }
        seekToManulPosition = -1
        media.seekTo(target)
    }

    fun forceShowNativeChromeForDebug() {
        if (suppressNativeChrome) return
        topContainer?.visibility = VISIBLE
        bottomContainer?.visibility = VISIBLE
        startButton?.visibility = VISIBLE
        bottomProgressBar?.visibility = GONE
        // Keep focus out of XML SeekBar so Compose D-pad still works.
        descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        progressBar?.isFocusable = false
        progressBar?.isFocusableInTouchMode = false
        startButton?.isFocusable = false
        backButton?.isFocusable = false
        fullscreenButton?.isFocusable = false
    }

    /** Same action as tapping XML [start] (play / pause / replay). */
    fun performStartButtonAction() {
        val btn = startButton
        if (btn != null) {
            btn.performClick()
            return
        }
        when (state) {
            STATE_PLAYING -> {
                mediaInterface?.pause()
                onStatePause()
            }
            STATE_PAUSE -> {
                mediaInterface?.start()
                onStatePlaying()
            }
            else -> startVideo()
        }
    }

    private fun notifyState() {
        onPlaybackStateChanged?.invoke(state)
    }

    override fun onStatePlaying() {
        super.onStatePlaying()
        notifyState()
    }

    override fun onStatePause() {
        super.onStatePause()
        notifyState()
    }

    override fun onStateError() {
        super.onStateError()
        notifyState()
    }

    override fun onStateAutoComplete() {
        super.onStateAutoComplete()
        notifyState()
    }

    override fun onStatePreparing() {
        super.onStatePreparing()
        notifyState()
    }

    override fun onStateNormal() {
        super.onStateNormal()
        notifyState()
    }

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
        if (suppressNativeChrome) {
            hideNativeChrome(keepLoading = false)
        }
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

    override fun onProgress(progress: Int, position: Long, duration: Long) {
        if (!suppressNativeChrome) {
            super.onProgress(progress, position, duration)
        }
        onPlaybackProgress?.invoke(position, duration)
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
        if (suppressNativeChrome) {
            hideNativeChrome(keepLoading = loadingPro == View.VISIBLE)
            posterImageView?.visibility = posterImg
            loadingProgressBar?.visibility = loadingPro
            return
        }
        topContainer.visibility = topCon
        bottomContainer.visibility = bottomCon
        startButton.visibility = startBtn
        loadingProgressBar.visibility = loadingPro
        posterImageView.visibility = posterImg
        bottomProgressBar.visibility = GONE
        mRetryLayout.visibility = retryLayout
    }

    override fun setBufferProgress(bufferProgress: Int) {
        if (!suppressNativeChrome) {
            super.setBufferProgress(bufferProgress)
        }
        bottomProgressBar?.visibility = GONE
        if (bufferProgress > 0) {
            posterImageView?.visibility = GONE
        }
    }

    override fun onError(what: Int, extra: Int) {
        super.onError(what, extra)
        if (suppressNativeChrome) {
            // Compose owns retry/refresh — keep JZ retry layout hidden.
            mRetryLayout?.visibility = GONE
            replayTextView?.visibility = GONE
        }
        Log.d(TAG, "Ошибка воспроизведения: what=$what extra=$extra url=$videoUrl")
    }

    override fun onCompletion() {
        super.onCompletion()
        if (suppressNativeChrome) {
            hideNativeChrome(keepLoading = false)
        }
        Log.d(TAG, "Воспроизведение завершено")
    }

    companion object {
        private const val TAG = "@@@"
    }
}
