package com.lishang.http.request;

import okhttp3.Request;

/**
 * Get 请求
 */
public class GetRequest extends BaseRequest<GetRequest> {
    @Override
    public Request generateRequest(Request.Builder builder) {
        return builder.build();
    }
}
