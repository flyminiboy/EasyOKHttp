package fly.com.easy;

import java.io.IOException;
import java.util.concurrent.Executor;

import okhttp3.Request;
import okhttp3.Response;

import static fly.com.easy.EasyUtils.checkNotNull;

/**
 * 作者 ${郭鹏飞}.<br/>
 * 通过Call实现请求
 * 通过Executor进行UI线程回调
 */

public class ExecutorCallbackCall implements EasyCall {

    final Executor callbackExecutor;
    final EasyCall delegate;

    public ExecutorCallbackCall(Executor callbackExecutor, EasyCall delegate) {
        this.callbackExecutor = callbackExecutor;
        this.delegate = delegate;
    }

    @Override
    public Request request() {
        return delegate.request();
    }

    @Override
    public Response execute() throws IOException {
        return null;
    }

    @Override
    public void enqueue(final EasyCallback callback) {
        checkNotNull(callback, "callback == null");
        delegate.enqueue(new EasyCallback() {
            @Override
            public void onResponse(EasyCall call, final Response response) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (delegate.isCanceled()) {
                            // Emulate OkHttp's behavior of throwing/delivering an IOException on cancellation.
                            callback.onFailure(ExecutorCallbackCall.this, new IOException("Canceled"));
                        } else {
                            if (response.isSuccessful()) {
                                callback.onResponse(ExecutorCallbackCall.this, response);
                            } else {
                                callback.onFailure(ExecutorCallbackCall.this, new EasyResponseFailedException(response));
                            }
                        }
                    }
                });
            }

            @Override
            public void onFailure(EasyCall call, final Throwable t) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(ExecutorCallbackCall.this, t);
                    }
                });
            }
        });
    }


    @Override
    public void cancel() {
        delegate.cancel();
    }

    @Override
    public boolean isExecuted() {
        return delegate.isExecuted();
    }

    @Override
    public boolean isCanceled() {
        return delegate.isCanceled();
    }

    @Override
    public EasyCall clone() {
        return new ExecutorCallbackCall(callbackExecutor, delegate);
    }

}
