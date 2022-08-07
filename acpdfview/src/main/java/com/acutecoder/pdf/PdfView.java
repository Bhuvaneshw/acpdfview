package com.acutecoder.pdf;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

/**
 * Created by Bhuvaneshwaran
 * on 12:36 AM, 30-07-2022
 * AcuteCoder
 */

@SuppressLint("NotifyDataSetChanged")
public class PdfView extends FrameLayout {

    private PdfRecyclerView recyclerView;
    private PdfAdapter adapter;
    private OnPageListener pageListener;
	private OnZoomListener zoomListener;
    private int totPage, pdfSpacing;
	private int pdfColor;

    public PdfView(@NonNull Context context) {
        super(context);
        init(null);
    }

    public PdfView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PdfView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
		float scale = 1f, maxZoom=5f, minZoom=0.8f;
		boolean zoomEnabled=true;
		int zoomDuration=300;
        pdfColor = Color.WHITE;
		pdfSpacing = 20;
		if (attrs != null) {
			TypedArray attributes = getContext().obtainStyledAttributes(
				attrs, R.styleable.PdfView);
			scale = attributes.getFloat(R.styleable.PdfView_zoom, scale);
			maxZoom = attributes.getFloat(R.styleable.PdfView_maxZoom, maxZoom);
			minZoom = attributes.getFloat(R.styleable.PdfView_minZoom, minZoom);
			zoomEnabled = attributes.getBoolean(R.styleable.PdfView_zoomEnabled, zoomEnabled);
			zoomDuration = attributes.getInteger(R.styleable.PdfView_zoomDuration, zoomDuration);
			pdfColor = attributes.getColor(R.styleable.PdfView_pageBackgroundColor, pdfColor);
			pdfSpacing = attributes.getDimensionPixelSize(R.styleable.PdfView_pageSpacing, pdfSpacing);
			attributes.recycle();
		}

		recyclerView = new PdfRecyclerView(getContext(), scale);
   		final PdfLayoutManager layoutManager = new PdfLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setZoomEnabled(zoomEnabled);
        recyclerView.setMaxScaleFactor(maxZoom);
        recyclerView.setMinScaleFactor(minZoom);
		recyclerView.setZoomDuration(zoomDuration);
		
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
				@Override
				public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
					super.onScrollStateChanged(recyclerView, newState);
				}

				@Override
				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
					if (pageListener != null)
						pageListener.onPageChanged(getMaximumVisibleViewPosition(layoutManager) + 1, totPage);
				}
			});
        recyclerView.setListener(new PdfRecyclerView.Listener() {
				@Override
				public void onZoom(float scale) {
					adapter.setScale(scale);
					adapter.notifyDataSetChanged();
					if (zoomListener != null) zoomListener.onZoom(scale);
				}

				@Override
				public void setTotalPage(int totalPage) {
					totPage = totalPage;
					if (pageListener != null) pageListener.onTotalPage(totalPage);
				}
			});
        addView(recyclerView);
    }

    public void setPath(File file) {
        adapter = new PdfAdapter(getContext(), file, recyclerView, pdfColor, pdfSpacing);
		adapter.setPdfColor(pdfColor);
		adapter.setPdfSpacing(pdfSpacing);
		recyclerView.setAdapter(adapter);
        load();
    }

    public void load() {
		refresh();
	}

	public void refresh() {
		recyclerView.postInvalidate();
		adapter.notifyDataSetChanged();
    }

    public void setZoomEnabled(boolean enabled) {
        recyclerView.setZoomEnabled(enabled);
    }

	public boolean isZoomEnabled() {
		return recyclerView.isZoomEnabled();
	}

	public void setZoom(float zoom) {
		recyclerView.setZoom(zoom);
	}

	public float getZoom() {
		return recyclerView.getZoom();
	}

    public void setMaxZoomScale(float maxZoomScale) {
        if (maxZoomScale > 5) throw new RuntimeException("Scale is too big");
        recyclerView.setMaxScaleFactor(maxZoomScale);
    }

	public float getMaxZoomScale() {
		return recyclerView.getMaxZoomScale();
	}

    public void setMinZoomScale(float minZoomScale) {
        recyclerView.setMinScaleFactor(minZoomScale);
    }

	public float getMinZoomScale() {
		return recyclerView.getMinZoomScale();
	}

	public int getNoOfPages() {
		return totPage;
	}

	public void setPageSpacing(int pageSpacing) {
		this.pdfSpacing = pageSpacing;
		((PdfAdapter)recyclerView.getAdapter()).setPdfSpacing(pageSpacing);
	}

	public int getPageSpacing() {
		return pdfSpacing;
	}

	public void setPageBackgroundColor(int pageBgColor) {
		this.pdfColor = pageBgColor;
		((PdfAdapter)recyclerView.getAdapter()).setPdfColor(pageBgColor);
	}

	public int getPageBackgroundColor() {
		return pdfColor;
	}

	public void setScaleAnimationDuration(int duration) {
		recyclerView.setScaleDuration(duration);
	}

	public int getScaleAnimationDuration() {
		return recyclerView.getScaleDuration();
	}

    public void setOnPageListener(OnPageListener pageListener) {
        this.pageListener = pageListener;
    }

    public void setOnZoomListener(OnZoomListener zoomListener) {
        this.zoomListener = zoomListener;
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
}
