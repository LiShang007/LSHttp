package com.lishang.http.callback;

import com.lishang.http.exception.LSHttpException;

public interface ResponseCallBack {

    void onFail(LSHttpException e);

}
