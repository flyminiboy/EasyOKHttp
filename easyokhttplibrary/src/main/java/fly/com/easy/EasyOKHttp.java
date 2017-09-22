package fly.com.easy;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static fly.com.easy.EasyUtils.checkMapNotEmpty;
import static fly.com.easy.EasyUtils.checkNotNull;

/**
 * 作者 ${郭鹏飞}.<br/>
 * 参考
 * Retrofit
 * https://github.com/square/retrofit
 * 实现多个baseUrl 参考
 * http://www.jianshu.com/p/2919bdb8d09a
 */

public class EasyOKHttp {

    private static final String DOMAIN_NAME = "Domain-Name";
    public static final String DOMAIN_NAME_HEADER = DOMAIN_NAME + ": ";

    final HttpUrl baseUrl;
    // 各种超时 单位毫秒
    final long readTimeout;
    final long connectTimeout;
    final long writeTimeout;
    final
    @Nullable
    Executor callbackExecutor;

    OkHttpClient httpClient;
    private Map<String, String> mBaseUrls;

    // 方法缓冲
    private final Map<Object, ServiceMethod> serviceRequestCache = new ConcurrentHashMap<>();

    private OkHttpClient.Builder builder;

    EasyOKHttp(HttpUrl baseUrl, Map<String, String> baseUrls, long readTimeout, long connectTimeout, long writeTimeout, Executor callbackExecutor) {
        this.baseUrl = baseUrl;
        mBaseUrls = baseUrls;
        this.readTimeout = readTimeout;
        this.connectTimeout = connectTimeout;
        this.writeTimeout = writeTimeout;
        this.callbackExecutor = callbackExecutor;
        initInterceptor();
    }

    EasyOKHttp(HttpUrl baseUrl, long readTimeout, long connectTimeout, long writeTimeout, Executor callbackExecutor) {
        this.baseUrl = baseUrl;
        this.readTimeout = readTimeout;
        this.connectTimeout = connectTimeout;
        this.writeTimeout = writeTimeout;
        this.callbackExecutor = callbackExecutor;
        initInterceptor();
    }

    private void initInterceptor() {
        builder = new OkHttpClient.Builder();
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                return chain.proceed(resetRequest(chain.request()));
            }
        });
    }

    private Request resetRequest(Request request) {
        List<String> headers = request.headers(DOMAIN_NAME);
        if (headers == null || headers.isEmpty()) {
            return request;
        }
        if (headers.size() > 1) {
            throw new IllegalArgumentException("Only one Domain-Name in the headers");
        }
        checkNotNull(mBaseUrls, "mBaseUrls == null");
        checkMapNotEmpty(mBaseUrls, "mBaseUrls is empty");
        String header = request.header(DOMAIN_NAME);
        String url = mBaseUrls.get(header);
        if (TextUtils.isEmpty(url)) {
            throw new NullPointerException("Did not find a matching address");
        }
        HttpUrl newHttpUrl = HttpUrl.parse(url);
        HttpUrl httpUrl = request.url();
        Request.Builder builder = request.newBuilder();
        builder.url(httpUrl.newBuilder().
                host(newHttpUrl.host()).
                scheme(newHttpUrl.scheme()).
                port(newHttpUrl.port()).build());
        return builder.build();
    }

    public OkHttpClient.Builder getOkHttpClientBuilder() {
        return builder;
    }

    public EasyOKHttp initHttpClient() {
        httpClient = buildHttpClient(builder.readTimeout(readTimeout, TimeUnit.MILLISECONDS).
                connectTimeout(connectTimeout, TimeUnit.MILLISECONDS).
                writeTimeout(writeTimeout, TimeUnit.MILLISECONDS));
        return this;
    }

    public void initHttpClient(OkHttpClient httpClient) {
        this.httpClient = buildHttpClient(httpClient.newBuilder());
    }

    private OkHttpClient buildHttpClient(OkHttpClient.Builder builder) {
        // 关闭重试
        return builder.retryOnConnectionFailure(false).build();
    }

    /**
     * @param requestSignature 请求签名 注意这个需要保证唯一性
     * @param easyOptions
     * @return
     */
    public EasyCall createCall(@NonNull Object requestSignature, @NonNull EasyOptions easyOptions) {
        checkNotNull(requestSignature, "methodSignature == null");
        checkNotNull(easyOptions, "easyOptions == null");
        if (httpClient == null) {
            initHttpClient();
        }
        ServiceMethod serviceMethod = loadServiceMethod(requestSignature, easyOptions);
        // TODO: 2017/9/21 优化配置不同的call实现不同的逻辑 上传和下载
        EasyCall call = new EasyCallImpl(serviceMethod);
        return serviceMethod.callAdapter.adapt(call);
    }

    private ServiceMethod loadServiceMethod(Object methodSignature, EasyOptions easyOptions) {
        ServiceMethod result = serviceRequestCache.get(methodSignature);
        if (result != null) return result;

        synchronized (serviceRequestCache) {
            result = serviceRequestCache.get(methodSignature);
            if (result == null) {
                result = new ServiceMethod.Builder(this, easyOptions).build();
                serviceRequestCache.put(methodSignature, result);
            }
        }
        return result;
    }

    public static final class Builder {
        private final Platform platform;
        private HttpUrl baseUrl;
        private Map<String, String> otherBaseUrls = null;
        private long readTimeout = 10 * 1000L;
        private long connectTimeout = 10 * 1000L;
        private long writeTimeout = 10 * 1000L;
        private
        @Nullable
        Executor callbackExecutor;

        Builder(Platform platform) {
            this.platform = platform;
        }

        public Builder() {
            this(Platform.get());
        }

        public Builder baseUrls(Map<String, String> otherBaseUrls) {
            checkNotNull(otherBaseUrls, "otherBaseUrls == null");
            checkMapNotEmpty(otherBaseUrls, "otherBaseUrls is empty");
            this.otherBaseUrls = otherBaseUrls;
            return this;
        }

        /**
         * Set the API base URL.
         *
         * @see #baseUrl(HttpUrl)
         */
        public Builder baseUrl(String baseUrl) {
            checkNotNull(baseUrl, "baseUrl == null");
            HttpUrl httpUrl = HttpUrl.parse(baseUrl);
            if (httpUrl == null) {
                throw new IllegalArgumentException("Illegal URL: " + baseUrl);
            }
            return baseUrl(httpUrl);
        }

        private Builder baseUrl(HttpUrl baseUrl) {
            checkNotNull(baseUrl, "baseUrl == null");
            List<String> pathSegments = baseUrl.pathSegments();
            if (!"".equals(pathSegments.get(pathSegments.size() - 1))) {
                throw new IllegalArgumentException("baseUrl must end in /: " + baseUrl);
            }
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder readTimeout(long readTimeout) {
            if (readTimeout < 0) {
                throw new IllegalArgumentException("readTimeout must be greater than 0 ");
            }
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder connectTimeout(long connectTimeout) {
            if (connectTimeout < 0) {
                throw new IllegalArgumentException("connectTimeout must be greater than 0 ");
            }
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder writeTimeout(long writeTimeout) {
            if (readTimeout < 0) {
                throw new IllegalArgumentException("writeTimeout must be greater than 0 ");
            }
            this.writeTimeout = writeTimeout;
            return this;
        }

        public EasyOKHttp build() {

            if (baseUrl == null) {
                throw new IllegalStateException("Base URL required.");
            }

            callbackExecutor = platform.defaultCallbackExecutor();

            if (otherBaseUrls == null) {
                return new EasyOKHttp(baseUrl, readTimeout, connectTimeout, writeTimeout, callbackExecutor);
            } else {
                return new EasyOKHttp(baseUrl, otherBaseUrls, readTimeout, connectTimeout, writeTimeout, callbackExecutor);
            }
        }

    }

}
