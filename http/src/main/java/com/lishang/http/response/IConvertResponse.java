package com.lishang.http.response;

import com.lishang.http.callback.ResponseCallBack;

import okhttp3.Response;

/**
 * 转换器
 */
public interface IConvertResponse {
    /**
     * 转换
     * @param response
     */
    void convert(Response response);

    /**
     * 回调
     * @param callBack
     */
    void setCallBack(ResponseCallBack callBack);
}
