package com.acutecoder.pdf;

/*
 *Created by Bhuvaneshwaran
 *on 11:37 AM, 28-07-2022
 *AcuteCoder
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

@SuppressWarnings("unused")
final class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.ViewHolder>
        implements PdfRecyclerView.OnFlingChangeListener {

    private static final float[] NEGATIVE = {
            -1.0f, 0, 0, 0, 255, // red
            0, -1.0f, 0, 0, 255, // green
            0, 0, -1.0f, 0, 255, // blue
            0, 0, 0, 1.0f, 0  // alpha
    };
    private final Context context;
    private final PdfRecyclerView recyclerView;
    private final TaskHandler handler;
    private PdfRenderer detailsRenderer, pageRenderer;
    private boolean isDarkMode;
    private float scale = 1f, width = 0;
    private boolean fling = false;
    private int modFlingLimit = 3000;
    private Drawable drawable;

    @SuppressLint("NotifyDataSetChanged")
    public PdfAdapter(Context context, PdfRecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
        handler = new TaskHandler();
    }

    public void setFile(File file) {
        try {
            if (file instanceof TemporaryFile)
                file = ((TemporaryFile) file).getTempFile(context, context.getCacheDir() + "/PdfView/");
            detailsRenderer = new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
            pageRenderer = new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
            recyclerView.setTotalPage(detailsRenderer.getPageCount());
        } catch (Exception e) {
            Log.e("adp", e.toString());
        }
    }

    @NonNull
    @Override
    @SuppressLint("ResourceType,InflateParams")
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout view = new LinearLayout(context);
        view.setPadding(20, 20, 20, 20);
        View imageView = new ImageView(context);
        imageView.setId(1001);
        view.addView(imageView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new ViewHolder(view);
    }

    @Override
    @SuppressLint("ResourceType")
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        if (width > 0 && detailsRenderer != null) {
            final int position = pos;
            PdfRenderer.Page page = detailsRenderer.openPage(position);
            final ImageView imageView = holder.itemView.findViewById(1001);
            imageView.setImageDrawable(null);
            imageView.setBackground(drawable.mutate());

            final int width = (int) (this.width * scale);
            final int height = (int) ((float) width / (float) page.getWidth() * (float) page.getHeight());
            ViewGroup.LayoutParams lp = imageView.getLayoutParams();
            lp.width = (int) (width / scale);
            lp.height = (int) (height / scale);
            page.close();
            holder.setSkipped(fling);
            if (!fling) {
                handler.add(new Task<>(() -> {
                    try {
                        final PdfRenderer.Page page2 = pageRenderer.openPage(position);
                        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        page2.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                        page2.close();
                        Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
                        if (isDarkMode)
                            drawable.setColorFilter(new ColorMatrixColorFilter(NEGATIVE));
                        imageView.setImageDrawable(drawable);
                    } catch (Exception ignored) {
                    }
                    return null;
                }, position));
                if (!handler.isRunning())
                    handler.startAvailableTask();
            }
        }
    }

    @Override
    public int getItemCount() {
        try {
            return detailsRenderer.getPageCount();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void onFling(PdfRecyclerView recyclerView, int velocity) {
        if (Math.abs(velocity) > modFlingLimit) {
            fling = true;
        }
    }

    @Override
    public void onScroll(PdfRecyclerView recyclerView) {
        int start = recyclerView.findFirstVisiblePosition();
        int end = recyclerView.findLastVisiblePosition();
        for (; start <= end; start++) {
            ViewHolder holder = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(start);
            if (holder != null && holder.isSkipped()) {
                onBindViewHolder(holder, start);
            }
        }
    }

    @Override
    public void onStop(PdfRecyclerView recyclerView) {
        if (fling) {
            fling = false;
            refresh(recyclerView);
        }
    }

    int getModFlingLimit() {
        return modFlingLimit;
    }

    void setModFlingLimit(int modFlingLimit) {
        this.modFlingLimit = Math.abs(modFlingLimit);
    }

    boolean isDarkMode() {
        return isDarkMode;
    }

    void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
        drawable = new ColorDrawable(isDarkMode ? 0xff111111 : 0xffffffff);
    }

    Drawable getDrawable() {
        return drawable;
    }

    void setScale(float scale) {
        this.scale = scale;
    }

    void setWidth(float width) {
        this.width = width - 40;
    }

    public void refresh(PdfRecyclerView recyclerView) {
        int start = recyclerView.findFirstVisiblePosition();
        int end = recyclerView.findLastVisiblePosition();
        refreshViewHolder(recyclerView, start, end);
    }

    void setBackground(Drawable drawable) {
        this.drawable = drawable;
    }

    float getScale() {
        return scale;
    }

    private void refreshViewHolder(PdfRecyclerView recyclerView, int start, int end) {
        for (; start <= end; start++) {
            refreshViewHolder(recyclerView, start);
        }
    }

    private void refreshViewHolder(PdfRecyclerView recyclerView, int pos) {
        ViewHolder holder = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(pos);
        if (holder != null && holder.isSkipped())
            onBindViewHolder(holder, pos);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private boolean isSkipped = false;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public boolean isSkipped() {
            return isSkipped;
        }

        public void setSkipped(boolean skipped) {
            isSkipped = skipped;
        }
    }

}
