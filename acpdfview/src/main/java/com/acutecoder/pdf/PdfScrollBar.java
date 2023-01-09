package com.acutecoder.pdf;

/*
 *Created by Bhuvaneshwaran
 *on 12:34 AM, 1/9/2023
 *AcuteCoder
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

@SuppressWarnings("unused")
public class PdfScrollBar extends LinearLayout {

    private View view;
    private PdfView pdfView;
    private int totalPage = 0;

    public PdfScrollBar(Context context) {
        super(context);
    }

    public PdfScrollBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PdfScrollBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        view = onCreateView(getContext());
        view.setOnTouchListener(new DragListener());
        removeAllViews();
        addView(view, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setGravity(Gravity.RIGHT);
    }

    public void attachTo(PdfView pdfView) {
        this.pdfView = pdfView;
        pdfView.addOnActionListener(new OnActionListener() {
            @Override
            public void onStartLoad() {
                init();
            }

            @Override
            public void onThemeChanged() {
                PdfScrollBar.this.onThemeChanged();
            }

            @Override
            public void onTotalPage(int totalPage) {
                onPageUpdate(view, 0, totalPage);
                post(() -> updateScrollPosition());
            }

            @Override
            public void onPageChanged(int currentPage, int totalPage) {
                onPageUpdate(view, currentPage, totalPage);
                post(() -> updateScrollPosition());
            }
        });
    }

    @SuppressLint("SetTextI18n")
    protected View onCreateView(Context context) {
        TextView view = new TextView(context);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(pdfView.isDarkMode() ? 0xff19282b : Color.WHITE);
        drawable.setStroke(1, 0xffeeeeee);
        drawable.setCornerRadii(new float[]{10, 10, 0, 0, 0, 0, 10, 10});
        view.setBackground(drawable);
        view.setTextColor(pdfView.isDarkMode() ? Color.WHITE : 0xff19282b);
        view.setPadding(20, 10, 20, 10);
        return view;
    }

    protected void onThemeChanged() {
        if (view == null) return;
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(pdfView.isDarkMode() ? 0xff19282b : Color.WHITE);
        drawable.setStroke(1, 0xffeeeeee);
        drawable.setCornerRadii(new float[]{10, 10, 0, 0, 0, 0, 10, 10});
        view.setBackground(drawable);
        ((TextView) view).setTextColor(pdfView.isDarkMode() ? Color.WHITE : 0xff19282b);
    }

    protected void onPageChange(View view, int currentPage, int totalPage) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            String newText = currentPage + "/" + totalPage;
            String oldText = textView.getText().toString();
            textView.setText(newText);
                if (oldText.length() == newText.length()) return;
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.width = (int) (2 * textView.getPaint().measureText(newText));
            textView.setLayoutParams(lp);
            view.setX(getWidth() - view.getWidth());
        }
    }

    protected void updateScrollPosition() {
        float offset = pdfView.getRecyclerView().computeVerticalScrollOffset();
        float extent = pdfView.getRecyclerView().computeVerticalScrollExtent();
        float range = pdfView.getRecyclerView().computeVerticalScrollRange();
        float percentage = offset / (range - extent);
        final float y = (getHeight() - view.getHeight()) * percentage;
        setViewY(y);
    }

    protected void setViewY(float y) {
        view.setY(y);
    }

    private void onChangeScroll(float y, boolean updatePdfView) {
        int fPos = (int) Math.floor(y * totalPage / getHeight());
        int cPos = (int) Math.ceil(y * totalPage / getHeight());
        onPageUpdate(view, fPos + 1, totalPage);
        if (updatePdfView) {
            pdfView.scrollToPage(cPos);
        }
    }

    private void onPageUpdate(View view, int currentPage, int totalPage) {
        this.totalPage = totalPage;
        onPageChange(view, currentPage, totalPage);
    }

    class DragListener implements View.OnTouchListener {
        boolean isDragging = false;
        float lastY = 0;
        float deltaY;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN && !isDragging) {
                isDragging = true;
                deltaY = event.getY();
                return true;
            } else if (isDragging) {
                if (action == MotionEvent.ACTION_MOVE) {
                    float y = view.getY() + event.getY() - deltaY;
                    if (y < 0) y = 0;
                    if (y >= getHeight() - view.getHeight())
                        y = getHeight() - view.getHeight();
                    setViewY(y);
                    pdfView.setScrolling();
                    onChangeScroll(view.getY(), false);
                    return true;
                } else if (action == MotionEvent.ACTION_UP) {
                    isDragging = false;
                    lastY = event.getY();
                    if (lastY < 0) lastY = 0;
                    if (lastY >= getHeight() - view.getHeight())
                        lastY = getHeight() - view.getHeight();
                    pdfView.setScrolling();
                    onChangeScroll(view.getY(), true);
                    return true;
                } else if (action == MotionEvent.ACTION_CANCEL) {
                    if (lastY < 0) lastY = 0;
                    if (lastY >= getHeight() - view.getHeight())
                        lastY = getHeight() - view.getHeight();
                    setViewY(lastY);
                    pdfView.setScrolling();
                    onChangeScroll(view.getY(), true);
                    isDragging = false;
                    return true;
                }
            }
            return false;
        }
    }
}
