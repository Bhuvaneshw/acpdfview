package com.acutecoder.pdf;

/*
 *Created by Bhuvaneshwaran
 *on 10:04 PM, 12/26/2022
 *AcuteCoder
 */

import android.os.Handler;
import android.os.Looper;

@SuppressWarnings("unused")
final class Task<T> {

    private static Handler handler;
    private final int position;
    private final Thread thread;
    private OnStateListener onStateListener;

    Task(BackgroundTask<T> backgroundTask, int position) {
        this(backgroundTask, null, position);
    }

    Task(BackgroundTask<T> backgroundTask, Result<T> result, int position) {
        this.position = position;
        thread = new Thread(() -> {
            T res = backgroundTask.run();
            if (handler == null) handler = new Handler(Looper.getMainLooper());
            if (onStateListener != null) onStateListener.onComplete(this);
            if (result != null)
                handler.post(() -> result.onResult(res));
        });
    }

    void setOnStateListener(OnStateListener onStateListener) {
        this.onStateListener = onStateListener;
    }

    void start() {
        if (onStateListener != null) onStateListener.onStart(this);
        try {
            thread.start();
        } catch (Exception ignored) {
        }
    }

    int getPosition() {
        return position;
    }

    interface BackgroundTask<T> {
        T run();
    }

    interface Result<T> {
        void onResult(T result);
    }

    @SuppressWarnings("all")
    interface OnStateListener {
        void onStart(Task task);

        void onComplete(Task task);
    }
}
