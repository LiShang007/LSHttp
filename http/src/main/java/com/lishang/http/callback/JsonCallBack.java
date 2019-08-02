package com.lishang.http.callback;

/**
 * json 实体类回调
 * @param <T>
 */
public interface JsonCallBack<T> extends ResponseCallBack {

    void onSuccess(T data);


}
