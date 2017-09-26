package com.fly.easy;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;
import java.util.Map;

import okhttp3.Headers;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public class EasyUtils {
    private EasyUtils() {
        throw new UnsupportedOperationException();
    }

    static <T> T checkNotNull(@Nullable T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

    static String checkStringArgument(@Nullable String str, String message) {
        if (TextUtils.isEmpty(str)) {
            throw new IllegalArgumentException(message);
        }
        return str;
    }

    static <T extends List> T checkListNotEmpty(@Nullable T object, String message) {
        if (object.isEmpty()) {
            throw new NullPointerException(message);
        }
        return object;
    }

    static <T extends Map> T checkMapNotEmpty(@Nullable T object, String message) {
        if (object.isEmpty()) {
            throw new NullPointerException(message);
        }
        return object;
    }

    static Headers checkHeadersNotEmpty(@Nullable Headers headers, String message) {
        if (headers.size() <= 0) {
            throw new NullPointerException(message);
        }
        return headers;
    }

}
