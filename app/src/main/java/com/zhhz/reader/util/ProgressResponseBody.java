package com.zhhz.reader.util;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class ProgressResponseBody extends ResponseBody {

    private static final String TAG = "ProgressResponseBody";

    private BufferedSource bufferedSource;

    private final ResponseBody responseBody;

    private ProgressListener listener;

    private long le;
    private final String url;
    public ProgressResponseBody(String url, ResponseBody responseBody,@NonNull long l) {
        this.responseBody = responseBody;
        le = l;
        if (l < 0 ) {
            le=responseBody.contentLength();
        }
        listener = ProgressInterceptor.LISTENER_MAP.get(url);
        this.url=url;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return le;
    }

    @NonNull
    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(new ProgressSource(responseBody.source()));
        }
        return bufferedSource;
    }

    private class ProgressSource extends ForwardingSource {

        long totalBytesRead = 0;

        int currentProgress;

        ProgressSource(Source source) {
            super(source);
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            long bytesRead = super.read(sink, byteCount);
            long fullLength = contentLength();
            if (bytesRead == -1) {
                totalBytesRead = fullLength;
            } else {
                totalBytesRead += bytesRead;
            }
            int progress = (int) (100f * totalBytesRead / contentLength());
            if (progress<0)
                progress=0;
            //Log.d(TAG, "download progress is " + totalBytesRead + "-->" + contentLength());
            if (listener != null && progress != currentProgress) {
                listener.onProgress(url,progress);
            }
            if (listener != null && totalBytesRead == fullLength) {
                listener = null;
            }
            currentProgress = progress;
            return bytesRead;
        }
    }

}