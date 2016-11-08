package com.facebook.stetho.okhttp3;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Dummy interceptor for release
 *
 * @author bishiboosh
 */
public final class StethoInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(chain.request());
    }
}
