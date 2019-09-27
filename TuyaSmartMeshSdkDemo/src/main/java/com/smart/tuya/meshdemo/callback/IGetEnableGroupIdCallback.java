package com.smart.tuya.meshdemo.callback;

public interface IGetEnableGroupIdCallback {
    void onSuccess(String enableGroupId);

    void onError(String errorCode, String errorMessage);
}
