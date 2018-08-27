package com.smart.tuya.meshdemo.view;

/**
 * Created by zsg on 17/8/27.
 */


import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.List;

/**
 * 群组设备管理界面接口
 * Created by chenshixin on 15/12/11.
 */
public interface IMeshGroupDeviceListView {


    /**
     * 更新群组设备列表
     */
    void updateAddDeviceList(List<DeviceBean> addBeanList, List<DeviceBean> foundBeanList);

    /**
     * 刷新数据
     */
    void refreshList();

    void setConfimBtnClickable(boolean b);

    void finishActivity();
}
