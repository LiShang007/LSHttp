package com.lishang.http.exception;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializer;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;

import java.io.IOException;
import java.io.NotSerializableException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;

/**
 * 预定的一些异常
 */
public class LSHttpException extends Exception {

    /**
     * 约定异常
     */
    public static class ERROR {
        /**
         * 未知错误
         */
        public static final int UNKNOWN = 1000;
        /**
         * 连接超时
         */
        public static final int TIMEOUT_ERROR = 1001;
        /**
         * 空指针错误
         */
        public static final int NULL_POINTER_EXCEPTION = 1002;

        /**
         * 证书出错
         */
        public static final int SSL_ERROR = 1003;

        /**
         * 类转换错误
         */
        public static final int CAST_ERROR = 1004;

        /**
         * 解析错误
         */
        public static final int PARSE_ERROR = 1005;

        /**
         * 非法数据异常
         */
        public static final int ILLEGAL_STATE_ERROR = 1006;

        /**
         * 服务端异常 http code ！= 200
         */
        public static final int HTTP_ERROR = 1007;


    }


    public final int code;
    public String message;

    public LSHttpException(Throwable throwable, int code) {
        super(throwable);
        this.code = code;
        this.message = throwable.getMessage();
    }

    public LSHttpException(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public static LSHttpException handleException(Throwable e) {
        LSHttpException ex;
        if (e instanceof SocketTimeoutException) {
            ex = new LSHttpException(e, ERROR.TIMEOUT_ERROR);
            ex.message = "网络不可用，请稍后再试";
            return ex;
        } else if (e instanceof ConnectException) {
            ex = new LSHttpException(e, ERROR.TIMEOUT_ERROR);
            ex.message = "网络不可用，请稍后再试";
            return ex;
        } else if (e instanceof ConnectTimeoutException) {
            ex = new LSHttpException(e, ERROR.TIMEOUT_ERROR);
            ex.message = "网络不可用，请稍后再试";
            return ex;
        } else if (e instanceof UnknownHostException) {
            ex = new LSHttpException(e, ERROR.TIMEOUT_ERROR);
            ex.message = "网络不可用，请稍后再试";
            return ex;
        } else if (e instanceof NullPointerException) {
            ex = new LSHttpException(e, ERROR.NULL_POINTER_EXCEPTION);
            ex.message = "空指针异常";
            return ex;
        } else if (e instanceof javax.net.ssl.SSLHandshakeException) {
            ex = new LSHttpException(e, ERROR.SSL_ERROR);
            ex.message = "证书验证失败";
            return ex;
        } else if (e instanceof ClassCastException) {
            ex = new LSHttpException(e, ERROR.CAST_ERROR);
            ex.message = "类型转换错误";
            return ex;
        } else if (e instanceof JsonParseException
                || e instanceof JSONException
                || e instanceof JsonSerializer
                || e instanceof NotSerializableException
                || e instanceof ParseException) {
            ex = new LSHttpException(e, ERROR.PARSE_ERROR);
            ex.message = "解析错误";
            return ex;
        } else if (e instanceof IllegalStateException) {
            ex = new LSHttpException(e, ERROR.ILLEGAL_STATE_ERROR);
            ex.message = e.getMessage();
            return ex;
        } else {
            ex = new LSHttpException(e, ERROR.UNKNOWN);
            ex.message = "未知错误";
            return ex;
        }
    }

}
