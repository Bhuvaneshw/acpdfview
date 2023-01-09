package com.acutecoder.pdf;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Bhuvaneshwaran
 * on 12:36 AM, 30-07-2022
 * AcuteCoder
 */

@SuppressLint("NotifyDataSetChanged")
@SuppressWarnings("unused")
public final class PdfView extends ViewGroup {

    private final ArrayList<OnActionListener> listeners = new ArrayList<>();
    private PdfRecyclerView recyclerView;
    private PdfAdapter adapter;
    private int totPage;
    private boolean isDarkMode = false, isCallbackCalled = false;
    private File file;
    private Drawable pageBackground;
    private int modFlingLimit = 1000;

    public PdfView(@NonNull Context context) {
        super(context);
        init();
    }

    public PdfView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PdfView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        recyclerView = new PdfRecyclerView(getContext());
        recyclerView.setZoomEnabled(true);
        recyclerView.setMaxScaleFactor(5f);
        recyclerView.setMinScaleFactor(0.9f);
        ZoomableLayoutManager layoutManager = new ZoomableLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        setBackgroundColor(isDarkMode ? 0xff333333 : 0xffeeeeee);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int currentPage = getMaximumVisibleViewPosition() + 1;
                for (OnActionListener listener : listeners)
                    listener.onPageChanged(currentPage, totPage);
                if (!isCallbackCalled && currentPage == 1) {
                    isCallbackCalled = true;
                    for (OnActionListener listener : listeners)
                        listener.onLoaded();
                }
            }
        });
        recyclerView.setOnZoomListener(new PdfRecyclerView.Listener() {
            @Override
            public void onZoom(float scale) {
                adapter.setScale(scale * 0.8f);
                adapter.notifyDataSetChanged();
                for (OnActionListener listener : listeners)
                    listener.onZoom(scale);
            }

            @Override
            public void setTotalPage(int totalPage) {
                totPage = totalPage;
                for (OnActionListener listener : listeners)
                    listener.onTotalPage(totalPage);
            }
        });
        addView(recyclerView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        preventViewModify = true;
    }

    public void setPath(File file) {
        this.file = file;
    }

    public void load() {
        for (OnActionListener listener : listeners)
            listener.onStartLoad();
        recyclerView.post(() -> {
            adapter = new PdfAdapter(getContext(), file, recyclerView, isDarkMode);
            if (pageBackground != null) adapter.setBackground(pageBackground);
            adapter.setModFlingLimit(modFlingLimit);
            recyclerView.setAdapter(adapter);
            recyclerView.setFlingChangeListener(adapter);
        });
//        adapter.notifyDataSetChanged(); //RecyclerView handles this, :-)
    }

    public boolean isDarkMode() {
        return isDarkMode;
    }

    public void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
        setBackgroundColor(isDarkMode ? 0xff333333 : 0xffeeeeee);
        if (adapter != null) {
            adapter.setDarkMode(darkMode);
            adapter.notifyDataSetChanged();
        }
        for (OnActionListener listener : listeners)
            listener.onThemeChanged();
    }

    public Drawable getPageBackground() {
        return adapter.getDrawable();
    }

    public void setPageBackground(Drawable drawable) {
        this.pageBackground = drawable;
        if (adapter != null)
            adapter.setBackground(drawable);
    }

    public int getModFlingLimit() {
        return modFlingLimit;
    }

    public void setModFlingLimit(int modFlingLimit) {
        this.modFlingLimit = modFlingLimit;
        if (adapter != null)
            adapter.setModFlingLimit(modFlingLimit);
    }

    public void addOnActionListener(OnActionListener actionListener) {
        this.listeners.add(actionListener);
    }

    public void removeOnActionListener(OnActionListener listener) {
        listeners.remove(listener);
    }

    @FloatRange(from = 1f, to = 7f)
    public float getMaxZoomScale() {
        return recyclerView.getMaxScale();
    }

    public void setMaxZoomScale(@FloatRange(from = 1f, to = 7f) float maxZoomScale) {
        if (maxZoomScale > 7) throw new RuntimeException("Scale is too big");
        recyclerView.setMaxScaleFactor(maxZoomScale);
    }

    @FloatRange(from = 0.1f, to = 1f)
    public float getMinZoomScale() {
        return recyclerView.getMinScale();
    }

    public void setMinZoomScale(@FloatRange(from = 0.1f, to = 1f) float minZoomScale) {
        recyclerView.setMinScaleFactor(minZoomScale);
    }

    public void scrollToPage(int position) {
        recyclerView.smoothScrollToPosition(position);
    }

    public boolean isZoomEnabled() {
        return recyclerView.isZoomEnabled();
    }

    public void setZoomEnabled(boolean enabled) {
        recyclerView.setZoomEnabled(enabled);
    }

    PdfRecyclerView getRecyclerView() {
        return recyclerView;
    }

    void setScrolling() {
        ZoomableLayoutManager layoutManager = (ZoomableLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null)
            layoutManager.setScrolling(true);
        adapter.onFling(recyclerView, adapter.getModFlingLimit() + 1);
    }

    private int getMaximumVisibleViewPosition() {
        int firstItemPosition = recyclerView.findFirstVisiblePosition();
        int lastItemPosition = recyclerView.findLastVisiblePosition();
        int mostVisibleItemPosition = firstItemPosition, maxPercentage = 0;
        for (int i = 0; i <= lastItemPosition; i++) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            View view = layoutManager != null ? layoutManager.findViewByPosition(i) : null;
            if (view != null && getVisiblePercent(view) > maxPercentage) {
                maxPercentage = getVisiblePercent(view);
                mostVisibleItemPosition = i;
            }
        }
        return mostVisibleItemPosition;
    }

    private int getVisiblePercent(View view) {
        if (view.isShown()) {
            Rect r = new Rect();
            boolean isVisible = view.getGlobalVisibleRect(r);
            if (isVisible) {
                double sVisible = r.width() * r.height();
                double sTotal = view.getWidth() * view.getHeight();
                return (int) (100 * sVisible / sTotal);
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }
}
