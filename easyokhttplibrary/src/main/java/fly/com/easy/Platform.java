package fly.com.easy;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

class Platform {
    private static final Platform PLATFORM = new OKAndroid();

    static Platform get() {
        return PLATFORM;
    }

    @Nullable
    Executor defaultCallbackExecutor() {
        return null;
    }

    static class OKAndroid extends Platform {

        @Override
        public Executor defaultCallbackExecutor() {
            return new MainThreadExecutor();
        }

        static class MainThreadExecutor implements Executor {
            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void execute(Runnable r) {
                handler.post(r);
            }
        }
    }

}
