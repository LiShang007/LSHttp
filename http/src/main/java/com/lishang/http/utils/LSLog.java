package com.lishang.http.utils;

import android.util.Log;

import com.lishang.http.LSHttp;

public class LSLog {

    private static final String TAG = "LSHttp";

    public static void e(String msg) {
        if (LSHttp.getInstance().isShowLog())
            Log.e(TAG, msg);
    }


    public static void i(String msg) {
        if (LSHttp.getInstance().isShowLog())
            Log.i(TAG, msg);
    }

}
