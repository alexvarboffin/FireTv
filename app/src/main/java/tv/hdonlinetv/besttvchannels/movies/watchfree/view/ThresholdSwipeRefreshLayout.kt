package tv.hdonlinetv.besttvchannels.movies.watchfree.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class ThresholdSwipeRefreshLayout : FrameLayout {
    private val startY = 0f
    private var screenHeight = 0f

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        screenHeight = context.resources.displayMetrics.heightPixels.toFloat()
    }

    companion object {
        private const val THRESHOLD = 0.4f
    }
}
