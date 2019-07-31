package com.lishang.okhttp.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;



import com.lishang.http.LSHttp;
import com.lishang.http.callback.JsonCallBack;
import com.lishang.http.callback.StringCallBack;
import com.lishang.http.callback.UploadCallBack;
import com.lishang.http.exception.LSHttpException;
import com.lishang.okhttp.JsonData;
import com.lishang.okhttp.MD5;
import com.lishang.okhttp.R;
import com.lishang.permissions.LSPermissions;
import com.lishang.permissions.listener.OnPermissionListener;
import com.lishang.permissions.listener.PermissionResult;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    private TextView mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        LSHttp.getInstance().setApplication(this.getApplication());


    }

    public void get(View view) {

        LSHttp.get("https://wanandroid.com/wxarticle/chapters/json")
                .callback(new JsonCallBack<JsonData>() {
                    @Override
                    public void onFail(LSHttpException e) {
                        mText.setText("请求失败:\n" + e.message);
                    }

                    @Override
                    public void onSuccess(JsonData data) {
                        Log.e("onSuccess", data.toString());
                        mText.setText("请求成功:\n" + data.toString());

                    }
                }).execute();

    }

    public void post(View view) {
        LSHttp.post("https://www.wanandroid.com/user/login")
                .addParams("username", "LiShang_King")
                .addParams("password", "2814274")
                .callback(new StringCallBack() {
                    @Override
                    public void onSuccess(String string) {
                        Log.e("onSuccess", string);
                        mText.setText("请求成功:\n" + string);

                    }

                    @Override
                    public void onFail(LSHttpException e) {
                        mText.setText("请求失败:\n" + e.message);

                    }
                }).execute();
    }

    private void initView() {
        mText = (TextView) findViewById(R.id.text);
    }

    public void download(View view) {

        startActivity(new Intent(this, DownloadActivity.class));


    }



    public void upload(View view) {

        startActivity(new Intent(this,UploadActivity.class));

    }

    public void uploadMore(View view) {


        LSPermissions.request(this, new OnPermissionListener() {
            @Override
            public void onResult(PermissionResult result) {

                if (result.granted) {
                    String url = "https://fix-apiweb.anchumall.cn/image/upload_multi" + "?appversion="
                            + "3.3.0"
                            + "&os=android&sign=" + MD5.encoderByUrl("/image/upload_multi");

                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/123.jpg";
                    Log.e("path", "path:" + path);

                    LSHttp.multipart(url).addFile("image_1", new File(path))
                            .addFile("image_2", new File(path))
                            .addHeader("Authorization", "3c4dfe6f7a7caaa7ccca39ec157554f5")
                            .progress(new UploadCallBack() {
                                @Override
                                public void onLoading(int progress) {
                                    mText.setText("上传中:\n" + progress);
                                }

                            })
                            .callback(new StringCallBack() {
                                @Override
                                public void onSuccess(String string) {
                                    Log.e("onSuccess", string);
                                    mText.setText("请求成功:\n" + string);

                                }

                                @Override
                                public void onFail(LSHttpException e) {
                                    mText.setText("请求失败:\n" + e.message);

                                }
                            }).execute();

                }

            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

    }

}
