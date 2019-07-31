package com.lishang.http.callback;


public interface JsonCallBack<T> extends ResponseCallBack {

    void onSuccess(T data);


}
