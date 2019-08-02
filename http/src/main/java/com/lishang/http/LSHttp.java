package com.lishang.http;


import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.lishang.http.https.HttpsUtils;
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
    private Handler mHandler; //用于将请求抛到主线程
    private LSHttpActivityLifecycleCallBacks lifecycleCallBacks; //用于注册Activity生命周期
    private Map<String, String> headers = new HashMap<>(); //全局共有请求头参数
    private boolean isShowLog;//是否显示日志
    private String baseUrl; //baseUrl

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

    /**
     * 下载
     *
     * @param url
     * @return
     */
    public static DownloadRequest download(String url) {
        return new DownloadRequest(LSHttp.getInstance().mClient.newBuilder()).addHeaders(LSHttp.getInstance().headers).url(url);
    }

    /**
     * 上传
     *
     * @param url
     * @return
     */
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

    /**
     * 取消所有请求
     */
    public static void cancelAll() {
        if (LSHttp.getInstance().mClient == null) return;
        LSHttp.getInstance().mClient.dispatcher().cancelAll();
    }

    /**
     * 根据tag取消
     *
     * @param tag
     */
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


    /**
     * 初始化 只用调用一次就可以
     *
     * @param application
     * @return
     */
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

    public static LSHttp init(Application application, OkHttpClient client) {
        LSHttp http = init(application);
        http.mClient = client;
        return http;
    }


    public static LSHttp getInstance() {
        return Holder.INSTANCE;
    }

    private LSHttp() {
        //获取UI主线程Handler
        mHandler = new Handler(Looper.getMainLooper());


        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();

        //配置默认的Client
        mClient = new OkHttpClient.Builder()
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(new LSHttpLoggingInterceptor().setLevel(LSHttpLoggingInterceptor.Level.BODY))
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
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


    /**
     * 显示打印日志
     *
     * @param show
     * @return
     */
    public LSHttp showLog(boolean show) {
        this.isShowLog = show;
        return this;
    }

    /**
     * base url
     *
     * @param url
     * @return
     */
    public LSHttp baseUrl(String url) {
        this.baseUrl = url;
        return this;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public OkHttpClient getClient() {
        return mClient;
    }

    public LSHttpActivityLifecycleCallBacks getLifecycleCallBacks() {
        return lifecycleCallBacks;
    }


    public boolean isShowLog() {
        return isShowLog;
    }

    public void runOnMainThread(Runnable runnable) {
        mHandler.post(runnable);
    }

}
