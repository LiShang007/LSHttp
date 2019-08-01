package com.lishang.http.request;

import android.net.Uri;
import android.text.TextUtils;

import com.lishang.http.callback.DownloadCallBack;
import com.lishang.http.exception.LSHttpException;
import com.lishang.http.utils.LSLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class DownloadRequest extends BaseRequest<DownloadRequest> {
    private OkHttpClient mClient;
    private String downFilePath; //下载文件路径
    private DownloadCallBack downloadCallBack;
    private DownloadFile downloadFile;
    private long startPos;//用于断点续传

    public DownloadRequest(OkHttpClient.Builder builder) {

        final ProgressListener listener = new ProgressListener() {
            boolean firstUpdate = true;

            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                if (done) {
                    System.out.println("completed");
                    success(downloadFile.file.getAbsolutePath());
                    downloadFile.close();
                } else {
                    if (firstUpdate) {
                        firstUpdate = false;
                        if (contentLength == -1) {
                            System.out.println("content-length: unknown");
                        } else {
                            System.out.format("content-length: %d\n", contentLength);
                        }
                        start();//开始下载

                    }

                    if (contentLength != -1) {
                        int progress = (int) ((100 * (bytesRead + startPos)) / (contentLength + startPos));
                        System.out.format("%d%% done\n", progress);

                        loading(progress);

                    }
                }

            }

            @Override
            public void writeTo(byte[] bytes, int off, int len) {
                try {
                    downloadFile.write(bytes, off, len);
                } catch (IOException e) {
                    e.printStackTrace();
                    downloadFile.cancel();
                    error(new LSHttpException(LSHttpException.ERROR.HTTP_ERROR, "文件下载失败"));
                }
            }

        };

        this.mClient = builder
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {

                        Response originalResponse = chain.proceed(chain.request());

                        if (originalResponse.isSuccessful()) {
                            return originalResponse.newBuilder()
                                    .body(new ProgressResponseBody(originalResponse.body(), listener))
                                    .build();
                        } else {
                            return originalResponse;
                        }


                    }
                })
                .build();
    }

    public DownloadRequest path(String path) {
        this.downFilePath = path;
        return this;
    }


    @Override
    public void execute(final Object obj) {

        checkUrl();

        if (getCallBack() != null) {
            downloadCallBack = (DownloadCallBack) getCallBack();
        }

        if (TextUtils.isEmpty(downFilePath)) {
            downloadCallBack.onFail(new LSHttpException(LSHttpException.ERROR.UNKNOWN, "下载路径为空"));
            return;
        }


        try {
            downloadFile = new DownloadFile(downFilePath, url);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            downloadCallBack.onFail(new LSHttpException(LSHttpException.ERROR.UNKNOWN, "下载路径为空"));
            return;
        }

        startPos = downloadFile.size();
        Request.Builder builder = request()
                .addHeader("RANGE", "bytes=" + startPos + "-");


        final Call call = mClient.newCall(builder.build());
        downloadFile.setCall(call);

        bindLifecycle(obj, call);


        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                e.printStackTrace();

                if (call.isCanceled()) {
                    System.out.println("call is canceled");
                    return;
                }

                error(LSHttpException.handleException(e));

                removeLifecycle(obj, call);

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                removeLifecycle(obj, call);

                if (response.body().contentLength() == 0) {
                    success(downloadFile.file.getAbsolutePath());
                    return;
                }

                if (!response.isSuccessful()) {
                    error(new LSHttpException(LSHttpException.ERROR.HTTP_ERROR, "请求失败，服务器开小差..." + response.code()));
                    return;
                }

                // 保存文件到本地
//                InputStream is = null;
//                RandomAccessFile randomAccessFile = null;
//                BufferedInputStream bis = null;
//                byte[] buff = new byte[2048];
//                int len = 0;
//                try {
//                    is = response.body().byteStream();
//                    bis = new BufferedInputStream(is);
//
//                    File file = new File(downFilePath);
//                    if (!file.exists()) {
//                        if (!downFilePath.contains(".")) {
//                            //不包含.默认文件夹
//                            file.mkdirs();
//                        }
//                    }
//                    if (file.isDirectory()) {
//
//                        //如果是目录
//                        List<String> list = call.request().url().pathSegments();
//                        String name = list.size() != 0 ? list.get(list.size() - 1) : System.currentTimeMillis() + "";
//                        file = new File(downFilePath, name);
//                    }
//                    // 随机访问文件，可以指定断点续传的起始位置
//                    randomAccessFile = new RandomAccessFile(file, "rwd");
//                    randomAccessFile.seek(0);
//                    while ((len = bis.read(buff)) != -1) {
//                        randomAccessFile.write(buff, 0, len);
//                    }
//                    //下载成功
//                    loading(100);
//                    success(file.getAbsolutePath());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    //下载失败
//                    error(e);
//                } finally {
//                    try {
//                        if (is != null) {
//                            is.close();
//                        }
//                        if (bis != null) {
//                            bis.close();
//                        }
//                        if (randomAccessFile != null) {
//                            randomAccessFile.close();
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }

            }

        });

    }

    private void start() {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (downloadCallBack != null) {
                    //开始下载
                    downloadCallBack.onStart();
                }
            }
        });
    }

    private void loading(final int progress) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (downloadCallBack != null) {
                    //下载成功
                    downloadCallBack.onLoading(progress);
                }
            }
        });
    }

    private void success(final String path) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (downloadCallBack != null) {
                    //下载成功
                    downloadCallBack.onSuccess(path);
                }
            }
        });
    }

    private void error(final Exception e) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (downloadCallBack != null) {
                    //下载失败
                    if (e instanceof LSHttpException) {
                        downloadCallBack.onFail((LSHttpException) e);
                    } else {
                        downloadCallBack.onFail(LSHttpException.handleException(e));
                    }
                }
            }
        });
    }


    private static class ProgressResponseBody extends ResponseBody {

        private final ResponseBody responseBody;
        private final ProgressListener progressListener;
        private BufferedSource bufferedSource;

        ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;

        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    boolean done = bytesRead == -1;
                    if (!done) {
                        byte[] bytes = sink.readByteArray();
                        progressListener.writeTo(bytes, 0, bytes.length);
                    }

                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;

                    LSLog.i("read size:" + totalBytesRead);


                    progressListener.update(totalBytesRead, responseBody.contentLength(), done);
                    return bytesRead;
                }
            };
        }
    }

    interface ProgressListener {
        void update(long bytesRead, long contentLength, boolean done);

        void writeTo(byte[] bytes, int off, int len);
    }


    private class DownloadFile {
        RandomAccessFile randomAccessFile;
        File file;
        Call call;

        public DownloadFile(String path, String url) throws FileNotFoundException {
            file = new File(path);

            if (!file.exists()) {
                if (!path.contains(".")) {
                    //不包含.默认文件夹
                    file.mkdirs();
                }
            }
            if (file.isDirectory()) {
                //如果是目录
                List<String> list = Uri.parse(url).getPathSegments();
                String name = list.size() != 0 ? list.get(list.size() - 1) : System.currentTimeMillis() + "";
                file = new File(path, name);
            }

            randomAccessFile = new RandomAccessFile(file, "rwd");
            if (file.exists()) {
                seek(size());
            }
        }

        public void setCall(Call call) {
            this.call = call;
        }

        public void seek(long pos) {
            try {
                randomAccessFile.seek(pos);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void write(byte[] bytes, int off, int len) throws IOException {
            randomAccessFile.write(bytes, off, len);
        }

        public void close() {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public long size() {
            if (file != null) {
                return file.length();
            } else {
                return 0;
            }
        }


        public void cancel() {
            call.cancel();
        }

    }

}
