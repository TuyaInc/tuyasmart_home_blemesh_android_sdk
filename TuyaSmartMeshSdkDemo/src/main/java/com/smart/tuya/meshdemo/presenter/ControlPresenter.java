package com.smart.tuya.meshdemo.presenter;

import android.content.Context;
import android.util.Log;

import com.smart.tuya.meshdemo.view.IControlView;
import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshDevice;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.bluemesh.IMeshDevListener;
import com.tuya.smart.sdk.bean.DeviceBean;
import java.util.List;

/**
 * Created by zhusg on 2018/5/30.
 */

public class ControlPresenter {
    private static final String TAG = "ControlPresenter";
    private Context mContext;
    private String mDevId;
    private ITuyaBlueMeshDevice mTuyaBlueMeshDevice;
    private DeviceBean devBean;
    private IControlView mView;

    public ControlPresenter(Context context, String devId,IControlView view) {
        this.mContext = context;
        this.mView=view;
        this.mDevId = devId;
        devBean = TuyaHomeSdk.getDataInstance().getDeviceBean(mDevId);
        mTuyaBlueMeshDevice = TuyaHomeSdk.newBlueMeshDeviceInstance(devBean.getMeshId());


        mTuyaBlueMeshDevice.registerMeshDevListener(new IMeshDevListener() {
            @Override
            public void onDpUpdate(String nodeId, String dps, boolean isFromLocal) {
                Log.d(TAG, "nodeId:" + nodeId + " now nodeId:" + devBean.getNodeId() + "  dps:" + dps);
                if (devBean.getNodeId().equals(nodeId)) {
                    mView.updateView(dps);
                }
            }

            @Override
            public void onStatusChanged(List<String> online, List<String> offline, String gwId) {

            }

            @Override
            public void onNetworkStatusChanged(String s, boolean b) {

            }

            @Override
            public void onRawDataUpdate(byte[] bytes) {

            }

            @Override
            public void onDevInfoUpdate(String devId) {
            }

            @Override
            public void onRemoved(String devId) {

            }
        });
    }


    public void sendDps(String command,IResultCallback callback) {
        Log.d(TAG,"sendCommand : "+command);
        mTuyaBlueMeshDevice.publishDps(devBean.getNodeId(), devBean.getCategory(), command, callback);
    }

    //获取设备当前dp点信息
    public void requestDeviceInfo() {
        mTuyaBlueMeshDevice.querySubDevStatusByLocal(devBean.getCategory(), devBean.getNodeId(), null);
    }

    public void onDestroy() {
        if(mTuyaBlueMeshDevice!=null){
            mTuyaBlueMeshDevice.unRegisterMeshDevListener();
            mTuyaBlueMeshDevice.onDestroy();
        }
    }
}
