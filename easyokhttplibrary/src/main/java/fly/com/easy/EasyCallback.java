package fly.com.easy;

import okhttp3.Response;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public interface EasyCallback {
    /**
     * Invoked for a received HTTP response.
     * <p>
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
     * Call {@link Response#isSuccessful()} to determine if the response indicates success.
     */
    void onResponse(EasyCall call, Response response);

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     */
    void onFailure(EasyCall call, Throwable t);

}
