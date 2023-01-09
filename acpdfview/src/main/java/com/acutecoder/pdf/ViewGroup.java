package com.acutecoder.pdf;

/*
 *Created by Bhuvaneshwaran
 *on 10:08 PM, 1/9/2023
 *AcuteCoder
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

class ViewGroup extends LinearLayout {

    protected boolean preventViewModify;

    public ViewGroup(Context context) {
        super(context);
    }

    public ViewGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        if (!preventViewModify)
            super.addView(child, index, params);
    }

    @Override
    protected boolean addViewInLayout(View child, int index, android.view.ViewGroup.LayoutParams params) {
        if (!preventViewModify)
            return super.addViewInLayout(child, index, params);
        return false;
    }

    @Override
    protected boolean addViewInLayout(View child, int index, android.view.ViewGroup.LayoutParams params, boolean preventRequestLayout) {
        if (!preventViewModify)
            return super.addViewInLayout(child, index, params, preventRequestLayout);
        return false;
    }

    @Override
    public void addView(View child) {
        if (!preventViewModify)
            super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (!preventViewModify)
            super.addView(child, index);
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        if (!preventViewModify)
            super.addView(child, params);
    }

    @Override
    public void addView(View child, int width, int height) {
        if (!preventViewModify)
            super.addView(child, width, height);
    }

    @Override
    public void removeAllViews() {
        if (!preventViewModify)
            super.removeAllViews();
    }

    @Override
    public void removeAllViewsInLayout() {
        if (!preventViewModify)
            super.removeAllViewsInLayout();
    }

    @Override
    public void removeViewInLayout(View view) {
        if (!preventViewModify)
            super.removeViewInLayout(view);
    }

    @Override
    public void removeViewsInLayout(int start, int count) {
        if (!preventViewModify)
            super.removeViewsInLayout(start, count);
    }

    @Override
    public void removeView(View view) {
        if (!preventViewModify)
            super.removeView(view);
    }

    @Override
    public void removeViewAt(int index) {
        if (!preventViewModify)
            super.removeViewAt(index);
    }

    @Override
    public void removeViews(int start, int count) {
        if (!preventViewModify)
            super.removeViews(start, count);
    }
}
