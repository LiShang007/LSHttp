package com.lishang.okhttp.convert;

import android.graphics.Bitmap;

import com.lishang.http.callback.ResponseCallBack;

public interface BitmapCallBack extends ResponseCallBack {
    void onSuccess(Bitmap bitmap);
}
