package com.lishang.http.response;

import com.lishang.http.LSHttp;
import com.lishang.http.callback.ResponseCallBack;
import com.lishang.http.callback.StringCallBack;
import com.lishang.http.exception.LSHttpException;

import java.io.IOException;

import okhttp3.Response;

/**
 * String 转换
 */
public class StringConvertResponse implements IConvertResponse {
    StringCallBack callBack;

    @Override
    public void convert(Response response) {
        try {
            final String string = response.body().string();
            LSHttp.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (callBack != null) {
                        callBack.onSuccess(string);
                    }
                }
            });

        } catch (final IOException e) {
            e.printStackTrace();
            LSHttp.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (callBack != null) {
                        callBack.onFail(LSHttpException.handleException(e));
                    }
                }
            });
        }

    }

    @Override
    public void setCallBack(ResponseCallBack callBack) {
        this.callBack = (StringCallBack) callBack;
    }
}
