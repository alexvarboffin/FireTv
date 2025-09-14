package tv.hdonlinetv.besttvchannels.movies.watchfree.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ThresholdSwipeRefreshLayout extends FrameLayout {

    private static final float THRESHOLD = 0.4F;

    private float startY;
    private float screenHeight;

    public ThresholdSwipeRefreshLayout(Context context) {
        super(context);
        init(context);
    }

    public ThresholdSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

}
