package com.lishang.http.request;


import android.text.TextUtils;

import com.lishang.http.LSHttp;
import com.lishang.http.callback.JsonCallBack;
import com.lishang.http.lifecycle.LSHttpActivityLifecycleCallBacks;
import com.lishang.http.callback.ResponseCallBack;
import com.lishang.http.exception.LSHttpException;
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

public class BaseRequest<T extends BaseRequest> {
    protected OkHttpClient mClient;
    protected LSHttpActivityLifecycleCallBacks lifecycleCallBacks;
    protected String url; //请求url
    protected Map<String, String> headers = new HashMap<>(); //请求头参数
    protected String tag;//请求tag
    protected ResponseCallBack callBack;//请求回调

    public BaseRequest() {
        mClient = LSHttp.getInstance().getClient();
        lifecycleCallBacks = LSHttp.getInstance().getLifecycleCallBacks();
    }

    public ResponseCallBack getCallBack() {
        return callBack;
    }


    public T addHeader(String key, String value) {
        headers.put(key, value);
        return (T) this;
    }

    public T addHeaders(Map<String, String> map) {
        if (map != null) {
            headers.putAll(map);
        }
        return (T) this;
    }

    public T url(String url) {
        this.url = url;
        return (T) this;
    }

    public T tag(String tag) {
        this.tag = tag;
        return (T) this;
    }


    public T callback(ResponseCallBack callBack) {
        this.callBack = callBack;
        return (T) this;
    }

    public Headers createHeaders() {
        Headers.Builder builder = new Headers.Builder();
        for (String key : headers.keySet()) {
            builder.add(key, headers.get(key));
        }
        return builder.build();
    }

    public Request.Builder request() {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .tag(tag)
                .headers(createHeaders());
        return builder;
    }

    public void execute() {
        execute(null);
    }

    public void execute(final Object obj) {
        checkUrl();
        Request.Builder builder = request();

        OkHttpClient client = mClient;
        if (this instanceof PostRequest) {
            builder.post(((PostRequest) this).createFormBody());
        } else if (this instanceof JsonRequest) {
            builder.post(((JsonRequest) this).createJsonBody());
        } else if (this instanceof MultipartRequest) {
            builder.post(((MultipartRequest) this).createMultipartBody());
        }


        Call call = client.newCall(builder.build());

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

                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {

                        if (response.code() == 200) {
                            //请求成功
                            if (callBack != null) {

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
                            if (callBack != null) {
                                callBack.onFail(new LSHttpException(LSHttpException.ERROR.HTTP_ERROR, "请求失败，服务器开小差..." + response.code()));
                            }
                        }

                    }
                });
                removeLifecycle(obj, call);
            }
        });
    }

    public void checkUrl() {
        if (TextUtils.isEmpty(this.url)) {
            throw new NullPointerException("url is null");
        }
    }

    public void runOnMainThread(Runnable runnable) {
        LSHttp.getInstance().runOnMainThread(runnable);
    }


    protected void bindLifecycle(Object obj, Call call) {
        if (lifecycleCallBacks != null && obj != null) {
            lifecycleCallBacks.put(obj.getClass().getName(), call);
        }
    }

    protected void removeLifecycle(Object obj, Call call) {
        if (lifecycleCallBacks != null && obj != null) {
            lifecycleCallBacks.remove(obj.getClass().getName(), call);
        }
    }

}
