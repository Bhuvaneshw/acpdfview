# ACPdfView
A simple light weight PDF View for Android. Written in Java.

## Screenshots

 <pre>
 <img src="screenshots/screenshot1.jpg" width="300" alt="screeonshot1">    <img src="screenshots/screenshot2.jpg" width="300" alt="screeonshot2">
</pre>

## Implementation
Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
	maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add the dependency
```
dependencies {
    implementation 'com.github.Bhuvaneshw:acpdfview:v1.0.0'
}
```
Change version with current release. [See Release](../../releases/)

Step 3. Declare View in xml
```
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#eeeeee">

    <com.acutecoder.pdf.PdfView
        android:id="@+id/pdfview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
	app:pageBackgroundColor="#ffffff"
	app:pageSpacing="10dp"
	app:maxZoom="5"
	app:minZoom="0.5"
	app:zoomDuration="500"/>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:layout_marginBottom="10dp"
        android:paddingLeft="15dp"
        android:paddingTop="10dp"
        android:paddingRight="20dp"
        android:paddingBottom="10dp"
        android:text="0/0"
        android:id="@+id/pageNo"
	android:background="@drawable/page_bg"
	tools:ignore="HardcodedText,RtlHardcoded" />

</RelativeLayout>
```

Step 4. Load in Activity
```
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
	//pdfView.setMinZoomScale(0.5f);
	//pdfView.setMaxZoomScale(5f);
	//pdfView.setPageBackgroundColor(Color.BLACK);
	//pdfView.setPageSpacing(50);
	//pdfView.setZoom(3);
        pdfView.load();
    }
}
```

## License
~~~
                     GNU GENERAL PUBLIC LICENSE
                       Version 3, 29 June 2007

 Copyright (C) 2007 Free Software Foundation, Inc. <https://fsf.org/>
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.

    ACPdfView  Copyright (C) 2022  Bhuvaneshwaran
    This program comes with ABSOLUTELY NO WARRANTY;
    This is free software, and you are welcome to redistribute it
    under certain conditions;

  The GNU General Public License does not permit incorporating your program
into proprietary programs.  If your program is a subroutine library, you
may consider it more useful to permit linking proprietary applications with
the library.  If this is what you want to do, use the GNU Lesser General
Public License instead of this License.  But first, please read
<https://www.gnu.org/licenses/why-not-lgpl.html>. 
~~~
