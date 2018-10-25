package com.smart.tuya.meshdemo.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


import com.smart.tuya.meshdemo.R;

import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshActivatorListener;
import com.tuya.smart.android.blemesh.bean.SearchDeviceBean;
import com.tuya.smart.android.blemesh.builder.TuyaBlueMeshActivatorBuilder;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.bluemesh.ITuyaBlueMeshActivator;
import com.tuya.smart.sdk.bean.BlueMeshBean;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsg on 17/8/3.
 */

public class ConfigTipFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "AddDeviceTipFragment";
    public static final int CONFIG_DEV_MAX_TIME = 120;
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
        TuyaHomeSdk.getTuyaBlueMeshClient().stopClient();


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
        BlueMeshBean blueMeshBean = TuyaHomeSdk.getMeshInstance().getBlueMeshBean(meshId);
        if (blueMeshBean == null) {
            Log.e(TAG, "blueMeshBean is null");
        }
        TuyaBlueMeshActivatorBuilder tuyaBlueMeshActivatorBuilder = new TuyaBlueMeshActivatorBuilder()
                .setSearchDeviceBeans(foundDevice)
                //默认版本号
                .setVersion("1.0")
                .setBlueMeshBean(blueMeshBean)
                //超时时间
                .setTimeOut(CONFIG_DEV_MAX_TIME)
                .setTuyaBlueMeshActivatorListener(new ITuyaBlueMeshActivatorListener() {

                    @Override
                    public void onSuccess(String mac, DeviceBean deviceBean) {
                        Log.d(TAG, "subDevBean onSuccess: " + deviceBean.getName());
                        succCount++;
                        updateView();
                    }

                    @Override
                    public void onError(String mac, String errorCode, String errorMsg) {
                        Log.d(TAG, "config mesh error" + errorCode + " " + errorMsg);
                        failCount = 0;
                        updateView();
                    }

                    @Override
                    public void onFinish() {
                        Log.d(TAG, "config mesh onFinish： ");
                        configFinish();
                    }
                });

        iTuyaBlueMeshActivator = TuyaHomeSdk.getTuyaBlueMeshConfig().newActivator(tuyaBlueMeshActivatorBuilder);
        iTuyaBlueMeshActivator.startActivator();

    }


    public void configWifiMesh(List<SearchDeviceBean> foundDevice, String meshId) {
        String wifiName = getArguments().getString("extra_wifi_ssid");
        String wifiPwd = getArguments().getString("extra_wifi_password");
        long homeId = getArguments().getLong("extra_home_id");
        allCount = foundDevice.size();
        succCount = 0;
        failCount = 0;
        updateView();
        BlueMeshBean blueMeshBean = TuyaHomeSdk.getMeshInstance().getBlueMeshBean(meshId);
        if (blueMeshBean == null) {
            Log.e(TAG, "blueMeshBean is null");
        }
        TuyaBlueMeshActivatorBuilder tuyaBlueMeshActivatorBuilder = new TuyaBlueMeshActivatorBuilder()
                .setWifiSsid(wifiName)
                .setWifiPassword(wifiPwd)
                .setSearchDeviceBeans(foundDevice)
                .setVersion("2.2")
                .setBlueMeshBean(blueMeshBean)
                .setHomeId(homeId)
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
                        Log.d(TAG, "errorCode: " + errorCode + " errorMsg: " + errorMsg);
                        failCount = 0;
                        updateView();
                    }

                    @Override
                    public void onFinish() {
                        //所有设备配网结束回调
                        Log.d(TAG, "subDevBean onFinish: ");
                        configFinish();

                    }
                });

        iTuyaBlueMeshActivator = TuyaHomeSdk.getTuyaBlueMeshConfig().newWifiActivator(tuyaBlueMeshActivatorBuilder);
        iTuyaBlueMeshActivator.startActivator();

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

