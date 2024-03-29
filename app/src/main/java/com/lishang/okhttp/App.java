package com.lishang.okhttp;

import android.app.Application;

import com.lishang.http.LSHttp;
import com.lishang.http.utils.LSHttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //配置全局的OkHttp
        OkHttpClient mClient = new OkHttpClient.Builder()
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(new LSHttpLoggingInterceptor().setLevel(LSHttpLoggingInterceptor.Level.BODY))
                .build();
        //初始化
        LSHttp.init(this, mClient)
                .showLog(true)
                .addHeader("key", "value")
                .baseUrl("https://wanandroid.com/");



    }
}
