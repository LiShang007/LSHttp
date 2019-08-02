package com.lishang.okhttp.request;

import com.lishang.http.request.BaseRequest;

import okhttp3.FormBody;
import okhttp3.Request;

public class PutRequest extends BaseRequest<PutRequest> {
    @Override
    public Request generateRequest(Request.Builder builder) {
        return builder.put(new FormBody.Builder().build()).build();
    }


}
