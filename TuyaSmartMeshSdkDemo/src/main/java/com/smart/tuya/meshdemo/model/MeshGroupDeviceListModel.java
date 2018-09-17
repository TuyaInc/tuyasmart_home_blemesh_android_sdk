package com.smart.tuya.meshdemo.model;

import android.content.Context;

import com.smart.tuya.meshdemo.callback.IMeshOperateGroupListener;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.mvp.model.BaseModel;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaGroup;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsg on 17/11/14.
 */

public class MeshGroupDeviceListModel extends BaseModel {
    private static String TAG = "MeshGroupDeviceListModelhuohuo";
    private ArrayList<DeviceBean> operateFailDeviceList;


    public MeshGroupDeviceListModel(Context ctx) {
        super(ctx);
        operateFailDeviceList = new ArrayList<>();
    }

    @Override
    public void onDestroy() {

    }

    public void addDeviceToMeshGroup(DeviceBean groupDevice, ITuyaGroup mGroup, IResultCallback callback) {
        mGroup.addDevice(groupDevice.getDevId(), callback);
    }

    public void removeDeviceToMeshGroup(DeviceBean groupDevice, ITuyaGroup mGroup, IResultCallback callback) {
        mGroup.removeDevice(groupDevice.getDevId(), callback);
    }


    public void operateDevice(ITuyaGroup mGroup, final ArrayList<DeviceBean> addBeans, final ArrayList<DeviceBean> removeBeans,
                              final String mVendorId, IMeshOperateGroupListener listener) {
        operateFailDeviceList.clear();
        operateDevice(mGroup, addBeans, removeBeans, mVendorId, 0, listener, 2);

    }

    /**
     * 执行添加 和 删除操作
     *
     * @param mGroup
     * @param addBeans
     * @param removeBeans
     * @param mVendorId
     * @param currentIndex
     * @param listener
     * @param tryCount     重试次数
     */
    public void operateDevice(final ITuyaGroup mGroup, final ArrayList<DeviceBean> addBeans, final ArrayList<DeviceBean> removeBeans,
                              final String mVendorId, final int currentIndex, final IMeshOperateGroupListener listener, final int tryCount) {
        int subCount = addBeans.size() + removeBeans.size();

        if (currentIndex < addBeans.size()) {
            final DeviceBean addDevice = addBeans.get(currentIndex);
            addDeviceToMeshGroup(addDevice,  mGroup, new IResultCallback() {
                @Override
                public void onError(String s, String s1) {
                    L.e(TAG, "addDeviceToMeshGroup  fail  " + s + "  " + s1);
                    if (tryCount > 0) {
                        //重试
                        operateDevice(mGroup, addBeans, removeBeans, mVendorId, currentIndex, listener, tryCount - 1);
                    } else {
                        listener.operateFail(addDevice, currentIndex + 1);
                        operateFailDeviceList.add(addDevice);
                        operateDevice(mGroup, addBeans, removeBeans, mVendorId, currentIndex + 1, listener, 2);
                    }
                }

                @Override
                public void onSuccess() {
                    L.e(TAG, "addDeviceToMeshGroup  onSuccess  ");
                    listener.operateSuccess(addDevice, currentIndex + 1);
                    operateDevice(mGroup, addBeans, removeBeans, mVendorId, currentIndex + 1, listener, 2);
                }
            });
        } else if (currentIndex < subCount) {
            final DeviceBean removeDevice = removeBeans.get(currentIndex - addBeans.size());

            removeDeviceToMeshGroup(removeDevice, mGroup, new IResultCallback() {
                @Override
                public void onError(String s, String s1) {
                    L.e(TAG, "removeDeviceToMeshGroup  fail  " + s + "  " + s1);
                    if (tryCount > 0) {
                        //重试
                        operateDevice(mGroup, addBeans, removeBeans, mVendorId, currentIndex, listener, tryCount - 1);
                    } else {
                        listener.operateFail(removeDevice, currentIndex + 1);
                        operateFailDeviceList.add(removeDevice);
                        operateDevice(mGroup, addBeans, removeBeans, mVendorId, currentIndex + 1, listener, 2);
                    }
                }

                @Override
                public void onSuccess() {
                    L.e(TAG, "removeDeviceToMeshGroup  onSuccess");
                    listener.operateSuccess(removeDevice, currentIndex + 1);
                    operateDevice(mGroup, addBeans, removeBeans, mVendorId, currentIndex + 1, listener, 2);
                }
            });
        } else {
            //操作结束
            listener.operateFinish(operateFailDeviceList);
        }

    }


    public String getEnableGroupId(String meshId) {

        List<GroupBean> groupBeanList = TuyaHomeSdk.getDataInstance().getMeshGroupList(meshId);

        if (groupBeanList == null || groupBeanList.size() == 0) {
            return "8001";
        } else {
            String[] localIds = {"8001", "8002", "8003", "8004",
                    "8005", "8006", "8007", "8008"};
            List<String> localIdList = new ArrayList<>();
            for (String id : localIds) {
                localIdList.add(id);
            }
            for (GroupBean bean : groupBeanList) {
                if (localIdList.contains(bean.getLocalId())) {
                    localIdList.remove(bean.getLocalId());
                }
            }
            if (localIdList.size() == 0) {
                //callback.onFail(mContext.getString(R.string.mesh_group_full_tip));
                return "";
            } else {
                return localIdList.get(0);
            }
        }

    }


}
