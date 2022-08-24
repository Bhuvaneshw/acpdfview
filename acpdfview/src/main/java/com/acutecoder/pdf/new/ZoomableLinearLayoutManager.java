package com.acutecoder.pdfview;

/*
 *Created by Bhuvaneshwaran
 *on 11:37 AM, 28-07-2022
 *AcuteCoder
 */

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Recycler;
import androidx.recyclerview.widget.RecyclerView.State;

final class ZoomableLinearLayoutManager extends LinearLayoutManager {

    private ZoomableRecyclerView recyclerView;

    public ZoomableLinearLayoutManager(Context context) {
        super(context);
    }

    public ZoomableLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public ZoomableLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ZoomableRecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setRecyclerView(ZoomableRecyclerView var1) {
        recyclerView = var1;
    }

    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        if (view instanceof ZoomableRecyclerView) {
            recyclerView = (ZoomableRecyclerView) view;
        }
    }

    @Override
    public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
        int scroll = recyclerView != null ? recyclerView.calculateScroll(dy) : dy;
        return super.scrollVerticallyBy(scroll, recycler, state);
    }

    @Override
    public void onLayoutCompleted(State state) {
        super.onLayoutCompleted(state);
        recyclerView.onLoadingFinished();
    }
}