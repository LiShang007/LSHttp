package com.lishang.http.request;

import android.util.Log;


import com.lishang.http.callback.UploadCallBack;
import com.lishang.http.utils.LSLog;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;

/**
 * 文件上传 （支持多文件 进度监听 ）
 */
public class MultipartRequest extends BaseRequest<MultipartRequest> {

    private Map<String, String> params = new HashMap<>(); //
    private Map<String, FileWrapper> files = new HashMap<>();
    private UploadCallBack progressCallBack;

    public MultipartRequest addParams(String key, String value) {
        params.put(key, value);
        return this;
    }

    public MultipartRequest addFile(String key, File file) {
        files.put(key, new FileWrapper(file, file.getName()));
        return this;
    }

    public MultipartRequest progress(UploadCallBack progressCallBack) {
        this.progressCallBack = progressCallBack;
        return this;
    }

    private RequestBody createMultipartBody() {


        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        if (!params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }

        if (!files.isEmpty()) {
            for (Map.Entry<String, FileWrapper> entry : files.entrySet()) {
                FileWrapper wrapper = entry.getValue();
                builder.addFormDataPart(entry.getKey(), wrapper.fileName, RequestBody.create(wrapper.contentType, wrapper.file));
            }
        }


        return builder.build();
    }

    @Override
    public Request generateRequest(Request.Builder builder) {
        return builder.post(createMultipartBody()).build();
    }

    @Override
    public void execute(Object obj) {
        mClient = mClient.newBuilder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        RequestBody body = original.body();
                        if (body instanceof MultipartBody) {
                            body = new ProgressRequestBody((MultipartBody) body, new UploadProgressListener() {

                                @Override
                                public void onProgress(long total, long current) {
                                    LSLog.i("onProgress upload:" + current + " / " + total);
                                    if (progressCallBack != null) {
                                        final int progress = (int) (100 * current / total);
                                        runOnMainThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressCallBack.onLoading(progress);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                        Request newRequest = original.newBuilder().method(original.method(), body).build();
                        return chain.proceed(newRequest);
                    }
                })
                .build();
        super.execute(obj);
    }

    /**
     * 文件类型的包装类
     */
    public static class FileWrapper implements Serializable {
        private static final long serialVersionUID = -2356139899636767776L;

        public File file;
        public String fileName;
        public transient MediaType contentType;
        public long fileSize;

        public FileWrapper(File file, String fileName) {
            this.file = file;
            this.fileName = fileName;
            this.contentType = guessMimeType(fileName);
            this.fileSize = file.length();
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            out.writeObject(contentType.toString());
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            contentType = MediaType.parse((String) in.readObject());
        }

        @Override
        public String toString() {
            return "FileWrapper{" + //
                    "file=" + file + //
                    ", fileName=" + fileName + //
                    ", contentType=" + contentType + //
                    ", fileSize=" + fileSize +//
                    "}";
        }


        /**
         * 根据文件名获取MIME类型
         */
        private static MediaType guessMimeType(String fileName) {
            FileNameMap fileNameMap = URLConnection.getFileNameMap();
            fileName = fileName.replace("#", "");   //解决文件名中含有#号异常的问题
            String contentType = fileNameMap.getContentTypeFor(fileName);
            if (contentType == null) {
                return MediaType.parse("application/octet-stream");
            }
            return MediaType.parse(contentType);
        }

    }


    private class ProgressRequestBody extends RequestBody {
        private MultipartBody mMultipartBody;
        private UploadProgressListener mProgressListener;

        public ProgressRequestBody(MultipartBody mMultipartBody, UploadProgressListener mProgressListener) {
            this.mMultipartBody = mMultipartBody;
            this.mProgressListener = mProgressListener;
        }

        @Override
        public MediaType contentType() {
            return mMultipartBody.contentType();
        }

        @Override
        public long contentLength() throws IOException {
            return mMultipartBody.contentLength();
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {

            //这里需要另一个代理类来获取写入的长度
            ForwardingSink forwardingSink = new ForwardingSink(sink) {

                final long totalLength = contentLength();
                long mCurrentLength;

                @Override
                public void write(Buffer source, long byteCount) throws IOException {

                    //这里可以获取到写入的长度
                    mCurrentLength += byteCount;
                    //回调进度
                    if (mProgressListener != null) {
                        mProgressListener.onProgress(totalLength, mCurrentLength);
                    }


                    super.write(source, byteCount);
                }
            };

            //转一下
            BufferedSink bufferedSink = Okio.buffer(forwardingSink);
            //写数据
            mMultipartBody.writeTo(bufferedSink);
            //刷新一下数据
            bufferedSink.flush();

        }
    }

    interface UploadProgressListener {
        void onProgress(long total, long current);
    }

}
