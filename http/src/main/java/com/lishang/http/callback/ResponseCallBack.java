package com.lishang.http.callback;

import com.lishang.http.exception.LSHttpException;

/**
 * 回调基类
 */
public interface ResponseCallBack {

    void onFail(LSHttpException e);

}
