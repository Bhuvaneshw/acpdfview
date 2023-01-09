package com.acutecoder.pdf;

/*
 *Created by Bhuvaneshwaran
 *on 9:56 PM, 1/9/2023
 *AcuteCoder
 */

@SuppressWarnings("unused")
public interface OnActionListener {

    default void onStartLoad() {
    }

    default void onLoaded() {
    }

    default void onZoom(float scale) {
    }

    default void onTotalPage(int totalPage) {
    }

    default void onPageChanged(int currentPage, int totalPage) {
    }

    default void onThemeChanged() {
    }
}