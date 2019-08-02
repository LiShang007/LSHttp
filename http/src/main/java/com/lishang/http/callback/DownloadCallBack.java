package com.lishang.http.callback;

/**
 * 下载回调
 */
public interface DownloadCallBack extends ResponseCallBack {
    //开始下载
    void onStart();

    //下载中
    void onLoading(int progress);

    void onSuccess(String path);

}
