package fly.com.easy;

import java.io.IOException;

import okhttp3.Response;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public class EasyResponseFailedException extends IOException {

    private Response mResponse;

    public EasyResponseFailedException(Response response) {
        mResponse = response;
    }

    public Response getResponse() {
        return mResponse;
    }

    @Override
    public String getMessage() {
        return mResponse.message();
    }
}
