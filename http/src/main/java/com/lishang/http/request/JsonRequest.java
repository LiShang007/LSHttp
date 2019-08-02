package com.lishang.http.request;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * post 请求 提交JSON字符串
 */
public class JsonRequest extends BaseRequest<JsonRequest> {
    private String param_json; // post json application/json; charset=utf-8

    public JsonRequest setJson(String json) {
        this.param_json = json;
        return this;
    }

    private RequestBody createJsonBody() {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, param_json);
        return body;
    }

    @Override
    public Request generateRequest(Request.Builder builder) {
        return builder.post(createJsonBody()).build();
    }
}
