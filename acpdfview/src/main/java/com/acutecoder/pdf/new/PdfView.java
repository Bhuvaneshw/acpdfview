package com.acutecoder.pdfview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Bhuvaneshwaran
 * on 12:36 AM, 30-07-2022
 * AcuteCoder
 */

@SuppressLint("NotifyDataSetChanged")
public class PdfView extends FrameLayout {

    private ZoomableRecyclerView recyclerView;
    private PdfAdapter adapter;
    private OnActionListener listener;
    private int totPage;

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

    public PdfView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setOnActionListener(OnActionListener actionListener) {
        this.listener = actionListener;
    }

    private void init() {
        recyclerView = new ZoomableRecyclerView(getContext());
        ZoomableLinearLayoutManager layoutManager = new ZoomableLinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setZoomEnabled(true);
        recyclerView.setMaxScaleFactor(5);
        recyclerView.setMinScaleFactor(0.9f);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (listener != null)
                    listener.onPageChanged(getMaximumVisibleViewPosition(layoutManager) + 1, totPage);
            }
        });
        recyclerView.setOnZoomListener(new ZoomableRecyclerView.Listener() {
            @Override
            public void onZoom(float scale) {
                adapter.setScale(scale * 0.8f);
                adapter.notifyDataSetChanged();
                if (listener != null) listener.onZoom(scale);
            }

            @Override
            public void onLoadingFinished() {
                if (listener != null) listener.onLoaded();
            }

            @Override
            public void setTotalPage(int totalPage) {
                totPage = totalPage;
                if (listener != null) listener.onTotalPage(totalPage);
            }
        });
        addView(recyclerView);
    }

    public void setPath(File file) {
        if (listener != null) listener.onStartLoad();
        adapter = new PdfAdapter(getContext(), file, recyclerView);
        recyclerView.setAdapter(adapter);
        load();
    }

    public void load() {
        post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void setZoomEnabled(boolean enabled) {
        recyclerView.setZoomEnabled(enabled);
    }

    public void setMaxZoomScale(float maxZoomScale) {
        if (maxZoomScale > 7) throw new RuntimeException("Scale is too big");
        recyclerView.setMaxScaleFactor(maxZoomScale);
    }

    public void setMinZoomScale(float minZoomScale) {
        recyclerView.setMinScaleFactor(minZoomScale);
    }

    private int getMaximumVisibleViewPosition(LinearLayoutManager layoutManager) {
        int firstItemPosition = layoutManager.findFirstVisibleItemPosition();
        int lastItemPosition = layoutManager.findLastVisibleItemPosition();
        int mostVisibleItemPosition = firstItemPosition, maxPercentage = 0;
        for (int i = 0; i <= lastItemPosition; i++) {
            View view = layoutManager.findViewByPosition(i);
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

    public void findText(String text) {
        adapter.findText(text);
    }

    public static class OnActionListener {

        protected void onStartLoad() {
        }

        protected void onLoaded() {
        }

        protected void onZoom(float scale) {
        }

        protected void onTotalPage(int totalPage) {
        }

        protected void onPageChanged(int currentPage, int totalPage) {
        }
    }
}
