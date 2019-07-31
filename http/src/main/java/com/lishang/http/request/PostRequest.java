package com.lishang.http.request;


import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;

public class PostRequest extends BaseRequest<PostRequest> {

    private Map<String, String> params = new HashMap<>(); //form 表单提交参数 application/x-www-form-urlencoded

    public PostRequest addParams(String key, String value) {
        params.put(key, value);
        return this;
    }

    public FormBody createFormBody() {
        FormBody.Builder builder = new FormBody.Builder();
        for (String key : params.keySet()) {
            builder.add(key, params.get(key));
        }
        return builder.build();
    }

}
