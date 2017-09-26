/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fly.easy;

import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.ResponseBody;

/**
 * An HTTP response.
 */
public final class EasyResponse {


    private final okhttp3.Response rawResponse;
    private final
    @Nullable
    ResponseBody body;
    private final String data;

    EasyResponse(okhttp3.Response rawResponse, @Nullable ResponseBody body) throws IOException {
        this.rawResponse = rawResponse;
        this.body = body;
        data = body.string();
    }

    public String string() {
        return data;
    }

    /**
     * The raw response from the HTTP client.
     */
    public okhttp3.Response raw() {
        return rawResponse;
    }

    /**
     * HTTP status code.
     */
    public int code() {
        return rawResponse.code();
    }

    /**
     * HTTP status message or null if unknown.
     */
    public String message() {
        return rawResponse.message();
    }

    /**
     * HTTP headers.
     */
    public Headers headers() {
        return rawResponse.headers();
    }

    /**
     * Returns true if {@link #code()} is in the range [200..300).
     */
    public boolean isSuccessful() {
        return rawResponse.isSuccessful();
    }

    /**
     * The deserialized response body of a {@linkplain #isSuccessful() successful} response.
     */
    public
    @Nullable
    ResponseBody body() {
        return body;
    }

    /**
     * The raw response body of an {@linkplain #isSuccessful() unsuccessful} response.
     */
    @Override
    public String toString() {
        return rawResponse.toString();
    }

}
