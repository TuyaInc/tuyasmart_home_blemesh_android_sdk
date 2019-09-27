package com.smart.tuya.meshdemo.presenter;

import com.tuya.smart.sdk.api.IResultCallback;

/**
 * @author aze
 * @Des
 * @date 2019-07-18.
 */
public interface IControlPresenter {
    void requestDeviceInfo();

    void sendDps(String command, IResultCallback callback);

    void onDestroy();
}
