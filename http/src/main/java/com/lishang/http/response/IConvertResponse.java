package com.lishang.http.response;

import com.lishang.http.callback.ResponseCallBack;

import okhttp3.Response;

public interface IConvertResponse {

    void convert(Response response);

    void setCallBack(ResponseCallBack callBack);
}
