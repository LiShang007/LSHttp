package com.lishang.okhttp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lishang.http.LSHttp;
import com.lishang.http.callback.JsonCallBack;
import com.lishang.http.callback.StringCallBack;
import com.lishang.http.exception.LSHttpException;
import com.lishang.okhttp.JsonData;
import com.lishang.okhttp.R;
import com.lishang.okhttp.convert.BitmapCallBack;
import com.lishang.okhttp.convert.BitmapConvertResponse;


public class MainActivity extends AppCompatActivity {

    private TextView mText;
    private ImageView mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();


    }

    public void get(View view) {

        LSHttp.get("wxarticle/chapters/json")
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
        LSHttp.post("user/login")
                .addParams("username", "xxxx")
                .addParams("password", "xxxxx")
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
        mImage = (ImageView) findViewById(R.id.image);
    }

    public void download(View view) {

        startActivity(new Intent(this, DownloadActivity.class));


    }


    public void upload(View view) {

        startActivity(new Intent(this, UploadActivity.class));

    }


    public void convert(View view) {
        LSHttp.get("https://www.33lc.com/article/UploadPic/2012-7/201272510182494484.jpg")
                .convert(new BitmapConvertResponse())
                .callback(new BitmapCallBack() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        if (bitmap != null) {
                            mImage.setImageBitmap(bitmap);
                        }
                    }

                    @Override
                    public void onFail(LSHttpException e) {

                    }
                }).execute(this);

    }
}
