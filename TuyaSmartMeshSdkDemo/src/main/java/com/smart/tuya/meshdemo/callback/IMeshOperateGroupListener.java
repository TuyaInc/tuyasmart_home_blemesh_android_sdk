package com.smart.tuya.meshdemo.callback;

import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;

/**
 * Created by zsg on 17/11/14.
 */

public interface IMeshOperateGroupListener {
    void operateSuccess(DeviceBean bean, int index);
    void operateFinish(ArrayList<DeviceBean> failList);
    void operateFail(DeviceBean bean, int index);

}
