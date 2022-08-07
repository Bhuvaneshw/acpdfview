package com.acutecoder.pdf;

/*
 *Created by Bhuvaneshwaran
 *on 11:37 AM, 28-07-2022
 *AcuteCoder
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;

final class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.ViewHolder> {

    private final Context context;
    private PdfRenderer renderer;
    private float scale = 1f, width = 0;
	private int pageSpacing=20;
	private int color;

    public PdfAdapter(Context context, File file, PdfRecyclerView recycler, int color, int pageSpacing) {
        this.context = context;
		this.color = color;
		this.pageSpacing = pageSpacing;
        try {
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            renderer = new PdfRenderer(fd);
            recycler.setTotalPage(renderer.getPageCount());
        } catch (Exception ignored) {
        }
    }

    @SuppressLint("ResourceType")
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams")
			LinearLayout view = new LinearLayout(context);
        view.setPadding(pageSpacing, pageSpacing, pageSpacing, pageSpacing);
        ImageView imageView = new ImageView(context);
        imageView.setId(1001);
        imageView.setBackgroundColor(color);
        view.addView(imageView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (width > 0) {
            int width, height;
            PdfRenderer.Page page = renderer.openPage(position);
            @SuppressLint("ResourceType")
				ImageView imageView = holder.itemView.findViewById(1001);

            width = (int) (this.width * scale);
            height = page.getWidth();
            height = (int) ((float) width / (float) height * (float) page.getHeight());
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            ViewGroup.LayoutParams lp = imageView.getLayoutParams();
            lp.width = (int) (width / scale);
            lp.height = (int) (height / scale);
            holder.itemView.setPadding(pageSpacing, pageSpacing, pageSpacing, pageSpacing);
			imageView.setImageBitmap(bitmap);
            imageView.setBackgroundColor(color);
			page.close();
        }
    }

    void setScale(float scale) {
        this.scale = scale;
    }

    void setWidth(float width) {
        this.width = width - 2 * pageSpacing;
    }
	
	void setPdfColor(int color){
		this.color=color;
	}
	
	void setPdfSpacing(int spacing){
		this.pageSpacing = spacing;
	}

    @Override
    public int getItemCount() {
        return renderer != null ? renderer.getPageCount() : 0;
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
