## 前言
>为何自己动手撸了一个呢，不使用流行的Retrofit+RxJava + OkHttp 方式。首先是因为对RxJava不了解，在平常项目中没有使用过，其次就为了一个网络框架引入RxJava RxAndroid 等，得不尝试，增加apk体积。以上观点仅代表个人观点，不喜勿喷。

## 介绍
[LSHttp](https://github.com/LiShang007/LSHttp)库，就仅仅是对OkHttp进行了一些封装，采用链式调用的方法，一行代码实现网络请求,支持绑定当前Activity、Fragment,在销毁时自动取消请求，让开发者使用起来更方便。

## 使用
    compile 'com.lishang.http:LSHttp:1.0.0'

## 支持以下操作
- get请求
- post表单、json
- 上传（单文件或多文件，支持进度监听）
- 下载（断点下载）
- 页面销毁时自动取消请求
- 自定义转换器，默认提供了StringCallBack,JsonCallBack

看到这里有些小伙伴就要说了，put、delete、head、options、trace、patch 这些请求为何不支持呢？这里分实话假话，实话就是水平有限我不会，工作了几年了没有用过。假话就是，在实际开发中Get/Post两种请求就可以满足99%的场景了。当然如果你的业务需求要用到的话，也可以继承BaseRequest,实现generateRequest方法就可以了。

## 各位大佬注意了，接下来就要开始介绍使用方法了
### 配置 在Application里面:
    LSHttp.init(this); //初始化,使用默认配置

    //配置自己的OkHttpClient
    OkHttpClient mClient = new OkHttpClient.Builder()
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(new LSHttpLoggingInterceptor().setLevel(LSHttpLoggingInterceptor.Level.BODY))
                //其它配置
                .build();

    LSHttp.init(this, mClient)
                .showLog(true) //是否显示日志
                .addHeader("key", "value") //全局添加header
                .addHeaders(map) //添加多个headers
                .baseUrl("https://wanandroid.com/"); //baseurl
#### baseUrl说明:
>如果配置了baseUrl,在request时可以直接传入路径，例如:user/login,如果某次请求的baseUrl不是当前配置的，直接传入完整的url就可以了,例如:https://www.33lc.com/article/UploadPic/2012-7/201272510182494484.jpg

### 如何做到Activity、Fragment销毁，请求自动取消
    LSHttp.xxx(url).
    execute(this); //this表示Activity或者Fragment

    LSHttp.xxx(url).
        execute(); //不传 Activity或者Fragment，在页面销毁时请求不会取消

### CallBack介绍，打交道最多的，最终请求数据都会回到CallBack,默认提供了两种CallBack:
#### StringCallBack 请求返回字符串
     LSHttp.xxx(url).
           .callback(new StringCallBack(){
                @Override
                public void onSuccess(String string) {
                    Log.e("onSuccess", string);
                

                }

                @Override
                public void onFail(LSHttpException e) {
                                  

                }
           });

#### JsonCallBack 返回实体类 内部使用了Gson
    LSHttp.xxx(url).
           .callback(new JsonCallBack<JsonData>(){
                @Override
                public void onSuccess(JsonData data) {
                    Log.e("onSuccess", data.toString());
                

                }

                @Override
                public void onFail(LSHttpException e) {
                                  

                }
           });
#### 如果StringCallBack和JsonCallBack不能满足你的使用，还可以自定义转换器,需要三步：
### 1.定义接口，继承ResponseCallBack
    public interface BitmapCallBack extends ResponseCallBack {
        void onSuccess(Bitmap bitmap);
    }
### 2.创建Class,实现接口IConvertResponse
    
    public class BitmapConvertResponse implements IConvertResponse {
        BitmapCallBack callBack;

        @Override
        public void convert(Response response) {
            InputStream inputStream = response.body().byteStream();//得到图片的流
            final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            //子线程中
            LSHttp.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (callBack != null) {
                        callBack.onSuccess(bitmap);
                    }
                }
            });
        }

        @Override
        public void setCallBack(ResponseCallBack callBack) {
            this.callBack = (BitmapCallBack) callBack;
        }
    }
### 3.请求
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


### 如何发起一个请求呢？
    LSHttp.xxx(url) //请求方法，get/post/json/download/multipart
          .xxx()//不同请求支持的方法
          .addHeader(key,value)//添加header
          .addHeaders(map)//添加多个header
          .tag(tag)//指定tag，用于取消
          .convert(IConvertResponse)//转换器,使用方法上面有介绍
          .callback(ResponseCallBack)//请求回调
          .execute(null/Activity/Fragment);//开始执行，传入Activity和Fragment，就会在页面销毁时自动取消请求
#### Get请求
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
#### Post请求
    //表单请求  application/x-www-form-urlencoded
    LSHttp.post("user/login")
                .addParams("username", "xxxx") //请求参数
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

    //json请求   json application/json; charset=utf-8
    LSHttp.json("https://www.wanandroid.com/user/login")
                .setJson(json) //json字符串
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
#### 下载
    String url = "https://6082fcfd683b09af1a65ea78561240f8.dd.cdntips.com/download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.apk";

                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/abcd/apk.apk";
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
                
    //取消下载
    LSHttp.cancel("down");

#### 上传
    //替换成自己的
    String url = "https://www.wanandroid.com/image/upload";


     String path = Environment.getExternalStorageDirectory().getAbsolutePath();

    LSHttp.multipart(url).addFile("image_1", new File(path + "/123.jpg"))
                        .addFile("image_2", new File(path + "/456.jpg"))
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


上传下载需要读写权限，推荐 [LSPermissions](https://github.com/LiShang007/LSPermissons "LSPermissions")

#### 如何扩展请求，例如put请求
##### 1.继承BaseRequest
    public class PutRequest extends BaseRequest <PutRequest>{
        @Override
        public Request generateRequest(Request.Builder builder) {
            
            return builder.put(requestbody).build();
        }
    }

##### 2.执行
    new PutRequest().url()
                    .xxx()
                    .xxx()
                    .execute();

>以上使用方法已经介绍完毕，接下来开始看下源码
## 源码
### BaseRequest
>定义了请求需要的公用参数，以及请求逻辑,绑定Acitivy、Fragment生命周期
    

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

        //添加请求header
        public T addHeader(String key, String value) {
            headers.put(key, value);
            return (T) this;
        }

    //添加多个header
    public T addHeaders(Map<String, String> map) {
        if (map != null) {
            headers.putAll(map);
        }
        return (T) this;
    }

    //url
    public T url(String url) {
        this.url = url;
        return (T) this;
    }

    //tag
    public T tag(String tag) {
        this.tag = tag;
        return (T) this;
    }

    //请求结果返回
    public T callback(ResponseCallBack callBack) {
        this.callBack = callBack;
        return (T) this;
    }

    //转换器
    public T convert(IConvertResponse convertResponse) {
        this.convertResponse = convertResponse;
        return (T) this;
    }

    //创建headers
    public Headers createHeaders() {
        Headers.Builder builder = new Headers.Builder();
        for (String key : headers.keySet()) {
            builder.add(key, headers.get(key));
        }
        return builder.build();
    }

    //构建基础request.builder
    public Request.Builder request() {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .tag(tag)
                .headers(createHeaders());
        return builder;
    }

    //用于定制request 差异
    public abstract Request generateRequest(Request.Builder builder);

    //异步执行网络请求
    public void execute() {
        execute(null);
    }

 
    //异步执行网络请求 obj 支持Activity、Fragment
    public void execute(final Object obj) {
        checkUrl();

        OkHttpClient client = mClient;


        Call call = client.newCall(generateRequest(request()));
        //与当前Acitivy、Fragment绑定关系
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
                //请求结束，移除绑定关系
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
                //请求结束，移除绑定关系
                removeLifecycle(obj, call);
            }
            });
        }

        //检查url是否合法
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

        //运行到主线程
        public void runOnMainThread(Runnable runnable) {
            LSHttp.getInstance().runOnMainThread(runnable);
        }

        //绑定Activity、Fragment
        public void bindLifecycle(Object obj, Call call) {
            if (lifecycleCallBacks != null && obj != null) {
                if (obj instanceof Activity || obj instanceof Fragment)
                    lifecycleCallBacks.put(obj.getClass().getName(), call);
            }
        }

        //移除绑定
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

### IConvertResponse
>请求结果转换接口，可以根据自己需求，实现该接口，进行定制化

    public interface IConvertResponse {
    /**
     * 转换 
     * @param response
     */
    void convert(Response response);

    /**
     * 回调
     * @param callBack
     */
    void setCallBack(ResponseCallBack callBack);
    }
### ResponseCallBack
>和上面的 IConvertResponse 配合使用,只定义了失败方法，成功方法根据转换自己定义对应的Success

    public interface ResponseCallBack {

        void onFail(LSHttpException e);

    }

### 请求如何绑定Activity、Fragment
>这里主要使用了 ActivityLifecycleCallbacks 和 FragmentManager.FragmentLifecycleCallbacks ，监听页面销毁时，将请求取消

#### LSHttpLifecycleCallBacks

    
    /**
    * 绑定生命周期基类、对外提供put remove
    */
    public class LSHttpLifecycleCallBacks {

        private ConcurrentMap<String, SparseArray<Call>> map = new ConcurrentHashMap<>();

        public void put(String key, Call call) {
            SparseArray<Call> calls = map.get(key);
            if (calls == null) {
                calls = new SparseArray<>();
            }
            calls.put(call.hashCode(), call);

            map.put(key, calls);

            LSLog.i("LifecycleCallBacks class:" + key + "call bind success");

        }

        public void remove(String key, Call call) {
            SparseArray<Call> calls = map.get(key);
            if (call != null) {
                calls.delete(call.hashCode());
                LSLog.i("LifecycleCallBacks class:" + key + "  call " + call.request().url().toString() + " remove success");
            }


        }

        public void destroyed(String key) {
            SparseArray<Call> calls = map.get(key);
            if (calls != null) {
                for (int i = 0; i < calls.size(); i++) {
                    Call call = calls.valueAt(i);
                    if (call != null && !call.isCanceled()) {
                        call.cancel();
                        LSLog.i("LifecycleCallBacks class:" + key + "  destroyed call " + call.request().url().toString() + " cancel success");
                    }
                }
                calls.clear();
                map.remove(key);

                LSLog.i("LifecycleCallBacks class:" + key + "  destroyed call remove success");

            }
        }

    }

#### LSHttpActivityLifecycleCallBacks

        
    /**
    * request 绑定生命周期 Activity销毁时自动取消
    */
    public class LSHttpActivityLifecycleCallBacks extends LSHttpLifecycleCallBacks implements Application.ActivityLifecycleCallbacks {


        private LSHttpFragmentLifecycleCallBacks fragmentLifecycleCallBacks;

        public LSHttpActivityLifecycleCallBacks() {
            this.fragmentLifecycleCallBacks = new LSHttpFragmentLifecycleCallBacks(this);
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {

            if (activity instanceof FragmentActivity) {

                FragmentManager fm = ((FragmentActivity) activity).getSupportFragmentManager();
                fm.registerFragmentLifecycleCallbacks(fragmentLifecycleCallBacks, true);

            }
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

            String key = activity.getClass().getName();
            destroyed(key);
        }


    }

#### LSHttpFragmentLifecycleCallBacks

    /**
    * request 绑定Fragment 生命周期
    */
    public class LSHttpFragmentLifecycleCallBacks extends FragmentManager.FragmentLifecycleCallbacks {

        private LSHttpLifecycleCallBacks callBacks;

        public LSHttpFragmentLifecycleCallBacks(LSHttpLifecycleCallBacks callBacks) {
            this.callBacks = callBacks;
        }


        @Override
        public void onFragmentDestroyed(FragmentManager fm, Fragment f) {
            super.onFragmentDestroyed(fm, f);

            String key = f.getClass().getName();
            callBacks.destroyed(key);


            fm.unregisterFragmentLifecycleCallbacks(this);

        }
    }

### 下载
>支持进度监听，通过查资料，一般有两种实现方式进行进度监听，一种是在请求成功后，在写入file时，监听的本地文件写入进度，还有一种就是通过网络拦截器包装ResponseBody，这种是监听网络下载进度，下载完毕后再写入file。但是我在写的时候发现了一个问题，那就是进度卡在100%,然后等file写入完毕后，才能拿到文件。我在方案二的基础上进行了改造，实现了边下边存，当下载完毕时文件也保存完毕了。

#### 1.定义接口
    
    interface ProgressListener {
        //下载进度监听
        void update(long bytesRead, long contentLength, boolean done);
        //写入文件
        void writeTo(byte[] bytes, int off, int len);
    }
#### 2.继承ResponseBody
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

#### 3.添加拦截器
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

#### 关键代码，在第二步
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


#### 如果没有下载完，不断的将读取的字节写入到文件
    if (!done) {
        byte[] bytes = sink.readByteArray();
        progressListener.writeTo(bytes, 0, bytes.length);
    }
#### 通过RandomAccessFile 不断的将字节写入到文件
    private class DownloadFile {
        RandomAccessFile randomAccessFile;
        File file;
        Call call;

        public DownloadFile(String path, String url) throws FileNotFoundException {
           ...
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
#### 监听
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


#### 使用到的库
[com.squareup.okhttp3:okhttp:3.12.3](https://square.github.io/okhttp/)

[com.google.code.gson:gson:2.8.2](https://github.com/google/gson)

#### https内容借鉴
[OKGO](https://github.com/jeasonlzy/okhttp-OkGo)

## 本次介绍到此为止，谢谢各位大佬的观看，有写的不对的地方，有写的不对的地方希望各位大佬指正。如果觉得写的可以的话，去GitHub给个星呗。