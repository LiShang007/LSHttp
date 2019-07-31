package com.lishang.okhttp.fragment;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lishang.http.LSHttp;
import com.lishang.http.callback.StringCallBack;
import com.lishang.http.callback.UploadCallBack;
import com.lishang.http.exception.LSHttpException;
import com.lishang.okhttp.MD5;
import com.lishang.okhttp.R;
import com.lishang.permissions.LSPermissions;
import com.lishang.permissions.listener.OnPermissionListener;
import com.lishang.permissions.listener.PermissionResult;

import java.io.File;

public class UploadFragment extends Fragment implements View.OnClickListener {


    private View view;
    private ProgressBar mProgressHorizontal;
    /**
     * 0%
     */
    private TextView mTxtProgress;
    /**
     * 结果
     */
    private TextView mTxtResult;
    /**
     * 上传
     */
    private Button mBtnUpload;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = View.inflate(getActivity(), R.layout.fragment_upload, null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO:OnCreateView Method has been created, run FindViewById again to generate code
        initView(view);
        return view;
    }

    public void initView(View view) {
        mProgressHorizontal = (ProgressBar) view.findViewById(R.id.progress_horizontal);
        mTxtProgress = (TextView) view.findViewById(R.id.txt_progress);
        mTxtResult = (TextView) view.findViewById(R.id.txt_result);
        mBtnUpload = (Button) view.findViewById(R.id.btn_upload);
        mBtnUpload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_upload:


                LSPermissions.request(this, new OnPermissionListener() {
                    @Override
                    public void onResult(PermissionResult result) {

                        if (result.granted) {
                            String url = "https://fix-apiweb.anchumall.cn/image/upload" + "?appversion="
                                    + "3.3.0"
                                    + "&os=android&sign=" + MD5.encoderByUrl("/image/upload");

                            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/123.jpg";
                            Log.e("path", "path:" + path);

                            LSHttp.multipart(url).addFile("image", new File(path))
                                    .addHeader("Authorization", "3c4dfe6f7a7caaa7ccca39ec157554f5")
                                    .progress(new UploadCallBack() {
                                        @Override
                                        public void onLoading(int progress) {
                                            mProgressHorizontal.setProgress(progress);
                                            mTxtProgress.setText("" + progress + "%");
                                        }
                                    })
                                    .callback(new StringCallBack() {
                                        @Override
                                        public void onSuccess(String string) {
                                            Log.e("onSuccess", string);
                                            mTxtResult.setText("请求成功:\n" + string);

                                        }

                                        @Override
                                        public void onFail(LSHttpException e) {
                                            mTxtResult.setText("请求失败:\n" + e.message);

                                        }
                                    }).execute(UploadFragment.this);

                        }

                    }
                }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

                break;
        }
    }
}
