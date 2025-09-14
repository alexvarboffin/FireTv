package com.merhold.extensiblepageindicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.viewpager2.widget.ViewPager2;

import tv.hdonlinetv.besttvchannels.movies.watchfree.R;


public class ExtensiblePageIndicatorNew extends View {

    private static final float OFFSET_MULTIPLIER_DRAG = 1.2f;
    private static final float OFFSET_MULTIPLIER_SETTLING = 1.4f;
    private static final float OFFSET_MULTIPLIER_NORMAL = 0.30f;

    private ViewPager2 mViewPager;
    private Paint activePaint;
    private Paint inactivePaint;

    private int circlePadding;
    private int circleRadius;
    private int circleCount;

    private int mGravity;
    private int mState;
    private float mPageOffset;
    private int mCurrentDragPage;
    private int mSelectedPage;
    private float currentNormalOffset;
    private float currentRelativePageOffset;
    private float startedSettleNormalOffset;
    private float startedSettlePageOffset;

    public ExtensiblePageIndicatorNew(Context context) {
        super(context);
        init(null);
    }

    public ExtensiblePageIndicatorNew(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ExtensiblePageIndicatorNew(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        if (isInEditMode()) {
            circleCount = 3;
        }

        circleRadius = (int) getResources().getDimension(R.dimen.fvp_default_circle_radius);
        circlePadding = (int) getResources().getDimension(R.dimen.fvp_default_circle_padding);

        int inactiveColor = ContextCompat.getColor(getContext(), R.color.fpi_default_indicator_inactive_color);
        int activeColor = ContextCompat.getColor(getContext(), R.color.fpi_default_indicator_active_color);
        int gravity = Gravity.CENTER;

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.ExtensiblePageIndicator, 0, 0);
            circleRadius = (int) a.getDimension(R.styleable.ExtensiblePageIndicator_indicatorRadius, circleRadius);
            circlePadding = (int) a.getDimension(R.styleable.ExtensiblePageIndicator_indicatorPadding, circlePadding);
            inactiveColor = a.getColor(R.styleable.ExtensiblePageIndicator_indicatorInactiveColor, inactiveColor);
            activeColor = a.getColor(R.styleable.ExtensiblePageIndicator_indicatorActiveColor, activeColor);
            mGravity = a.getInt(R.styleable.ExtensiblePageIndicator_android_gravity, gravity);
        }

        activePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        activePaint.setColor(activeColor);

        inactivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        inactivePaint.setColor(inactiveColor);
    }

    public void initViewPager(ViewPager2 viewPager) {
        if (mViewPager == viewPager)
            return;
        if (mViewPager != null)
            mViewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        if (viewPager.getAdapter() == null)
            throw new IllegalStateException("ViewPager2 doesn't have an adapter instance.");
        mViewPager = viewPager;
        mViewPager.registerOnPageChangeCallback(pageChangeCallback);
        circleCount = viewPager.getAdapter().getItemCount();
        mCurrentDragPage = viewPager.getCurrentItem();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = View.resolveSize(getDesiredHeight(), heightMeasureSpec);
        int width = View.resolveSize(getDesiredWidth(), widthMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int getDesiredHeight() {
        return getPaddingTop() + getPaddingBottom() + circleRadius * 2;
    }

    private int getDesiredWidth() {
        return getPaddingLeft() + getPaddingRight() + (circleRadius * 2) * circleCount + (circleCount - 1) * circlePadding;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        for (int i = 0; i < circleCount; i++) {
            float center = getCircleCenter(i);
            canvas.drawCircle(center, getPaddingTop() + circleRadius, circleRadius, inactivePaint);
        }

        drawRect(canvas);
    }

    private void drawRect(Canvas canvas) {
        if (mViewPager == null || mViewPager.getAdapter() == null || mViewPager.getAdapter().getItemCount() == 0)
            return;

        float top = getPaddingTop();
        float bottom = top + circleRadius * 2;
        float moveDistance = circleRadius * 2 + circlePadding;
        boolean isDragForward = mSelectedPage - mCurrentDragPage < 1;

        float relativePageOffset = isDragForward ? mPageOffset : 1.0f - mPageOffset;
        currentRelativePageOffset = relativePageOffset;

        float shiftedOffset = relativePageOffset * OFFSET_MULTIPLIER_NORMAL;
        float settleShiftedOffset = Math.max(0, mapValue(relativePageOffset, startedSettlePageOffset, 1.0f, startedSettleNormalOffset, 1.0f));
        float normalOffset = mState == ViewPager2.SCROLL_STATE_SETTLING ? settleShiftedOffset : shiftedOffset;
        currentNormalOffset = normalOffset;

        float largerOffset = Math.min(mState == ViewPager2.SCROLL_STATE_SETTLING ? relativePageOffset * OFFSET_MULTIPLIER_SETTLING : relativePageOffset * OFFSET_MULTIPLIER_DRAG, 1.0f);
        float circleCenter = getCircleCenter(isDragForward ? mCurrentDragPage : mSelectedPage);

        float normal = moveDistance * normalOffset;
        float large = moveDistance * largerOffset;

        float left = isDragForward ? circleCenter - circleRadius + normal : circleCenter - circleRadius - large;
        float right = isDragForward ? circleCenter + circleRadius + large : circleCenter + circleRadius - normal;

        RectF rectF = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rectF, circleRadius, circleRadius, activePaint);
    }

    private float mapValue(float value, float a1, float a2, float b1, float b2) {
        return b1 + (value - a1) * (b2 - b1) / (a2 - a1);
    }

    private float getCirclePadding(int position) {
        return circlePadding * position + circleRadius * 2 * position;
    }

    private float getCircleCenter(int position) {
        return getStartedX() + circleRadius + getCirclePadding(position);
    }

    private float getStartedX() {
        switch (mGravity) {
            case Gravity.LEFT:
            case GravityCompat.START:
                return getPaddingLeft();
            case Gravity.RIGHT:
            case GravityCompat.END:
                return getMeasuredWidth() - getPaddingRight() - getAllCirclesWidth();
            case Gravity.CENTER:
            default:
                return (getMeasuredWidth() / 2 - getAllCirclesWidth() / 2);
        }
    }

    private float getAllCirclesWidth() {
        return circleRadius * 2 * circleCount + (circleCount - 1) * circlePadding;
    }

    private final ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            mCurrentDragPage = position;
            mPageOffset = positionOffset;
            postInvalidate();
        }

        @Override
        public void onPageSelected(int position) {
            // No-op
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mState = state;
            if (state == ViewPager2.SCROLL_STATE_IDLE || state == ViewPager2.SCROLL_STATE_DRAGGING) {
                mSelectedPage = mViewPager.getCurrentItem();
                currentNormalOffset = 0;
                currentRelativePageOffset = 0;
            } else if (state == ViewPager2.SCROLL_STATE_SETTLING) {
                startedSettleNormalOffset = currentNormalOffset;
                startedSettlePageOffset = currentRelativePageOffset;
            }
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        if (mViewPager != null) {
            mViewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        }
        super.onDetachedFromWindow();
    }
}
