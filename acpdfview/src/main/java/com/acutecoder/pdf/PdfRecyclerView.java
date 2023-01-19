package com.acutecoder.pdf;

/*
 *Created by Bhuvaneshwaran
 *on 11:37 AM, 28-07-2022
 *AcuteCoder
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

@SuppressLint({"ClickableViewAccessibility"})
final class PdfRecyclerView extends RecyclerView {

    private static final float INVALID_TOUCH_POSITION = -1f;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetectorCompat mGestureDetector;
    private float mViewWidth, mViewHeight, mTranX, mTranY, mScaleFactor, mLastTouchX, mLastTouchY, mPrevScaleFactor;
    private float mScaleCenterX, mScaleCenterY, mMaxTranX, mMaxTranY, mMaxScaleFactor, mMinScaleFactor, mDefaultScaleFactor;
    private int mActivePointerId, mScaleDuration;
    private boolean isScaling, isEnableScale;
    private ValueAnimator mScaleAnimator;
    private Listener listener;
    private OnFlingChangeListener flingChangeListener;
    private int prevFirstPos;

    public PdfRecyclerView(Context context) {
        super(context);
        init();
    }

    public PdfRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PdfRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mActivePointerId = MotionEvent.INVALID_POINTER_ID;
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        mGestureDetector = new GestureDetectorCompat(getContext(), new GestureListener());
        mMaxScaleFactor = 2.0F;
        mMinScaleFactor = 0.5F;
        mDefaultScaleFactor = 1.0F;
        mScaleFactor = mPrevScaleFactor = mDefaultScaleFactor;
        mScaleDuration = 300;
        setOnFlingListener(new OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                if (flingChangeListener != null && prevFirstPos != findFirstVisiblePosition())
                    flingChangeListener.onFling(PdfRecyclerView.this, velocityY);
                return false;
            }

        });
        addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        prevFirstPos = findFirstVisiblePosition();
                        if (flingChangeListener != null)
                            flingChangeListener.onStop(PdfRecyclerView.this);
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        flingChangeListener.onScroll(PdfRecyclerView.this);
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        break;
                }
            }
        });
        setNestedScrollingEnabled(false);
    }

    public void delete() throws Throwable {
        finalize();
    }

    void setMaxScaleFactor(float mMaxScaleFactor) {
        this.mMaxScaleFactor = mMaxScaleFactor;
    }

    void setMinScaleFactor(float mMinScaleFactor) {
        this.mMinScaleFactor = mMinScaleFactor;
    }

    void setOnZoomListener(Listener listener) {
        this.listener = listener;
    }

    void setFlingChangeListener(OnFlingChangeListener flingChangeListener) {
        this.flingChangeListener = flingChangeListener;
    }

    void setTotalPage(int pageCount) {
        if (listener != null) listener.setTotalPage(pageCount);
    }

    boolean isZoomEnabled() {
        return isEnableScale;
    }

    void setZoomEnabled(boolean value) {
        if (isEnableScale != value) {
            isEnableScale = value;
            if (!isEnableScale && mScaleFactor != 1.0F) {
                zoom(mScaleFactor, 1.0F);
            }
        }
    }

    @FloatRange(from = 1f, to = 7f)
    float getMaxScale() {
        return mMaxScaleFactor;
    }

    @FloatRange(from = 0.1f, to = 1f)
    float getMinScale() {
        return mMinScaleFactor;
    }

    int findFirstVisiblePosition() {
        ZoomableLayoutManager layoutManager = (ZoomableLayoutManager) getLayoutManager();
        return layoutManager != null ? layoutManager.findFirstVisibleItemPosition() : 0;
    }

    int findLastVisiblePosition() {
        ZoomableLayoutManager layoutManager = (ZoomableLayoutManager) getLayoutManager();
        return layoutManager != null ? layoutManager.findLastVisibleItemPosition() : 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mViewWidth = (float) MeasureSpec.getSize(widthMeasureSpec);
        mViewHeight = (float) MeasureSpec.getSize(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnableScale) {
            return super.onTouchEvent(ev);
        } else {
            boolean retVal = mGestureDetector.onTouchEvent(ev) || mScaleDetector.onTouchEvent(ev);
            int action = ev.getActionMasked();
            int pointerIndex;
            float x;
            float y;
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    pointerIndex = ev.getActionIndex();
                    x = ev.getX(pointerIndex);
                    y = ev.getY(pointerIndex);
                    mLastTouchX = x;
                    mLastTouchY = y;
                    mActivePointerId = ev.getPointerId(0);
                    break;

                case MotionEvent.ACTION_MOVE:
                    float dx;
                    float dy;
                    try {
                        pointerIndex = ev.findPointerIndex(mActivePointerId);
                        x = ev.getX(pointerIndex);
                        y = ev.getY(pointerIndex);
                        if (!isScaling && mScaleFactor > 1) {
                            dx = x - mLastTouchX;
                            dy = y - mLastTouchY;
                            setTranslateXY(mTranX + dx, mTranY + dy);
                            correctTranslateXY();
                        }

                        invalidate();
                        mLastTouchX = x;
                        mLastTouchY = y;
                    } catch (Exception var9) {
                        x = ev.getX();
                        y = ev.getY();
                        if (!isScaling && mScaleFactor > 1 && mLastTouchX != INVALID_TOUCH_POSITION) {
                            dx = x - mLastTouchX;
                            dy = y - mLastTouchY;
                            setTranslateXY(mTranX + dx, mTranY + dy);
                            correctTranslateXY();
                        }

                        invalidate();
                        mLastTouchX = x;
                        mLastTouchY = y;
                    }
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    pointerIndex = ev.getActionIndex();
                    int pointerId = ev.getPointerId(pointerIndex);
                    if (pointerId == mActivePointerId) {
                        int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        mLastTouchX = ev.getX(newPointerIndex);
                        mLastTouchY = ev.getY(newPointerIndex);
                        mActivePointerId = ev.getPointerId(newPointerIndex);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                    mLastTouchX = INVALID_TOUCH_POSITION;
                    mLastTouchY = INVALID_TOUCH_POSITION;
                    onZoomListener(mScaleFactor);
                    break;
            }

            return super.onTouchEvent(ev) || retVal;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(mTranX, mTranY);
        canvas.scale(mScaleFactor, mScaleFactor);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    private void setTranslateXY(float tranX, float tranY) {
        mTranX = tranX;
        mTranY = tranY;
    }

    private void correctTranslateXY() {
        float[] correctXY = correctTranslateXY(mTranX, mTranY);
        mTranX = correctXY[0];
        mTranY = correctXY[1];
    }

    private float[] correctTranslateXY(float x, float y) {
        if (mScaleFactor > 1f) {
            if (x > 0.0F) {
                x = 0.0F;
            } else if (x < mMaxTranX) {
                x = mMaxTranX;
            }
            if (y > 0.0F) {
                y = 0.0F;
            } else if (y < mMaxTranY) {
                y = mMaxTranY;
            }
        }
        return new float[]{x, y};
    }

    private void zoom(float startVal, float endVal) {
        onZoomListener(endVal);
        if (mScaleAnimator == null) {
            newZoomAnimation();
        }

        if (!mScaleAnimator.isRunning()) {
            mMaxTranX = mViewWidth - mViewWidth * endVal;
            mMaxTranY = mViewHeight - mViewHeight * endVal;
            float startTranX = mTranX;
            float startTranY = mTranY;
            float endTranX = mTranX - (endVal - startVal) * mScaleCenterX;
            float endTranY = mTranY - (endVal - startVal) * mScaleCenterY;
            float[] correct = correctTranslateXY(endTranX, endTranY);
            endTranX = correct[0];
            endTranY = correct[1];
            PropertyValuesHolder scaleHolder = PropertyValuesHolder.ofFloat("scale", startVal, endVal);
            PropertyValuesHolder tranXHolder = PropertyValuesHolder.ofFloat("tranX", startTranX, endTranX);
            PropertyValuesHolder tranYHolder = PropertyValuesHolder.ofFloat("tranY", startTranY, endTranY);
            mScaleAnimator.setValues(scaleHolder, tranXHolder, tranYHolder);
            mScaleAnimator.setDuration(mScaleDuration);
            mScaleAnimator.start();
        }
    }

    private void onZoomListener(float scaleFactor) {
        if (listener != null && scaleFactor >= 1f && mPrevScaleFactor != scaleFactor) {
            listener.onZoom(scaleFactor);
            PdfAdapter adapter = (PdfAdapter) getAdapter();
            if (adapter != null) adapter.refresh(this);
            ZoomableLayoutManager layoutManager = (ZoomableLayoutManager) getLayoutManager();
            if (layoutManager != null) layoutManager.setScale(scaleFactor);
            mPrevScaleFactor = scaleFactor;
        }
    }

    private void newZoomAnimation() {
        mScaleAnimator = new ValueAnimator();
        mScaleAnimator.setInterpolator(new DecelerateInterpolator());
        mScaleAnimator.addUpdateListener(animation -> {
            mScaleFactor = (Float) animation.getAnimatedValue("scale");
            setTranslateXY((Float) animation.getAnimatedValue("tranX"), (Float) animation.getAnimatedValue("tranY"));
            invalidate();
        });
        mScaleAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                isScaling = true;
            }

            public void onAnimationEnd(Animator animation) {
                isScaling = false;
            }

            public void onAnimationCancel(Animator animation) {
                isScaling = false;
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        PdfAdapter adapter = (PdfAdapter) getAdapter();
        if (adapter != null) {
            adapter.setWidth(getMeasuredWidth());
//            adapter.notifyDataSetChanged(); //RecyclerView handles this, :-)
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    interface OnFlingChangeListener {
        void onFling(PdfRecyclerView recyclerView, int velocity);

        void onScroll(PdfRecyclerView recyclerView);

        void onStop(PdfRecyclerView recyclerView);
    }

    interface Listener {

        void onZoom(float scale);

        void setTotalPage(int totalPage);
    }

    private final class ScaleListener implements OnScaleGestureListener {

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            float mLastScale = mScaleFactor;
            mScaleFactor = mScaleFactor * detector.getScaleFactor();
            mScaleFactor = Math.max(mMinScaleFactor, Math.min(mScaleFactor, mMaxScaleFactor));
            mMaxTranX = mViewWidth - mViewWidth * mScaleFactor;
            mMaxTranY = mViewHeight - mViewHeight * mScaleFactor;
            mScaleCenterX = detector.getFocusX();
            mScaleCenterY = detector.getFocusY();
            float offsetX = mScaleCenterX * (mLastScale - mScaleFactor);
            float offsetY = mScaleCenterY * (mLastScale - mScaleFactor);
            setTranslateXY(mTranX + offsetX, mTranY + offsetY);
            isScaling = true;
            invalidate();
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            if (mScaleFactor <= mDefaultScaleFactor) {
                mScaleCenterX = -mTranX / (mScaleFactor - 1);
                mScaleCenterY = -mTranY / (mScaleFactor - 1);
                zoom(mScaleFactor, mDefaultScaleFactor);
            }

            isScaling = false;
        }
    }

    private final class GestureListener extends SimpleOnGestureListener {

        public boolean onDoubleTap(MotionEvent e) {
            float startFactor = mScaleFactor;
            float endFactor;
            if (mScaleFactor == mDefaultScaleFactor) {
                mScaleCenterX = e.getX();
                mScaleCenterY = e.getY();
                endFactor = mMaxScaleFactor;
            } else {
                mScaleCenterX = mScaleFactor == 1.0F ? e.getX() : -mTranX / (mScaleFactor - (float) 1);
                mScaleCenterY = mScaleFactor == 1.0F ? e.getY() : -mTranY / (mScaleFactor - (float) 1);
                endFactor = mDefaultScaleFactor;
            }

            zoom(startFactor, endFactor);
            onZoomListener(endFactor);
            return super.onDoubleTap(e);
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}