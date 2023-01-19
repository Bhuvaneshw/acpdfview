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
import java.util.List;

/**
 * PdfView is an important class used to render pdf files.
 * <br><br>
 * Created by Bhuvaneshwaran
 * on 12:36 AM, 30-07-2022.
 *
 * @author AcuteCoder
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
    private List<TemporaryFile> temporaryFiles;
    private float quality = 0.8f;

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
        temporaryFiles = new ArrayList<>();

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
                adapter.setScale(scale * quality);
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

    /**
     * Sets the source path of pdf file
     *
     * @param file Defines the source path of pdf file
     */
    public void setPath(File file) {
        this.file = file;
        if (file instanceof TemporaryFile)
            temporaryFiles.add((TemporaryFile) file);
    }

    /**
     * Loads the pdf file and render it in PdfView
     *
     * @see PdfView
     */
    public void load() {
        for (OnActionListener listener : listeners)
            listener.onStartLoad();
        post(() -> {
            if (adapter == null) {
                adapter = new PdfAdapter(getContext(), recyclerView);
                recyclerView.setAdapter(adapter);
                recyclerView.setFlingChangeListener(adapter);
            }
            adapter.setScale(adapter.getScale() * quality);
            adapter.setFile(file);
            adapter.setDarkMode(isDarkMode);
            if (pageBackground != null) adapter.setBackground(pageBackground);
            adapter.setModFlingLimit(modFlingLimit);
        });
    }

    /**
     * Refresh the content of the PdfView
     */
    public void refresh() {
        if (adapter != null) {
            post(() -> {
                adapter.notifyDataSetChanged();
                post(() -> adapter.refresh(recyclerView));
                scrollToPage(1);
            });
        }
    }

    /**
     * Return true if it is dark mode
     *
     * @return boolean
     */
    public boolean isDarkMode() {
        return isDarkMode;
    }

    /**
     * Sets dark mode
     *
     * @param darkMode boolean
     */
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

    /**
     * Returns page background
     *
     * @return Drawable
     */
    public Drawable getPageBackground() {
        return adapter.getDrawable();
    }

    /**
     * Sets page background
     *
     * @param drawable Drawable
     */
    public void setPageBackground(Drawable drawable) {
        this.pageBackground = drawable;
        if (adapter != null)
            adapter.setBackground(drawable);
    }

    /**
     * Returns the modulus Fling limit value
     *
     * @return int
     */
    public int getModFlingLimit() {
        return modFlingLimit;
    }

    /**
     * Sets the modulus Fling limit value
     * <br>
     * This value is used to stop rendering the page on user scroll. Smaller limit - smoother the page scroll
     * <br><br>
     * Example <br>
     * setModFlingLimit(1000); //Smoother Scroll<br>
     * setModFlingLimit(5000);
     *
     * @param modFlingLimit int
     */
    public void setModFlingLimit(int modFlingLimit) {
        this.modFlingLimit = modFlingLimit;
        if (adapter != null)
            adapter.setModFlingLimit(modFlingLimit);
    }

    /**
     * Action Listener for<br>
     * <ol><li>onStartLoad()</li>
     * <li>onLoaded()</li>
     * <li>onZoom(float scale)</li>
     * <li>onTotalPage(int totalPage)</li>
     * <li>onPageChanged(int currentPage, int totalPage)</li>
     * <li>onThemeChanged()</li></ol>
     *
     * @param actionListener OnActionListener
     * @see OnActionListener
     */
    public void addOnActionListener(OnActionListener actionListener) {
        this.listeners.add(actionListener);
    }

    /**
     * Removes the Action Listener
     *
     * @param listener OnActionListener
     */
    public void removeOnActionListener(OnActionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Return the maximum zoom scale
     */
    @FloatRange(from = 1f, to = 7f)
    public float getMaxZoomScale() {
        return recyclerView.getMaxScale();
    }

    /**
     * Sets the maximum zoom scale<br>
     * Range from 1f to 7f
     *
     * @param maxZoomScale float
     */
    public void setMaxZoomScale(@FloatRange(from = 1f, to = 7f) float maxZoomScale) {
        if (maxZoomScale > 7) throw new RuntimeException("Scale is too big");
        recyclerView.setMaxScaleFactor(maxZoomScale);
    }

    /**
     * Return the minimum zoom scale
     */
    @FloatRange(from = 0.1f, to = 1f)
    public float getMinZoomScale() {
        return recyclerView.getMinScale();
    }

    /**
     * Sets the minimum zoom scale<br>
     * Range from 0.1f to 1f
     *
     * @param minZoomScale float
     */
    public void setMinZoomScale(@FloatRange(from = 0.1f, to = 1f) float minZoomScale) {
        recyclerView.setMinScaleFactor(minZoomScale);
    }

    /**
     * Scroll to a specific Page number
     *
     * @param pageNo int
     */
    public void scrollToPage(int pageNo) {
        recyclerView.smoothScrollToPosition(pageNo - 1);
    }

    /**
     * returns true if zoom enabled
     *
     * @return boolean
     */
    public boolean isZoomEnabled() {
        return recyclerView.isZoomEnabled();
    }

    /**
     * Sets zoom enabled
     *
     * @param enabled boolean
     */
    public void setZoomEnabled(boolean enabled) {
        recyclerView.setZoomEnabled(enabled);
    }

    /**
     * Returns the quality of Pdf Page
     *
     * @return quality
     */
    @FloatRange(from = 0.01f, to = 1f)
    public float getQuality() {
        return quality;
    }

    /**
     * Sets the quality of Pdf Page
     *
     * @param quality Quality
     */
    public void setQuality(@FloatRange(from = 0.01f, to = 1f) float quality) {
        this.quality = quality;
    }

    /**
     * Deletes all the created temporary files passed to PdfView
     */
    public void recycle() {
        for (TemporaryFile file : temporaryFiles) {
            file.recycle();
        }
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
