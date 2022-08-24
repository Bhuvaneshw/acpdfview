package com.acutecoder.pdfview;

/*
 *Created by Bhuvaneshwaran
 *on 11:37 AM, 28-07-2022
 *AcuteCoder
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.ViewHolder> {

    private final Context context;
    private PdfRenderer renderer;
    private float scale = 1f, width = 0;
    private String textToFind = "";

    public PdfAdapter(Context context, File file, ZoomableRecyclerView recycler) {
        this.context = context;
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
        view.setPadding(20, 20, 20, 20);
        ImageView imageView = new ImageView(context);
        imageView.setId(1001);
        imageView.setBackgroundColor(Color.WHITE);
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
            Bitmap t = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            if (textToFind != null && !textToFind.equals("")) bitmap = spotText(bitmap);
            if (bitmap == null) bitmap = t;
            imageView.setImageBitmap(bitmap);
            page.close();
        }
    }

    private Bitmap spotText(Bitmap bitmap) {
        Bitmap tempBitmap = null;
        //Convert The Image To Bitmap
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        if (!textRecognizer.isOperational()) {
            Log.e("ocr", "Dependencies not available");
            // Check android for low storage so dependencies can be loaded, DEPRECATED CHANGE LATER
            IntentFilter intentLowStorage = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = context.registerReceiver(null, intentLowStorage) != null;
            if (hasLowStorage) {
                Toast.makeText(context, "Low Memory On Disk", Toast.LENGTH_LONG).show();
                Log.e("ocr", "Low Memory On Disk");
            }
        } else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);
            List<TextBlock> blocks = new ArrayList<TextBlock>();
            TextBlock myItem = null;
            for (int i = 0; i < items.size(); ++i) {
                myItem = (TextBlock) items.valueAt(i);
                //Add All TextBlocks to the `blocks` List
                blocks.add(myItem);
            }
            //END OF DETECTING TEXT
            //The Color of the Rectangle to Draw on top of Text
            Paint rectPaint = new Paint();
            rectPaint.setColor(Color.RED);
            rectPaint.setStyle(Paint.Style.STROKE);
            rectPaint.setStrokeWidth(4.0f);
            //Create the Canvas object,
            //Which ever way you do image that is ScreenShot for example, you
            //need the views Height and Width to draw rectangles
            //because the API detects the position of Text on the View
            //So Dimensions are important for Draw method to draw at that Text
            //Location
            tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(tempBitmap);
            canvas.drawBitmap(bitmap, 0, 0, null);
            //Loop through each `Block`
            for (TextBlock textBlock : blocks) {
                List<? extends Text> textLines = textBlock.getComponents();
                //loop Through each `Line`
                for (Text currentLine : textLines) {
                    List<? extends Text> words = currentLine.getComponents();
                    //Loop through each `Word`
                    for (Text currentword : words) {
                        if (!currentword.getValue().toLowerCase(Locale.ROOT).startsWith(textToFind))
                            continue;
                        //Get the Rectangle/boundingBox of the word
                        RectF rect = new RectF(currentword.getBoundingBox());
                        rectPaint.setColor(Color.RED);
                        //Finally Draw Rectangle/boundingBox around word
                        canvas.drawRect(rect, rectPaint);
                        //Set image to the `View`
                        //imageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
                    }
                }
            }
        }
        return tempBitmap;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setWidth(float width) {
        this.width = width - 40;
    }

    @Override
    public int getItemCount() {
        return renderer.getPageCount();
    }

    public void findText(String text) {
        textToFind = text;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
