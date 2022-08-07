package com.acutecoder.pdf;

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

final class PdfLayoutManager extends LinearLayoutManager {

    private PdfRecyclerView recyclerView;

    public PdfLayoutManager(Context context) {
        super(context);
    }

    PdfRecyclerView getRecyclerView() {
        return recyclerView;
    }

    void setRecyclerView(PdfRecyclerView var1) {
        recyclerView = var1;
    }

	@Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        if (view instanceof PdfRecyclerView) {
            recyclerView = (PdfRecyclerView) view;
        }
    }

    @Override
    public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
        int scroll = recyclerView != null ? recyclerView.calculateScroll(dy) : dy;
        return super.scrollVerticallyBy(scroll, recycler, state);
    }
}
