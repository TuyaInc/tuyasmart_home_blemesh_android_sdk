package com.smart.tuya.meshdemo.presenter.sigmesh;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ListHolder;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.smart.tuya.meshdemo.Config.MeshTypeConfig;
import com.smart.tuya.meshdemo.R;
import com.smart.tuya.meshdemo.activity.ConfigTipActivity;
import com.smart.tuya.meshdemo.presenter.IMeshPresenter;
import com.smart.tuya.meshdemo.utils.BluetoothUtils;
import com.smart.tuya.meshdemo.utils.CheckPermissionUtils;
import com.smart.tuya.meshdemo.utils.DialogUtils;
import com.smart.tuya.meshdemo.view.IMeshDemoView;
import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshSearch;
import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshSearchListener;
import com.tuya.smart.android.blemesh.bean.SearchDeviceBean;
import com.tuya.smart.android.blemesh.bean.SigMeshSearchDeviceBean;
import com.tuya.smart.android.blemesh.builder.SearchBuilder;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.api.ITuyaHome;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.bean.SigMeshBean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

/**
 * @author aze
 * @Des
 * @date 2019-07-16.
 */
public class SigMeshPresenter implements IMeshPresenter {
    private static SigMeshBean mSigMeshBean;
    private static final String TAG = "SigMeshPresenter";

    protected Activity mContext;
    private IMeshDemoView iMeshDemoView;
    private CheckPermissionUtils checkPermission;
    private UUID[] MESH_PROVISIONING_UUID = {UUID.fromString("00001827-0000-1000-8000-00805f9b34fb")};

    public static final int MESH_BLUETOOTH_OPEN = 1;
    public static final int MESH_BLUETOOTH_CLOSE = 2;
    public static final int MESH_BLUETOOTH_NULL = -1;

    public static final int REQUEST_OPEN_BLE = 1234;
    public static final int REQUEST_CODE_FOR_PERMISSION = 222;
    public static final int GPS_REQUEST_CODE = 223;

    public SigMeshPresenter(Activity context, IMeshDemoView meshDemoView) {
        this.mContext = context;
        this.iMeshDemoView = meshDemoView;
        checkPermission = new CheckPermissionUtils(context);
    }
    @Override
    public void showCreateDialog(final long homeId) {
        createMesh(homeId);
    }

    @Override
    public String getMeshName() {
        return mSigMeshBean==null?"":mSigMeshBean.getMeshId();
    }

    private void doMeshConfig(ArrayList<SigMeshSearchDeviceBean> searchList) {
        Intent intent = new Intent(mContext, ConfigTipActivity.class);
        Bundle bu = new Bundle();
        bu.putParcelableArrayList("extra_found_device", searchList);
        bu.putInt("extra_config_type", 1);
        bu.putString("extra_mesh_id", getMeshId());
        intent.putExtras(bu);
        mContext.startActivity(intent);
    }

    private void doMeshWifiConfig(long homeId, List<?> searchList, DialogPlus dialogPlus) {
        Toast.makeText(mContext,"SigMesh网关配网暂未提供",Toast.LENGTH_SHORT).show();
    }
    @Override
    public String getMeshId() {
        return mSigMeshBean.getMeshId();
    }

    private void createMesh(long homeId) {
        TuyaHomeSdk.newHomeInstance(homeId).createSigMesh(new ITuyaResultCallback<SigMeshBean>() {
            @Override
            public void onSuccess(SigMeshBean result) {
                Toast.makeText(mContext, "创建mesh成功", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(mContext, "创建mesh失败  " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void showMeshList(long homeId, final int type) {
        List<String> MeshNameList = new ArrayList<>();

        ITuyaHome tuyaHome = TuyaHomeSdk.newHomeInstance(homeId);
        if (tuyaHome.getHomeBean() == null) {
            Toast.makeText(mContext, "尚未创建home", Toast.LENGTH_LONG).show();
            return;
        }
        final List<SigMeshBean> meshList = TuyaHomeSdk.getSigMeshInstance().getSigMeshList();
        if (meshList.isEmpty()) {
            Toast.makeText(mContext, "mesh 列表为空  请先创建mesh", Toast.LENGTH_LONG).show();
            return;
        }
        for (SigMeshBean bean : meshList) {
            MeshNameList.add(bean.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, MeshNameList);
        final DialogPlus dialogPlus = DialogPlus.newDialog(mContext)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                        if (type == 0) {
                            initMesh(meshList.get(position));
                        } else if (type == 1) {
                            removeMesh(meshList.get(position));
                        }
                        dialog.dismiss();
                    }
                })
                .setContentHolder(new ListHolder())
                .setAdapter(adapter)
                .setExpanded(false)
                .setMargin(30, 30, 30, 30)
                .setGravity(Gravity.CENTER)
                .create();
        dialogPlus.show();
    }

    private void initMesh(SigMeshBean sigMeshBean) {
        if (sigMeshBean != null && !TextUtils.isEmpty(sigMeshBean.getMeshId())) {
            TuyaHomeSdk.getTuyaSigMeshClient().initMesh(sigMeshBean.getMeshId(),true);
            mSigMeshBean = sigMeshBean;
            iMeshDemoView.updateTip();
        }
    }


    private void removeMesh(final SigMeshBean sigMeshBean) {
        if (sigMeshBean != null && !TextUtils.isEmpty(sigMeshBean.getMeshId())) {
            TuyaHomeSdk.newSigMeshDeviceInstance(sigMeshBean.getMeshId()).removeMesh(new IResultCallback() {
                @Override
                public void onError(String errorCode, String errorMsg) {
                    Toast.makeText(mContext, "删除mesh失败  " + errorMsg, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess() {
                    Toast.makeText(mContext, "删除mesh成功", Toast.LENGTH_LONG).show();
                    if (mSigMeshBean != null && sigMeshBean.getMeshId().equals(mSigMeshBean.getMeshId())) {
                        mSigMeshBean = null;
                        iMeshDemoView.updateTip();
                    }
                }
            });
        }
    }

    public void destroyMesh() {
        TuyaHomeSdk.getTuyaSigMeshClient().destroyMesh(mSigMeshBean.getMeshId());
        mSigMeshBean = null;
        iMeshDemoView.updateTip();
    }
    @Override
    public void check() {
        if (!checkPermission()){
            return;
        }
        scan();
    }

    private int checkBluetooth() {
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

    private void scan() {
        final List<String> searchNameList = new ArrayList<>();

        final ArrayAdapter adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, searchNameList);
        final ArrayList<SearchDeviceBean> searchList = new ArrayList();
        SearchBuilder searchBuilder = new SearchBuilder()//两种协议不同，需要按需求指定参数
                .setServiceUUIDs(MESH_PROVISIONING_UUID)       //SigMesh为固定值，BlueMesh为空
                .setTimeOut(100)        //扫描时长 单位秒
                .setTuyaBlueMeshSearchListener(new ITuyaBlueMeshSearchListener() {
                    @Override
                    public void onSearched(final SearchDeviceBean searchDeviceBean) {
                        Observable.just(1)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<Integer>() {
                                    @Override
                                    public void accept(Integer integer) throws Exception {
                                        searchList.add(searchDeviceBean);
                                        searchNameList.add((searchNameList.size() + 1) + " : " + searchDeviceBean.getMeshName() + "   " + searchDeviceBean.getMacAdress());
                                        adapter.notifyDataSetChanged();
                                    }
                                });

                    }

                    @Override
                    public void onSearchFinish() {
                        Toast.makeText(mContext, "扫描结束", Toast.LENGTH_SHORT).show();

                    }
                }).build();
        final ITuyaBlueMeshSearch mMeshSearch = TuyaHomeSdk.getTuyaBlueMeshConfig().newTuyaBlueMeshSearch(searchBuilder);

        //开启扫描
        mMeshSearch.startSearch();

        View headView = LayoutInflater.from(mContext).inflate(R.layout.head_tip_layout, null);
        ((TextView) headView.findViewById(R.id.tv_head_title)).setText("扫描结果");
        final DialogPlus dialogPlus = DialogPlus.newDialog(mContext)
                .setHeader(headView)
                .setContentHolder(new ListHolder())
                .setAdapter(adapter)
                .setExpanded(false)
                .setContentHeight(600)
                .setMargin(30, 30, 30, 30)
                .setGravity(Gravity.CENTER)
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogPlus dialog) {
                        mMeshSearch.stopSearch();
                    }
                })
                .create();

        dialogPlus.show();
    }


    @Override
    public void showSearchList(final long homeId) {
        if (!checkPermission()){
            return;
        }
        final List<String> searchNameList = new ArrayList<>();
        final ArrayList<SigMeshSearchDeviceBean> searchList = new ArrayList<>();
        final ArrayAdapter adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, searchNameList);

        SearchBuilder searchBuilder = new SearchBuilder()   //两种协议不同，需要按需求指定参数
                .setMeshName(null)        //BLueMesh要扫描设备的名称（默认会是out_of_mesh，设备处于配网状态下的名称），SigMesh为空
                .setServiceUUIDs(MESH_PROVISIONING_UUID)
                .setTimeOut(100)        //扫描时长 单位秒
                .setTuyaBlueMeshSearchListener(new ITuyaBlueMeshSearchListener() {
                    @Override
                    public void onSearched(final SearchDeviceBean searchDeviceBean) {
                        Observable.just(1)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<Integer>() {
                                    @Override
                                    public void accept(Integer integer) throws Exception {
                                        searchList.add((SigMeshSearchDeviceBean) searchDeviceBean);
                                        searchNameList.add((searchNameList.size() + 1) + " : " + searchDeviceBean.getMeshName() + "   " + searchDeviceBean.getMacAdress() + "  " + String.format("%0" + 4 + "d", searchDeviceBean.getVendorId()));
                                        adapter.notifyDataSetChanged();
                                    }
                                });

                    }
                    @Override
                    public void onSearchFinish() {
                        Toast.makeText(mContext, "扫描结束", Toast.LENGTH_SHORT).show();
                    }
                }).build();
        final ITuyaBlueMeshSearch mMeshSearch = TuyaHomeSdk.getTuyaBlueMeshConfig().newTuyaBlueMeshSearch(searchBuilder);
        mMeshSearch.startSearch();
        View headView = LayoutInflater.from(mContext).inflate(R.layout.head_tip_layout, null);
        ((TextView) headView.findViewById(R.id.tv_head_title)).setText("扫描结果");


        View footView = LayoutInflater.from(mContext).inflate(R.layout.sig_foot_tip_layout, null);
        Button configBtn = footView.findViewById(R.id.bt_confim1);
        configBtn.setText("子设备配网");


        final DialogPlus dialogPlus = DialogPlus.newDialog(mContext)
                .setHeader(headView)
                .setContentHolder(new ListHolder())
                .setFooter(footView)
                .setAdapter(adapter)
                .setExpanded(false)
                .setContentHeight(700)
                .setMargin(30, 30, 30, 30)
                .setGravity(Gravity.CENTER)
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogPlus dialog) {
                        mMeshSearch.stopSearch();
                    }
                })
                .create();

        configBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSigMeshBean==null) {
                    Toast.makeText(mContext, "请先初始化mesh", Toast.LENGTH_SHORT).show();
                } else if (searchList.isEmpty()) {
                    Toast.makeText(mContext, "未扫描到待配网设备", Toast.LENGTH_SHORT).show();
                } else {
                    //子设备入网
                    doMeshConfig(searchList);
                    dialogPlus.dismiss();
                }
            }
        });
        dialogPlus.show();
    }
    private boolean checkPermission(){
        //检查 蓝牙和位置权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2
                || !mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //不支持ble
            Toast.makeText(mContext, "This version not support bluetooth", Toast.LENGTH_LONG).show();
            return false;
        }

        if (checkBluetooth() == MESH_BLUETOOTH_CLOSE) {
            //开启蓝牙

            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mContext.startActivityForResult(enableBtIntent, REQUEST_OPEN_BLE);
            return false;
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

                    DialogUtils.simpleConfirmDialog(mContext, "Tip",
                            "请打开 GPS(定位)服务", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == DialogInterface.BUTTON_POSITIVE) {
                                        Intent intent = new Intent(
                                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        ((Activity) mContext).startActivityForResult(intent, GPS_REQUEST_CODE); // 设置完成后返回到原来的界面
                                    }
                                }
                            });
                    return false;

                } else {
                    return true;
                }
            } else {

            }
        }
        return false;
    }
}
