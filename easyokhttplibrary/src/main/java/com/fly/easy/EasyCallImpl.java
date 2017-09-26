package com.fly.easy;

import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 作者 ${郭鹏飞}.<br/>
 * 进行网络请求
 */

final class EasyCallImpl implements EasyCall {

    private volatile boolean canceled;

    private Call mCall;
    private boolean executed;
    private @Nullable
    Throwable creationFailure; // Either a RuntimeException or IOException.

    private ServiceMethod mServiceMethod;

    EasyCallImpl(ServiceMethod serviceMethod) {
        mServiceMethod = serviceMethod;
    }

    private Call createRawCall() throws IOException {
        Request request = mServiceMethod.toRequest();
        Call call = mServiceMethod.httpClient.newCall(request);
        if (call == null) {
            throw new NullPointerException("httpClient returned null by newCall");
        }
        return call;
    }

    private EasyResponse parseResponse(okhttp3.Response rawResponse) throws IOException {

        ResponseBody rawBody = rawResponse.body();

        return new EasyResponse(rawResponse, rawBody);

    }

    @Override
    public synchronized Request request() {
        Call call = mCall;
        if (call != null) {
            return call.request();
        }
        return null;
    }


    @Override
    public synchronized Response execute() throws IOException {
        Call call;

        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed.");
            executed = true;

            if (creationFailure != null) {
                if (creationFailure instanceof IOException) {
                    throw (IOException) creationFailure;
                } else {
                    throw (RuntimeException) creationFailure;
                }
            }

            call = mCall;
            if (call == null) {
                try {
                    call = mCall = createRawCall();
                } catch (IOException | RuntimeException e) {
                    creationFailure = e;
                    throw e;
                }
            }
        }

        if (canceled) {
            call.cancel();
        }

        return call.execute();
    }

    @Override
    public void enqueue(final EasyCallback callback) {
        EasyUtils.checkNotNull(callback, "callback == null");

        Call call;
        Throwable failure;

        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed.");
            executed = true;

            call = mCall;
            failure = creationFailure;
            if (call == null && failure == null) {
                try {
                    call = mCall = createRawCall();
                } catch (Throwable t) {
                    failure = creationFailure = t;
                }
            }
        }

        if (failure != null) {
            callback.onFailure(this, failure);
            return;
        }

        if (canceled) {
            call.cancel();
        }

        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {
                callSuccess(parseResponse(response));
            }

            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    callback.onFailure(EasyCallImpl.this, e);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            private void callFailure(Throwable e) {
                try {
                    callback.onFailure(EasyCallImpl.this, e);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            private void callSuccess(EasyResponse response) {
                try {
                    callback.onResponse(EasyCallImpl.this, response);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }

    @Override
    public void cancel() {
        canceled = true;

        Call call;
        synchronized (this) {
            call = mCall;
        }
        if (call != null) {
            call.cancel();
        }
    }

    @Override
    public synchronized boolean isExecuted() {
        return executed;
    }

    @Override
    public boolean isCanceled() {
        if (canceled) {
            return true;
        }
        synchronized (this) {
            return mCall != null && mCall.isCanceled();
        }
    }

    @Override
    public EasyCall clone() {
        return new EasyCallImpl(mServiceMethod);
    }

}
