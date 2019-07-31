package com.lishang.okhttp.activity;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.lishang.http.LSHttp;
import com.lishang.http.callback.DownloadCallBack;
import com.lishang.http.exception.LSHttpException;
import com.lishang.okhttp.R;
import com.lishang.permissions.LSPermissions;
import com.lishang.permissions.listener.OnPermissionListener;
import com.lishang.permissions.listener.PermissionResult;

public class DownloadActivity extends AppCompatActivity {

    private ProgressBar mProgressHorizontal;
    /**
     * 0%
     */
    private TextView mTxtProgress;
    /**
     * 结果
     */
    private TextView mTxtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        initView();
    }


    public void download(View view) {

        LSPermissions.request(this, new OnPermissionListener() {
            @Override
            public void onResult(PermissionResult result) {

                if (result.granted) {
                    String url = "https://6082fcfd683b09af1a65ea78561240f8.dd.cdntips.com/download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.ap";

                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/abcd/apk.apk";
                    Log.e("path", "path:" + path);
                    LSHttp.download(url).path(path).tag("down").callback(new DownloadCallBack() {
                        @Override
                        public void onStart() {
                            Log.e("onStart", "开始下载");
                            mTxtResult.setText("开始下载...");
                        }

                        @Override
                        public void onLoading(int progress) {
                            Log.e("onLoading", "下载中" + progress);
                            mProgressHorizontal.setProgress(progress);
                            mTxtProgress.setText("" + progress + "%");
                        }

                        @Override
                        public void onSuccess(String path) {
                            Log.e("onSuccess", "下载成功" + path);
                            mTxtResult.setText("下载成功" + path);
                        }

                        @Override
                        public void onFail(LSHttpException e) {
                            mTxtResult.setText("请求失败:\n" + e.message);
                        }
                    }).execute(DownloadActivity.this);

                }

            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);


    }


    public void pause(View view) {
        LSHttp.cancel("down");
    }

    private void initView() {
        mProgressHorizontal = (ProgressBar) findViewById(R.id.progress_horizontal);
        mTxtProgress = (TextView) findViewById(R.id.txt_progress);
        mTxtResult = (TextView) findViewById(R.id.txt_result);
    }
}
