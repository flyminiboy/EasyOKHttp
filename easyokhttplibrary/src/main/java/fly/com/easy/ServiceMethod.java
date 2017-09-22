package fly.com.easy;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static fly.com.easy.EasyOptions.JSON;
import static fly.com.easy.EasyUtils.checkListNotEmpty;
import static fly.com.easy.EasyUtils.checkStringArgument;


/**
 * 作者 ${郭鹏飞}.<br/>
 */
 class ServiceMethod {

    OkHttpClient httpClient;
    Request.Builder requestBuilder;
    EasyCallAdapter<ExecutorCallbackCall> callAdapter;

    ServiceMethod(Builder builder) {
        httpClient = builder.easyOKHttp.httpClient;
        callAdapter = builder.callAdapter;
        requestBuilder = builder.builder;
    }

    Request toRequest() {
        return requestBuilder.build();
    }

    static final class Builder {

        private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

        final EasyOKHttp easyOKHttp;
        final EasyOptions easyOptions;
        Request.Builder builder;
        EasyCallAdapter<ExecutorCallbackCall> callAdapter;

        Builder(EasyOKHttp easyOKHttp, EasyOptions easyOptions) {
            this.easyOKHttp = easyOKHttp;
            this.easyOptions = easyOptions;
        }

        public ServiceMethod build() {
            callAdapter = createCallAdapter();

            builder = buildRequestBuilder(getUrl());

            addHeaders(builder);

            return new ServiceMethod(this);
        }

        private void addHeaders(Request.Builder builder) {
            if (easyOptions.sigHeaders != null && !easyOptions.sigHeaders.isEmpty()) {
                for (Map.Entry<String, String> headers :
                        easyOptions.sigHeaders.entrySet()) {
                    checkStringArgument(headers.getKey(), "header key == null");
                    checkStringArgument(headers.getValue(), "header value == null");
                    builder.header(headers.getKey(), headers.getValue());
                }
            }
            if (easyOptions.mulHeaders != null && !easyOptions.mulHeaders.isEmpty()) {
                for (Map.Entry<String, List<String>> headers :
                        easyOptions.mulHeaders.entrySet()) {
                    String key = headers.getKey();
                    checkStringArgument(headers.getKey(), "addHeader key == null");
                    List<String> values = headers.getValue();
                    checkListNotEmpty(values, "addHeader values == null or empty");
                    for (String value :
                            values) {
                        checkStringArgument(value, "addHeader value == null");
                        builder.addHeader(key, value);
                    }
                }
            }
        }

        private String getUrl() {
            String url;
            if (easyOptions.isFullPath()) {
                url = easyOptions.url;
            } else {
                url = easyOKHttp.baseUrl + easyOptions.path;
            }
            return url;
        }

        private Request.Builder buildRequestBuilder(String url) {
            Request.Builder builder;
            if (easyOptions.method == EasyOptions.GET) {
                if (easyOptions.params != null) {
                    url = url + "?" + spliceParamsForGET(easyOptions.params);
                }
                builder = new Request.Builder().get().url(url);
            } else {
                RequestBody body;
                if (easyOptions.params != null) {
                    body = getBodyByParams(easyOptions.params);
                } else {
                    if (easyOptions.postType == JSON) {
                        body = RequestBody.create(MEDIA_TYPE, "");
                    } else {
                        FormBody.Builder formBilder = new FormBody.Builder();
                        body = formBilder.build();
                    }
                }
                builder = new Request.Builder().post(body).url(url);
            }
            return builder;
        }

        private RequestBody getBodyByParams(Map<String, Object> params) {
            RequestBody body;
            if (easyOptions.postType == JSON) {
                JSONObject json = new JSONObject();
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    try {
                        json.put(param.getKey(), param.getValue());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                body = RequestBody.create(MEDIA_TYPE, json.toString());
            } else {
                FormBody.Builder formBilder = new FormBody.Builder();
                for (Map.Entry<String, Object> param : easyOptions.params.entrySet()) {
                    if (param.getValue() instanceof String) {
                        formBilder.add(param.getKey(), (String) param.getValue());
                    } else {
                        throw new IllegalArgumentException("The form submission value must be string");
                    }
                }
                body = formBilder.build();
            }
            return body;
        }

        private String spliceParamsForGET(Map<String, Object> params) {
            StringBuilder sb = new StringBuilder();
            int index = 0;
            for (Map.Entry<String, Object> entry:
                 params.entrySet()) {
                if (index != 0) {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                index++;
            }
            return sb.toString();
        }

        private EasyCallAdapter<ExecutorCallbackCall> createCallAdapter() {
            return new EasyCallAdapter<ExecutorCallbackCall>() {
                @Override
                public ExecutorCallbackCall adapt(EasyCall call) {
                    return new ExecutorCallbackCall(easyOKHttp.callbackExecutor, call);
                }
            };
        }
    }

}
