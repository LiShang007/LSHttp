package com.lishang.http.request;


import android.app.Activity;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.lishang.http.LSHttp;
import com.lishang.http.callback.JsonCallBack;
import com.lishang.http.lifecycle.LSHttpActivityLifecycleCallBacks;
import com.lishang.http.callback.ResponseCallBack;
import com.lishang.http.exception.LSHttpException;
import com.lishang.http.response.IConvertResponse;
import com.lishang.http.response.JsonConvertResponse;
import com.lishang.http.response.StringConvertResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 基础Request
 *
 * @param <T>
 */
public abstract class BaseRequest<T extends BaseRequest> {
    protected OkHttpClient mClient;
    protected LSHttpActivityLifecycleCallBacks lifecycleCallBacks;
    protected String url; //请求url
    protected Map<String, String> headers = new HashMap<>(); //请求头参数
    protected String tag;//请求tag
    protected ResponseCallBack callBack;//请求回调
    protected IConvertResponse convertResponse; //用于自定义转换器

    public BaseRequest() {
        mClient = LSHttp.getInstance().getClient();
        lifecycleCallBacks = LSHttp.getInstance().getLifecycleCallBacks();
    }

    public ResponseCallBack getCallBack() {
        return callBack;
    }

    /**
     * 添加请求header
     *
     * @param key
     * @param value
     * @return
     */
    public T addHeader(String key, String value) {
        headers.put(key, value);
        return (T) this;
    }

    /**
     * 添加多个header
     *
     * @param map
     * @return
     */
    public T addHeaders(Map<String, String> map) {
        if (map != null) {
            headers.putAll(map);
        }
        return (T) this;
    }

    /**
     * url
     *
     * @param url
     * @return
     */
    public T url(String url) {
        this.url = url;
        return (T) this;
    }

    /**
     * tag
     *
     * @param tag
     * @return
     */
    public T tag(String tag) {
        this.tag = tag;
        return (T) this;
    }

    /**
     * 请求结果返回
     *
     * @param callBack
     * @return
     */
    public T callback(ResponseCallBack callBack) {
        this.callBack = callBack;
        return (T) this;
    }

    /**
     * 转换器
     *
     * @param convertResponse
     * @return
     */
    public T convert(IConvertResponse convertResponse) {
        this.convertResponse = convertResponse;
        return (T) this;
    }

    /**
     * 创建headers
     *
     * @return
     */
    public Headers createHeaders() {
        Headers.Builder builder = new Headers.Builder();
        for (String key : headers.keySet()) {
            builder.add(key, headers.get(key));
        }
        return builder.build();
    }

    /**
     * 构建基础request.builder
     *
     * @return
     */
    public Request.Builder request() {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .tag(tag)
                .headers(createHeaders());
        return builder;
    }

    /**
     * 用于定制request 差异
     *
     * @param builder
     * @return
     */
    public abstract Request generateRequest(Request.Builder builder);

    /**
     * 异步执行网络请求
     */
    public void execute() {
        execute(null);
    }

    /**
     * 异步执行网络请求
     *
     * @param obj 支持Activity、Fragment
     */
    public void execute(final Object obj) {
        checkUrl();

        OkHttpClient client = mClient;


        Call call = client.newCall(generateRequest(request()));

        bindLifecycle(obj, call);


        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                e.printStackTrace();
                if (call.isCanceled()) {
                    System.out.println("call is canceled");
                    return;
                }
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (callBack != null) {
                            callBack.onFail(LSHttpException.handleException(e));
                        }
                    }
                });
                removeLifecycle(obj, call);

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                if (response.code() == 200) {
                    //请求成功
                    if (convertResponse != null) {
                        convertResponse.setCallBack(callBack);
                        convertResponse.convert(response);
                    } else if (callBack != null) {

                        if (callBack instanceof JsonCallBack) {
                            JsonConvertResponse convertResponse = new JsonConvertResponse();
                            convertResponse.setCallBack(callBack);
                            convertResponse.convert(response);
                        } else {
                            StringConvertResponse convertResponse = new StringConvertResponse();
                            convertResponse.setCallBack(callBack);
                            convertResponse.convert(response);
                        }
                    }
                } else {

                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (callBack != null) {
                                callBack.onFail(new LSHttpException(LSHttpException.ERROR.HTTP_ERROR, "请求失败，服务器开小差..." + response.code()));
                            }
                        }
                    });
                }
                removeLifecycle(obj, call);
            }
        });
    }

    /**
     * 检查url是否合法
     */
    public void checkUrl() {
        if (TextUtils.isEmpty(this.url)) {
            throw new NullPointerException("url is null");
        }
        String baseUrl = LSHttp.getInstance().getBaseUrl();
        if (!TextUtils.isEmpty(baseUrl)) {
            Uri uri = Uri.parse(url);
            if ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) {

            } else {
                url = baseUrl + url;
            }
        }
    }

    /**
     * 运行到主线程
     *
     * @param runnable
     */
    public void runOnMainThread(Runnable runnable) {
        LSHttp.getInstance().runOnMainThread(runnable);
    }

    /**
     * 绑定Activity、Fragment
     *
     * @param obj
     * @param call
     */
    public void bindLifecycle(Object obj, Call call) {
        if (lifecycleCallBacks != null && obj != null) {
            if (obj instanceof Activity || obj instanceof Fragment)
                lifecycleCallBacks.put(obj.getClass().getName(), call);
        }
    }

    /**
     * 移除绑定
     *
     * @param obj
     * @param call
     */
    public void removeLifecycle(Object obj, Call call) {
        if (lifecycleCallBacks != null && obj != null) {
            lifecycleCallBacks.remove(obj.getClass().getName(), call);
        }
    }

    public OkHttpClient getClient() {
        return mClient;
    }

    public LSHttpActivityLifecycleCallBacks getLifecycleCallBacks() {
        return lifecycleCallBacks;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getTag() {
        return tag;
    }

    public IConvertResponse getConvertResponse() {
        return convertResponse;
    }
}
