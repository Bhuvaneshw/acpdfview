package com.acutecoder.pdfsample;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.acutecoder.pdf.OnPageListener;
import com.acutecoder.pdf.PdfView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private PdfView pdfView;
    private TextView pageNo;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pdfView = findViewById(R.id.pdfview);
        pageNo = findViewById(R.id.pageNo);

        pdfView.setOnPageListener(new OnPageListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onTotalPage(int totalPage) {
                pageNo.setText("0/" + totalPage);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onPageChanged(int currentPage, int totalPage) {
                pageNo.setText(currentPage + "/" + totalPage);
            }
        });
        pdfView.setPath(new File(Environment.getExternalStorageDirectory() + "/pdf.pdf"));
//		pdfView.setMinZoomScale(0.5f);
//		pdfView.setMaxZoomScale(5f);
//		pdfView.setPageBackgroundColor(Color.BLACK);
//		pdfView.setPageSpacing(50);
//		pdfView.setZoom(3);
        pdfView.load();
    }
}
