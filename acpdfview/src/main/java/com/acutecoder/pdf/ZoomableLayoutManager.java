package com.acutecoder.pdf;

/*
 *Created by Bhuvaneshwaran
 *on 11:37 AM, 28-07-2022
 *AcuteCoder
 */

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView.Recycler;
import androidx.recyclerview.widget.RecyclerView.State;

@SuppressWarnings("unused")
final class ZoomableLayoutManager extends LinearLayoutManager {

    private float scale;
    private boolean isScrolledByScroller;

    ZoomableLayoutManager(Context context) {
        super(context);
    }

    ZoomableLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    ZoomableLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
        if (isScrolledByScroller)
            return super.scrollVerticallyBy(dy, recycler, state);
        if (scale == 0) scale = 1;
        return super.scrollVerticallyBy((int) (dy / scale), recycler, state);
    }

    public void setScrolling(boolean isScrolling) {
        isScrolledByScroller = isScrolling;
    }

    void setScale(float scale) {
        this.scale = scale;
    }
}