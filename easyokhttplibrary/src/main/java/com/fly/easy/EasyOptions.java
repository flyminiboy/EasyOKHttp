package com.fly.easy;

import android.support.annotation.IntDef;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者 ${郭鹏飞}.<br/>
 * 主要用于封装{@link okhttp3.Request}
 */
public class EasyOptions {

    public static final int GET = 0x00000001; // GET 请求
    public static final int POST = 0x00000002; // POST 请求

    @IntDef({POST, GET})
    @Retention(RetentionPolicy.SOURCE) //注解保留范围为源代码
    public @interface Method {
    }

    public static final int JSON = 0; // post提交json
    public static final int FROM = 1; // post提交form表单

    @IntDef({JSON, FROM})
    @Retention(RetentionPolicy.SOURCE) //注解保留范围为源代码
    public @interface PostType {
    }

    String url; // 访问全路径
    String path; // 地址 当url和path同时存在url生效
    final int method; // 请求方式 支持 GET POST 默认GET
    int postType; // post提交json
    Map<String, Object> params; // 参数t
    // 配置请求头 不会覆盖之前添加的
    Map<String, List<String>> mulHeaders;
    // 键，值一一对应 如果已经存在对应值，则进行覆盖
    Map<String, String> sigHeaders;

    public EasyOptions(int method) {
        this.method = method;
    }

    /**
     * 是否配置的是全地址
     * 全地址不在使用baseUrl进行配置
     * 不是全地址根据配置的baseUrl进行配置
     * {@link EasyOKHttp.Builder}
     *
     * @return
     */
    public boolean isFullPath() {
        return TextUtils.isEmpty(path);
    }

    public static class OKHttpOptionsBuilder {

        private String url = null;
        private String path = null;
        private int method = GET;
        private Map<String, Object> params = null; // 参数
        private Map<String, List<String>> mulHeaders;
        private Map<String, String> sigHeaders;

        public OKHttpOptionsBuilder() {
        }

        /**
         * 设置访问地址
         *
         * @param url 地址
         */
        public OKHttpOptionsBuilder url(String url) {
            EasyUtils.checkNotNull(url, "url == null");
            this.url = url;
            return this;
        }

        /**
         * 设置访问地址
         *
         * @param path 地址
         */
        public OKHttpOptionsBuilder path(String path) {
            EasyUtils.checkNotNull(path, "path == null");
            this.path = path;
            return this;
        }

        /**
         * 设置请求方式
         *
         * @param method {@link Method}
         */
        public OKHttpOptionsBuilder method(@Method int method) {
            if (method != GET && method != POST) {
                throw new IllegalArgumentException("see Method");
            }
            this.method = method;
            if (method == GET) {
                return this;
            } else {
                return new OKHttpOptionsPostBuilder(this);
            }
        }

        /**
         * 指定post提交数据的形式
         *
         * @param postType {@link PostType}
         * @see #method(int)
         */
        public OKHttpOptionsBuilder postType(@PostType int postType) {
            if (this instanceof OKHttpOptionsPostBuilder) {
                ((OKHttpOptionsPostBuilder)this).postType(postType);
            } else {
                throw new IllegalArgumentException("this is not OKHttpOptionsPostBuilder, see method(int)");
            }
            return this;
        }

        /**
         * 添加参数
         *
         * @param key   键
         * @param value 值
         */
        public OKHttpOptionsBuilder param(String key, Object value) {
            EasyUtils.checkNotNull(key, "key == null");
            EasyUtils.checkNotNull(value, "value == null");
            if (params == null) {
                params = new HashMap<>();
            }
            params.put(key, value);
            return this;
        }

        /**
         * 制定一个参数集合
         *
         * @param params 键值对的参数集合
         */
        public OKHttpOptionsBuilder params(Map<String, Object> params) {
            EasyUtils.checkNotNull(params, "params == null");
            EasyUtils.checkMapNotEmpty(params, "params is empty");
            if (this.params == null) {
                this.params = params;
            } else {
                this.params.putAll(params);
            }
            return this;
        }

        /**
         * 添加header
         *
         * @param header
         */
        public OKHttpOptionsBuilder header(Map<String, String> header) {
            EasyUtils.checkNotNull(header, "header == null");
            EasyUtils.checkMapNotEmpty(header, "header is empty");
            this.sigHeaders = header;
            return this;
        }

        /**
         * 添加header
         *
         * @param headers
         */
        public OKHttpOptionsBuilder headers(Map<String, List<String>> headers) {
            EasyUtils.checkNotNull(headers, "headers == null");
            EasyUtils.checkMapNotEmpty(headers, "headers is empty");
            this.mulHeaders = headers;
            return this;
        }

        /**
         * 构建OKHttpOptions对象
         *
         * @return {@link EasyOptions}
         */
        public EasyOptions build() {

            if (TextUtils.isEmpty(url) && TextUtils.isEmpty(path)) {
                throw new IllegalStateException("url or path must be one.");
            }

            EasyOptions easyOptions = new EasyOptions(method);
            if (TextUtils.isEmpty(this.url)) {
                easyOptions.path = this.path;
            } else {
                easyOptions.url = this.url;
            }
            if (this instanceof OKHttpOptionsPostBuilder) {
                OKHttpOptionsPostBuilder builder = (OKHttpOptionsPostBuilder) this;
                easyOptions.postType = builder.postType;
            }
            if (params != null && !params.isEmpty()) {
                easyOptions.params = params;
            }
            if (sigHeaders != null && !sigHeaders.isEmpty()) {
                easyOptions.sigHeaders = sigHeaders;
            }
            if (mulHeaders != null && !mulHeaders.isEmpty()) {
                easyOptions.mulHeaders = mulHeaders;
            }

            return easyOptions;
        }

    }

    private static final class OKHttpOptionsPostBuilder extends OKHttpOptionsBuilder {

        private OKHttpOptionsBuilder mBuilder;
        final String url;
        final String path;
        final Map<String, Object> params; // 参数
        Map<String, List<String>> mulHeaders;
        Map<String, String> sigHeaders;
        int postType = JSON;

        OKHttpOptionsPostBuilder(OKHttpOptionsBuilder builder) {
            url = builder.url;
            path = builder.path;
            params = builder.params;
            sigHeaders = builder.sigHeaders;
            mulHeaders = builder.mulHeaders;
            mBuilder = builder;
        }

        /**
         * 指定post提交参数的形式
         *
         * @param postType {@link PostType}
         * @see #method(int)
         */
        public OKHttpOptionsPostBuilder postType(@PostType int postType) {
            if (postType != JSON && postType != FROM) {
                throw new IllegalArgumentException("see PostType");
            }
            this.postType = postType;
            return this;
        }

        @Override
        public OKHttpOptionsBuilder url(String url) {
            return mBuilder.url(url);
        }

        @Override
        public OKHttpOptionsBuilder path(String path) {
            return mBuilder.path(path);
        }

        @Override
        public OKHttpOptionsBuilder method(@Method int method) {
            throw new UnsupportedOperationException();
        }

        @Override
        public OKHttpOptionsBuilder param(String key, Object value) {
            return mBuilder.param(key, value);
        }

        @Override
        public OKHttpOptionsBuilder params(Map<String, Object> params) {
            return mBuilder.params(params);
        }

        @Override
        public OKHttpOptionsBuilder header(Map<String, String> header) {
            return mBuilder.header(header);
        }

        @Override
        public OKHttpOptionsBuilder headers(Map<String, List<String>> headers) {
            return mBuilder.headers(headers);
        }

        @Override
        public EasyOptions build() {
            return mBuilder.build();
        }
    }
}
