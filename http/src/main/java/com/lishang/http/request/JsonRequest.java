package com.lishang.http.request;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class JsonRequest extends BaseRequest<JsonRequest> {
    private String param_json; // post json application/json; charset=utf-8

    public JsonRequest setJson(String json) {
        this.param_json = json;
        return this;
    }

    public RequestBody createJsonBody() {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, param_json);
        return body;
    }
}
