package com.lishang.okhttp.convert;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.lishang.http.LSHttp;
import com.lishang.http.callback.ResponseCallBack;
import com.lishang.http.response.IConvertResponse;

import java.io.InputStream;

import okhttp3.Response;

public class BitmapConvertResponse implements IConvertResponse {
    BitmapCallBack callBack;

    @Override
    public void convert(Response response) {
        InputStream inputStream = response.body().byteStream();//得到图片的流
        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        //子线程中
        LSHttp.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onSuccess(bitmap);
                }
            }
        });
    }

    @Override
    public void setCallBack(ResponseCallBack callBack) {
        this.callBack = (BitmapCallBack) callBack;
    }
}
