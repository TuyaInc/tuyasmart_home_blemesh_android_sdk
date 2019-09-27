package com.smart.tuya.meshdemo.presenter.sigmesh;

import android.content.Context;
import android.util.Log;

import com.smart.tuya.meshdemo.presenter.IControlPresenter;
import com.smart.tuya.meshdemo.view.IControlView;
import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshDevice;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.bluemesh.IMeshDevListener;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.List;

/**
 * @author aze
 * @Des
 * @date 2019-07-18.
 */

public class SigMeshControlPresenter implements IControlPresenter {
    private static final String TAG = "SigMeshControlPresenter";
    private Context mContext;
    private String mDevId;
    private ITuyaBlueMeshDevice mTuyaBlueMeshDevice;
    private DeviceBean devBean;
    private IControlView mView;

    public SigMeshControlPresenter(Context context, String devId, IControlView view) {
        this.mContext = context;
        this.mView=view;
        this.mDevId = devId;
        devBean = TuyaHomeSdk.getDataInstance().getDeviceBean(mDevId);
        mTuyaBlueMeshDevice = TuyaHomeSdk.newSigMeshDeviceInstance(devBean.getMeshId());

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



    @Override
    public void sendDps(String command, IResultCallback callback) {
        Log.d(TAG,"sendCommand : "+command);
        mTuyaBlueMeshDevice.publishDps(devBean.getNodeId(), devBean.getCategory(), command, callback);
    }

    //获取设备当前dp点信息
    @Override
    public void requestDeviceInfo() {
        mTuyaBlueMeshDevice.querySubDevStatusByLocal(devBean.getCategory(), devBean.getNodeId(), null);
    }
    @Override
    public void onDestroy() {
        if(mTuyaBlueMeshDevice!=null){
            mTuyaBlueMeshDevice.unRegisterMeshDevListener();
            mTuyaBlueMeshDevice.onDestroy();
        }
    }
}

