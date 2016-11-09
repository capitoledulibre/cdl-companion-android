package org.toulibre.capitoledulibre.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * @author bishiboosh
 */

public class ProgressInterceptor implements Interceptor {

    private LocalBroadcastManager mBroadcastManager;
    private String mProgressAction;
    private String mProgressExtra;

    public ProgressInterceptor(Context context, String progressAction, String progressExtra) {
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
        mProgressAction = progressAction;
        mProgressExtra = progressExtra;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        return originalResponse.newBuilder()
                .body(new ProgressResponseBody(originalResponse.body()))
                .build();
    }

    private class ProgressResponseBody extends ResponseBody {

        private final ResponseBody responseBody;
        private BufferedSource bufferedSource;

        ProgressResponseBody(ResponseBody responseBody) {
            this.responseBody = responseBody;
        }

        @Override public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override public long contentLength() {
            return responseBody.contentLength();
        }

        @Override public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    long percent = bytesRead == -1 ? 100 : (totalBytesRead * 100) / contentLength();
                    mBroadcastManager.sendBroadcast(new Intent(mProgressAction).putExtra(mProgressExtra, (int) percent));
                    return bytesRead;
                }
            };
        }
    }
}
