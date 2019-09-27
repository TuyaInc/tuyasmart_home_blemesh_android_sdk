package com.smart.tuya.meshdemo.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.tuya.meshdemo.R;
import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshActivatorListener;
import com.tuya.smart.android.blemesh.bean.SearchDeviceBean;
import com.tuya.smart.android.blemesh.builder.TuyaSigMeshActivatorBuilder;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.bluemesh.ITuyaBlueMeshActivator;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.SigMeshBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author aze
 * @Des
 * @date 2019-07-16.
 */
public class SigMeshTipFragment  extends Fragment implements View.OnClickListener {
    private static final String TAG = "SigMeshTipFragment";
    public static final int CONFIG_DEV_MAX_TIME = 240;
    private View mContentView;
    protected FragmentActivity mActivity;

    protected TextView mTvConfigTip;
    protected TextView mTvClose;
    private ITuyaBlueMeshActivator iTuyaBlueMeshActivator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_config_tip, container, false);
        initView(mContentView);
        config();
        return mContentView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (FragmentActivity) context;
    }

    protected void initView(View view) {
        mTvConfigTip = (TextView) view.findViewById(R.id.ty_config_tip);
        mTvClose = (TextView) view.findViewById(R.id.btn_close);
        mTvClose.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_close) {
            mActivity.finish();
        }
    }

    public void config() {
        //配网前要先停止连接
        TuyaHomeSdk.getTuyaSigMeshClient().stopClient();

        int configType = getArguments().getInt("extra_config_type", 0);
        String meshId = getArguments().getString("extra_mesh_id");
        ArrayList<SearchDeviceBean> foundDevice = getArguments().getParcelableArrayList("extra_found_device");
        if (configType == 1) {
            //普通设备配网
            configMesh(foundDevice, meshId);
        } else if (configType == 2) {
            //网关设备配网
            configWifiMesh(foundDevice, meshId);
        }
    }

    int allCount = 0;
    int succCount = 0;
    int failCount = 0;

    private void configMesh(List<SearchDeviceBean> foundDevice, String meshId) {
        allCount = foundDevice.size();
        succCount = 0;
        failCount = 0;
        updateView();
        final SigMeshBean sigMeshBean = TuyaHomeSdk.getSigMeshInstance().getSigMeshBean(meshId);
        if (sigMeshBean == null) {
            Log.e(TAG, "sigMeshBean is null");
        }
        TuyaSigMeshActivatorBuilder tuyaSigMeshActivatorBuilder=new TuyaSigMeshActivatorBuilder()
                .setSearchDeviceBeans(foundDevice)
                .setSigMeshBean(sigMeshBean)
                .setTimeOut(CONFIG_DEV_MAX_TIME)
                .setTuyaBlueMeshActivatorListener(new ITuyaBlueMeshActivatorListener() {
                    @Override
                    public void onSuccess(String mac, DeviceBean deviceBean) {
                        //单个设备配网成功回调
                        Log.d(TAG, "startConfig  success");
                        succCount++;
                        updateView();
                    }

                    @Override
                    public void onError(String mac, String errorCode, String errorMsg) {
                        //单个设备配网失败回调
                        Log.d(TAG, "key is"+sigMeshBean.getMeshkey()+"mac:"+mac+"errorCode: " + errorCode + " errorMsg: " + errorMsg);
                        failCount ++;
                        updateView();
                    }

                    @Override
                    public void onFinish() {
                        //所有设备配网结束回调
                        Log.d(TAG, "subDevBean onFinish: ");
                        configFinish();
                    }
                });

        iTuyaBlueMeshActivator = TuyaHomeSdk.getTuyaBlueMeshConfig().newSigActivator(tuyaSigMeshActivatorBuilder);
        iTuyaBlueMeshActivator.startActivator();

    }


    public void configWifiMesh(List<SearchDeviceBean> foundDevice, String meshId) {
        Toast.makeText(getContext(),"暂未提供网关配网实现",Toast.LENGTH_SHORT).show();
    }


    private void updateView() {
        String tip = String.format("配网设备数量:%d  成功:%d  失败:%d", allCount, succCount, failCount);
        mTvConfigTip.setText(tip);
    }

    private void configFinish() {
        String tip = String.format("配网结束  成功:%d  失败:%d", succCount, failCount);
        mTvConfigTip.setText(tip);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (iTuyaBlueMeshActivator != null) {
            iTuyaBlueMeshActivator.stopActivator();
        }
    }
}
