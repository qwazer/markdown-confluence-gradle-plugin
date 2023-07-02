package com.github.qwazer.markdown.confluence.core;

import okhttp3.Interceptor;
import okhttp3.Request;

public class OkHttpUtils {

    public static Interceptor getAuthorizationInterceptor(String authorizationHeader) {
        return chain -> {
            final Request request = chain.request()
                .newBuilder()
                .header(HttpHeader.ACCEPT, "application/json")
                .header(HttpHeader.AUTHORIZATION, authorizationHeader)
                .build();
            return chain.proceed(request);
        };
    }

}
