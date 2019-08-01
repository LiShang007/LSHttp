package com.lishang.http;


import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.lishang.http.lifecycle.LSHttpActivityLifecycleCallBacks;
import com.lishang.http.request.DownloadRequest;
import com.lishang.http.request.GetRequest;
import com.lishang.http.request.JsonRequest;
import com.lishang.http.request.MultipartRequest;
import com.lishang.http.request.PostRequest;
import com.lishang.http.utils.LSHttpLoggingInterceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;

public class LSHttp {
    private static class Holder {
        static LSHttp INSTANCE = new LSHttp();
    }

    private OkHttpClient mClient; //默认client
    private Application application;
    private Handler mHandler; //用于主线程
    private LSHttpActivityLifecycleCallBacks lifecycleCallBacks;
    private Map<String, String> headers = new HashMap<>(); //全局共有请求头参数

    /**
     * get 请求
     *
     * @param url
     * @return
     */
    public static GetRequest get(String url) {
        return new GetRequest().url(url).addHeaders(LSHttp.getInstance().headers);
    }

    /**
     * post 表单
     *
     * @param url
     * @return
     */
    public static PostRequest post(String url) {
        return new PostRequest().url(url).addHeaders(LSHttp.getInstance().headers);
    }

    public static DownloadRequest download(String url) {
        return new DownloadRequest(LSHttp.getInstance().mClient.newBuilder()).addHeaders(LSHttp.getInstance().headers).url(url);
    }

    public static MultipartRequest multipart(String url) {
        return new MultipartRequest().url(url).addHeaders(LSHttp.getInstance().headers);
    }

    /**
     * post json
     *
     * @param url
     * @return
     */
    public static JsonRequest json(String url) {
        return new JsonRequest().url(url).addHeaders(LSHttp.getInstance().headers);
    }

    public static void cancelAll() {
        if (LSHttp.getInstance().mClient == null) return;
        LSHttp.getInstance().mClient.dispatcher().cancelAll();
    }

    public static void cancel(String tag) {
        if (LSHttp.getInstance().mClient == null) return;
        if (TextUtils.isEmpty(tag)) return;
        for (Call call : LSHttp.getInstance().mClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }

        for (Call call : LSHttp.getInstance().mClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    public static LSHttp getInstance() {
        return Holder.INSTANCE;
    }

    private LSHttp() {
        mHandler = new Handler(Looper.getMainLooper());

        mClient = new OkHttpClient.Builder()
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(new LSHttpLoggingInterceptor().setLevel(LSHttpLoggingInterceptor.Level.BODY))
                .build();


    }

    /**
     * 设置全局请求头
     *
     * @param key
     * @param value
     * @return
     */
    public LSHttp addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    /**
     * 设置全局请求头
     *
     * @return
     */
    public LSHttp addHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    public LSHttp setClient(OkHttpClient client) {
        this.mClient = client;
        return this;
    }

    public static LSHttp init(Application application) {
        LSHttp http = getInstance();
        if (http.lifecycleCallBacks == null) {
            http.lifecycleCallBacks = new LSHttpActivityLifecycleCallBacks();
        }

        if (http.application != null) {
            http.application.unregisterActivityLifecycleCallbacks(http.lifecycleCallBacks);
        }

        http.application = application;

        http.application.registerActivityLifecycleCallbacks(http.lifecycleCallBacks);

        return http;
    }


    public OkHttpClient getClient() {
        return mClient;
    }

    public LSHttpActivityLifecycleCallBacks getLifecycleCallBacks() {
        return lifecycleCallBacks;
    }

    public void runOnMainThread(Runnable runnable) {
        mHandler.post(runnable);
    }

}
