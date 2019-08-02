package com.lishang.http.response;

import com.google.gson.Gson;
import com.lishang.http.LSHttp;
import com.lishang.http.callback.JsonCallBack;
import com.lishang.http.callback.ResponseCallBack;
import com.lishang.http.exception.LSHttpException;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Response;

/**
 * json 转换
 */
public class JsonConvertResponse implements IConvertResponse {
    JsonCallBack callBack;

    Type type;


    @Override
    public void convert(Response response) {
        String string = null;
        try {

            string = response.body().string();
            final Object data = new Gson().fromJson(string, type);
            LSHttp.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (callBack != null) {
                        callBack.onSuccess(data);
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
        this.callBack = (JsonCallBack) callBack;

        Type interfacesType = callBack.getClass().getGenericInterfaces()[0];
        Type genericType = ((ParameterizedType) interfacesType).getActualTypeArguments()[0];

        type = genericType;
    }
}
