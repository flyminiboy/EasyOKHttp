package com.fly.easy;

import java.io.IOException;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public class EasyResponseFailedException extends IOException {

    private EasyResponse mResponse;

    public EasyResponseFailedException(EasyResponse response) {
        mResponse = response;
    }

    public EasyResponse getResponse() {
        return mResponse;
    }

    @Override
    public String getMessage() {
        return mResponse.message();
    }
}
