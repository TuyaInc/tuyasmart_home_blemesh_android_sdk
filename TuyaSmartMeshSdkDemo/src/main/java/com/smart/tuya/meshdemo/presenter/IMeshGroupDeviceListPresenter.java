package com.smart.tuya.meshdemo.presenter;

import com.tuya.smart.sdk.bean.DeviceBean;

/**
 * @author aze
 * @Des
 * @date 2019-07-18.
 */
public interface IMeshGroupDeviceListPresenter {
    void onClickSelect(int actionType, DeviceBean bean);
    void doConfirm();
    void doOpen();
    void doClose();
}
