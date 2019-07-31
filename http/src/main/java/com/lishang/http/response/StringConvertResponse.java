package com.lishang.http.response;

import com.lishang.http.callback.ResponseCallBack;
import com.lishang.http.callback.StringCallBack;
import com.lishang.http.exception.LSHttpException;

import java.io.IOException;

import okhttp3.Response;

public class StringConvertResponse implements IConvertResponse {
    StringCallBack callBack;

    @Override
    public void convert(Response response) {
        String string = null;
        try {
            string = response.body().string();
            if (callBack != null) {
                callBack.onSuccess(string);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (callBack != null) {
                callBack.onFail(LSHttpException.handleException(e));
            }
        }

    }

    @Override
    public void setCallBack(ResponseCallBack callBack) {
        this.callBack = (StringCallBack) callBack;
    }
}
