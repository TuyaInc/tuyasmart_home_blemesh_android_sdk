package com.smart.tuya.meshdemo.presenter;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;

import android.text.TextUtils;
import android.util.Log;

import android.widget.Toast;


import com.smart.tuya.meshdemo.activity.LightControlActivity;
import com.smart.tuya.meshdemo.utils.BluetoothUtils;
import com.smart.tuya.meshdemo.utils.CheckPermissionUtils;
import com.smart.tuya.meshdemo.utils.DialogUtils;
import com.smart.tuya.meshdemo.view.IMeshDeviceListView;
import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshDevice;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.api.ITuyaHome;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.bluemesh.IMeshDevListener;
import com.tuya.smart.sdk.bean.BlueMeshBean;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by zhusg on 2018/5/29.
 */

public class MeshDeviceListPresenter {

    private static final String TAG = "MeshDeviceListPresenter";
    public Activity mContext;
    private long homeId;
    private String meshId;
    private BlueMeshBean meshBean;

    private ITuyaHome mTuyaHome;
    private IMeshDeviceListView mView;
    private ITuyaBlueMeshDevice mTuyaBlueMeshDevice;

    public CheckPermissionUtils checkPermission;


    public static final int REQUEST_OPEN_BLE = 1234;
    public static final int REQUEST_CODE_FOR_PERMISSION = 222;
    public static final int GPS_REQUEST_CODE = 223;

    private IMeshDevListener iMeshDevListener = new IMeshDevListener() {
        /**
         * 数据更新
         * @param nodeId    更新设备的nodeId
         * @param dps       dp数据
         * @param isFromLocal   数据来源 true表示从本地蓝牙  false表示从云端
         */
        @Override
        public void onDpUpdate(String nodeId, String dps, boolean isFromLocal) {
            DeviceBean deviceBean = mTuyaBlueMeshDevice.getMeshSubDevBeanByNodeId(nodeId);
            Log.d(TAG, "onDpUpdate nodeId:" + nodeId + "  dps:" + dps);

        }

        /**
         * 设备状态的上报
         * @param online    在线设备列表
         * @param offline   离线设备列表
         * @param gwId      状态的来源 gwId不为空表示来自云端（gwId是上报数据的网关Id）   为空则表示来自本地蓝牙
         */
        @Override
        public void onStatusChanged(List<String> online, List<String> offline, String gwId) {
            if (online != null) {
                Log.d(TAG, "onStatusChanged  onLine:" + online.toString());

            }
            if (offline != null) {
                Log.d(TAG, "onStatusChanged  offline:" + offline.toString());
            }

            if (online != null) {
                for (String nodeId : online) {
                    DeviceBean deviceBean = mTuyaBlueMeshDevice.getMeshSubDevBeanByNodeId(nodeId);
                    if (deviceBean != null) {
                        notifyDeviceStatusChange(deviceBean);
                    }
                }
            }

            if (offline != null) {
                for (String nodeId : offline) {
                    DeviceBean deviceBean = mTuyaBlueMeshDevice.getMeshSubDevBeanByNodeId(nodeId);
                    if (deviceBean != null) {
                        notifyDeviceStatusChange(deviceBean);
                    }
                }
            }


        }

        /**
         * 网络状态变化
         * @param s
         * @param b
         */
        @Override
        public void onNetworkStatusChanged(String s, boolean b) {

        }

        /**
         * raw类型数据上报
         * @param bytes
         */
        @Override
        public void onRawDataUpdate(byte[] bytes) {
        }

        @Override
        public void onDevInfoUpdate(String devId) {
            DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(devId);
            if (deviceBean != null)
                notifyDeviceStatusChange(deviceBean);
        }

        @Override
        public void onRemoved(String devId) {
            mView.updateUi(mTuyaHome.getHomeBean().getDeviceList());
        }
    };

    private void notifyDeviceStatusChange(DeviceBean deviceBean) {
        mView.updateUi(deviceBean);

    }

    public MeshDeviceListPresenter(Activity context, IMeshDeviceListView view, long homeId, String meshId) {
        this.mContext = context;
        this.homeId = homeId;
        mTuyaHome = TuyaHomeSdk.newHomeInstance(homeId);
        this.mView = view;
        this.meshId = meshId;
        meshBean = TuyaHomeSdk.getMeshInstance().getBlueMeshBean(meshId);
        mTuyaBlueMeshDevice = TuyaHomeSdk.newBlueMeshDeviceInstance(meshId);
        mTuyaBlueMeshDevice.registerMeshDevListener(iMeshDevListener);

        checkPermission = new CheckPermissionUtils(context);


    }

    public void getDataFromServer() {
        mTuyaHome.getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                mView.updateUi(homeBean.getDeviceList());
                mView.loadFinish();
            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                Toast.makeText(mContext, errorCode + " " + errorMsg, Toast.LENGTH_LONG).show();
                mView.loadFinish();
            }
        });
    }

    public void startClient() {
        if (meshBean != null) {
            if (check()) {
                TuyaHomeSdk.getTuyaBlueMeshClient().startClient(meshBean);
            }
        } else {
            Toast.makeText(mContext, "meshBean is null", Toast.LENGTH_LONG).show();
        }
    }

    public void stopClient() {
        TuyaHomeSdk.getTuyaBlueMeshClient().stopClient();
    }

    public void onDestroy() {
        if (mTuyaBlueMeshDevice != null) {
            mTuyaBlueMeshDevice.unRegisterMeshDevListener();
        }
    }


    public boolean check() {
        //检查 蓝牙和位置权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2
                || !mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //不支持ble
            Toast.makeText(mContext, "This version not support bluetooth", Toast.LENGTH_LONG).show();
            return false;
        }

        if (checkBluetooth() == MESH_BLUETOOTH_CLOSE) {
            //不支持ble
            Toast.makeText(mContext, "请先开启蓝牙", Toast.LENGTH_LONG).show();
        } else {
            //检查位置权限
            Log.d(TAG, "check location permission");
            if (checkPermission.checkSinglePermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_CODE_FOR_PERMISSION)
                    && checkPermission.checkSinglePermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_CODE_FOR_PERMISSION)) {
                //检查gps
                Log.d(TAG, "check location gps");
                LocationManager locationManager = (LocationManager) mContext
                        .getSystemService(Context.LOCATION_SERVICE);
                // 判断GPS模块是否开启，如果没有则开启
                if (!locationManager
                        .isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {

                    Toast.makeText(mContext, "请先开启GPS", Toast.LENGTH_LONG).show();


                } else {
                    return true;
                }
            } else {
                Toast.makeText(mContext, "请先开启定位权限", Toast.LENGTH_LONG).show();
            }
        }

        return false;

    }

    public static final int MESH_BLUETOOTH_OPEN = 1;
    public static final int MESH_BLUETOOTH_CLOSE = 2;
    public static final int MESH_BLUETOOTH_NULL = -1;

    public int checkBluetooth() {
        if (mContext == null)
            return 0;

        if (!BluetoothUtils.isBleSupported(mContext)) {
            return MESH_BLUETOOTH_NULL;
        }
        if (BluetoothUtils.isBluetoothEnabled()) {
            return MESH_BLUETOOTH_OPEN;
        } else {
            return MESH_BLUETOOTH_CLOSE;
        }

    }

    public void itemOnClick(String devId) {
        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(devId);
        //判断是不是mesh设备
        if (TextUtils.isEmpty(deviceBean.getMeshId())) {
            Toast.makeText(mContext, "当前设备不是 mesh产品", Toast.LENGTH_LONG).show();

            return;
        }

        if (deviceBean != null) {
            if (deviceBean.getIsOnline()) {
                showLightControlActivity(deviceBean, devId);
            } else {
                Toast.makeText(mContext, "设备离线 无法控制", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(mContext, "device not exist", Toast.LENGTH_LONG).show();
        }
    }

    private void showLightControlActivity(DeviceBean deviceBean, String devId) {
        Intent intent = null;
        //判断当前设备产品类型
        String vendorId = deviceBean.getCategory();
        if (vendorId.endsWith("01")) {
            //灯大类
            intent = new Intent(mContext, LightControlActivity.class);
        } else if (vendorId.endsWith("02")) {
            //电工
        } else if (vendorId.endsWith("04")) {
            //传感器
        } else if (vendorId.endsWith("10")) {
            //执行器
        } else if (vendorId.equals("08")) {
            //适配器(网关)
        }

        if (intent != null) {
            intent.putExtra("devId", devId);
            mContext.startActivity(intent);
        }
    }


    public void itemOnLongClick(final String devId) {
        final DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(devId);

        //判断是不是mesh设备
        if (TextUtils.isEmpty(deviceBean.getMeshId())) {
            Toast.makeText(mContext, "当前设备不是 mesh产品", Toast.LENGTH_LONG).show();

            return;
        }
        if (deviceBean != null) {
            DialogUtils.simpleConfirmDialog(mContext, "Tip", "是否删除 " + deviceBean.getName(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == -1) {
                        mTuyaBlueMeshDevice.removeMeshSubDev(devId, new IResultCallback() {
                            @Override
                            public void onError(String s, String s1) {
                                Toast.makeText(mContext, "删除设备失败 " + s + "  " + s1, Toast.LENGTH_LONG).show();

                            }

                            @Override
                            public void onSuccess() {
                                Toast.makeText(mContext, "删除设备成功", Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                }
            });
        } else {
            Toast.makeText(mContext, "device not exist", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * 查询所有设备的信息
     */


    public void getStatusAll() {
        //List<DeviceBean> deviceBeanList=TuyaHomeSdk.getDataInstance().getHomeDeviceList(homeId);
        mTuyaBlueMeshDevice.queryAllStatusByLocal(new IResultCallback() {
            @Override
            public void onError(String s, String s1) {

            }

            @Override
            public void onSuccess() {

            }
        });
//        for(DeviceBean bean:deviceBeanList){
//            mTuyaBlueMeshDevice.querySubDevStatusByLocal(bean.getCategory(), bean.getNodeId(), new IResultCallback() {
//                @Override
//                public void onError(String s, String s1) {
//
//                }
//
//                @Override
//                public void onSuccess() {
//
//                }
//            });
//            try {
//                Thread.sleep(350);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }

    public void doOpenAll() {
        final String dps = "{\"1\":true}";

        final List<DeviceBean> deviceBeanList = TuyaHomeSdk.getDataInstance().getHomeDeviceList(homeId);
        Observable.interval(0, 350, TimeUnit.MILLISECONDS)
                .map(new Function<Long, DeviceBean>() {
                    @Override
                    public DeviceBean apply(Long aLong) throws Exception {
                        return deviceBeanList.get(aLong.intValue());
                    }
                }).take(deviceBeanList.size())
                .subscribe(new Consumer<DeviceBean>() {
                    @Override
                    public void accept(final DeviceBean devBean) throws Exception {
                        L.e(TAG, "call:" + System.currentTimeMillis() + "  ");
                        mTuyaBlueMeshDevice.publishDps(devBean.getNodeId(), devBean.getCategory(), dps, new IResultCallback() {
                            @Override
                            public void onError(String s, String errorMsg) {
                                L.e(TAG, devBean.getName() + "  发送失败");
                            }

                            @Override
                            public void onSuccess() {
                                L.e(TAG, devBean.getName() + "  发送成功");
                            }
                        });
                    }
                });
//        for (final DeviceBean devBean : deviceBeanList) {
//            new Thread(){
//                @Override
//                public void run() {
//                    mTuyaBlueMeshDevice.publishDps(devBean.getNodeId(), devBean.getCategory(), dps, new IResultCallback() {
//                        @Override
//                        public void onError(String s, String errorMsg) {
//                            L.e(TAG,devBean.getName()+"  发送失败");
//                        }
//
//                        @Override
//                        public void onSuccess() {
//                            L.e(TAG,devBean.getName()+"  发送成功");
//                        }
//                    });
//                }
//            }.start();


    }

    public void doCloseAll() {
        final String dps = "{\"1\":false}";
        final List<DeviceBean> deviceBeanList = TuyaHomeSdk.getDataInstance().getHomeDeviceList(homeId);
        Observable.interval(0, 350, TimeUnit.MILLISECONDS)
                .map(new Function<Long, DeviceBean>() {
                    @Override
                    public DeviceBean apply(Long aLong) throws Exception {
                        return deviceBeanList.get(aLong.intValue());
                    }
                }).take(deviceBeanList.size())
                .subscribe(new Consumer<DeviceBean>() {
                    @Override
                    public void accept(final DeviceBean devBean) throws Exception {
                        L.e(TAG, "call:" + System.currentTimeMillis() + "  ");
                        mTuyaBlueMeshDevice.publishDps(devBean.getNodeId(), devBean.getCategory(), dps, new IResultCallback() {
                            @Override
                            public void onError(String s, String errorMsg) {
                                L.e(TAG, devBean.getName() + "  发送失败");
                            }

                            @Override
                            public void onSuccess() {
                                L.e(TAG, devBean.getName() + "  发送成功");
                            }
                        });
                    }
                });
    }

}
