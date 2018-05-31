package com.smart.tuya.meshdemo.view;

import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.List;

/**
 * Created by zhusg on 2018/5/29.
 */

public interface IMeshDeviceListView {
    void loadStart();

    void loadFinish();

    void updateUi(List<DeviceBean> deviceBeanList);
    void updateUi(DeviceBean deviceBean);

}
