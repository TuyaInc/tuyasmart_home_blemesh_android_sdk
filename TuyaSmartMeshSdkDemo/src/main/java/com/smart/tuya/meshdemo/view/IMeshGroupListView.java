package com.smart.tuya.meshdemo.view;

import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;

import java.util.List;

/**
 * Created by zhusg on 2018/5/29.
 */

public interface IMeshGroupListView {
    void loadStart();

    void loadFinish();

    void updateUi(List<GroupBean> groupBeanList);
    void updateUi(GroupBean groupBean);

}
