#### 网络请求框架 
#### 目前对以下请求进行了封装
- get请求
- post表单、json
- 上传（单文件或多文件，支持进度监听）
- 下载（断点下载）
- 页面销毁时自动取消请求
- 自定义转换器，默认提供了StringCallBack,JsonCallBack

#### 初始化 在Application

    LSHttp.init(this); //初始化

#### 配置自己的OkHttpClient，不配置的话一切使用默认的配置

    //配置全局的OkHttp
    OkHttpClient mClient = new OkHttpClient.Builder()
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(new LSHttpLoggingInterceptor().setLevel(LSHttpLoggingInterceptor.Level.BODY))
                //其它配置
                .build();

    LSHttp.getInstance().setClient(mClient);

#### Activity、Fragment消耗自动取消请求

    LSHttp.xxx(url).
        execute(this); //this表示Activity或者Fragment
    
     LSHttp.xxx(url).
        execute(); //不传 Activity或者Fragment，在页面销毁时请求不会取消

#### CallBack介绍
##### StringCallBack 请求返回字符串
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

##### JsonCallBack 返回实体类 内部使用了Gson
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
#### 1.定义接口，继承ResponseCallBack
    public interface BitmapCallBack extends ResponseCallBack {
        void onSuccess(Bitmap bitmap);
    }
#### 2.创建Class,实现接口IConvertResponse
    
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
#### 3.请求
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





#### Get请求
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

#### Post请求
    //表单请求  application/x-www-form-urlencoded
    LSHttp.post("https://www.wanandroid.com/user/login")
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

    //json请求   json application/json; charset=utf-8
    LSHttp.json("https://www.wanandroid.com/user/login")
                .setJson(json)
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

上传下载需要读写权限，推荐[LSPermissions]("https://github.com/LiShang007/LSPermissons")


