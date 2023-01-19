package com.acutecoder.pdf;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Temporary file - used to copy asset or raw file to temporary folder<br><br>
 * Created by Bhuvaneshwaran
 * on 4:33 PM, 1/14/2023
 *
 * @author AcuteCoder
 */
@SuppressWarnings("unused")
public class TemporaryFile extends File {

    private int rawId;
    private boolean isRaw;
    private String tempPath;

    /**
     * File used to copy asset file to temporary folder
     */
    public TemporaryFile(@NonNull String pathname) {
        super(pathname);
    }

    /**
     * File used to copy raw file to temporary folder
     */
    public TemporaryFile(@RawRes int rawId) {
        super(String.valueOf(rawId));
        this.rawId = rawId;
        isRaw = true;
    }

    /**
     * Copy asset or raw file to the given temporary folder
     *
     * @param context  Required to get raw file name
     * @param tempFolderPath Required temporary dir path
     * @return tempFile
     * @throws IOException if operation fails
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File getTempFile(Context context, String tempFolderPath) throws IOException {
        tempPath = getTempPath(context, tempFolderPath);
        InputStream in;
        if (isRaw)
            in = context.getResources().openRawResource(rawId);
        else
            in = context.getAssets().open(toString());
        File file = new File(tempPath);
        if (file.exists()) return file;
        file.delete();
        Objects.requireNonNull(file.getParentFile()).mkdirs();
        file.createNewFile();
        copyFile(in, new FileOutputStream(tempPath));
        return file;
    }

    @NonNull
    private String getTempPath(Context context, String tempPath) {
        return tempPath + (isRaw ? getName(context) : getName());
    }

    /**
     * Deletes the created temporary file
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void recycle() {
        if (tempPath == null) return;
        File f = new File(tempPath);
        if (f.exists())
            f.delete();
    }

    private String getName(Context context) {
        String name = context.getResources().getResourceName(rawId);
        return name.substring(name.lastIndexOf("/") + 1).trim();
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.flush();
        out.close();
    }
}
