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
import com.smart.tuya.meshdemo.R;
import com.smart.tuya.meshdemo.activity.ConfigTipActivity;
import com.smart.tuya.meshdemo.utils.BluetoothUtils;
import com.smart.tuya.meshdemo.utils.CheckPermissionUtils;
import com.smart.tuya.meshdemo.utils.DialogUtils;
import com.smart.tuya.meshdemo.view.IMeshDemoView;
import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshSearch;
import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshSearchListener;
import com.tuya.smart.android.blemesh.bean.SearchDeviceBean;
import com.tuya.smart.android.blemesh.builder.SearchBuilder;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.api.ITuyaHome;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.bean.BlueMeshBean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;


/**
 * Created by zhusg on 2018/5/26.
 */

public class MeshPresenter {
    private static final String TAG = "MeshPresenter";
    public Activity mContext;
    private IMeshDemoView iMeshDemoView;
    private static BlueMeshBean mBlueMeshBean;
    public CheckPermissionUtils checkPermission;


    public static final int REQUEST_OPEN_BLE = 1234;
    public static final int REQUEST_CODE_FOR_PERMISSION = 222;
    public static final int GPS_REQUEST_CODE = 223;

    public MeshPresenter(Activity context, IMeshDemoView meshDemoView) {
        this.mContext = context;
        this.iMeshDemoView = meshDemoView;
        checkPermission = new CheckPermissionUtils(context);

    }


    public void showCreateDialog(final long homeId) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_input, null);
        final EditText editText1 = view.findViewById(R.id.ed_input1);
        final EditText editText2 = view.findViewById(R.id.ed_input2);
        editText1.setHint("meshName 16字节以内");
        editText2.setVisibility(View.GONE);
        final DialogPlus dialogPlus = DialogPlus.newDialog(mContext)
                .setContentHolder(new ViewHolder(view))
                .setExpanded(false)
                .setPadding(60, 50, 60, 80)
                .setGravity(Gravity.CENTER)
                .create();

        view.findViewById(R.id.bt_confim).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String meshName = editText1.getText().toString();
                if (TextUtils.isEmpty(meshName)) {
                    Toast.makeText(mContext, "mesh不能为空", Toast.LENGTH_SHORT).show();
                } else if (meshName.getBytes().length > 16) {
                    Toast.makeText(mContext, "meshName 不能超过16字节", Toast.LENGTH_SHORT).show();

                } else {
                    createMesh(homeId, meshName);
                    dialogPlus.dismiss();
                }

            }
        });
        dialogPlus.show();
    }

    private void createMesh(long homeId, String meshName) {
        TuyaHomeSdk.newHomeInstance(homeId).createBlueMesh(meshName, new ITuyaResultCallback<BlueMeshBean>() {
            @Override
            public void onError(String errorCode, String errorMsg) {
                Toast.makeText(mContext, "创建mesh失败  " + errorMsg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(BlueMeshBean blueMeshBean) {
                Toast.makeText(mContext, "创建mesh成功", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void showMeshList(long homeId, final int type) {
        List<String> MeshNameList = new ArrayList<>();
        ITuyaHome tuyaHome = TuyaHomeSdk.newHomeInstance(homeId);
        if (tuyaHome.getHomeBean() == null) {
            Toast.makeText(mContext, "尚未创建home", Toast.LENGTH_LONG).show();
            return;
        }
        final List<BlueMeshBean> meshList = tuyaHome.getHomeBean().getMeshList();
        if (meshList.isEmpty()) {
            Toast.makeText(mContext, "mesh 列表为空  请先创建mesh", Toast.LENGTH_LONG).show();
            return;
        }
        for (BlueMeshBean bean : meshList) {
            MeshNameList.add(bean.getName());
        }


        ArrayAdapter adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, MeshNameList);
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

    public void initMesh(BlueMeshBean blueMeshBean) {
        if (blueMeshBean != null && !TextUtils.isEmpty(blueMeshBean.getMeshId())) {
            TuyaHomeSdk.getTuyaBlueMeshClient().initMesh(blueMeshBean.getMeshId());
            mBlueMeshBean = blueMeshBean;
            iMeshDemoView.updateTip();
        }
    }


    public void removeMesh(final BlueMeshBean blueMeshBean) {
        if (blueMeshBean != null && !TextUtils.isEmpty(blueMeshBean.getMeshId())) {
            TuyaHomeSdk.newBlueMeshDeviceInstance(blueMeshBean.getMeshId()).removeMesh(new IResultCallback() {
                @Override
                public void onError(String errorCode, String errorMsg) {
                    Toast.makeText(mContext, "删除mesh失败  " + errorMsg, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess() {
                    Toast.makeText(mContext, "删除mesh成功", Toast.LENGTH_LONG).show();
                    if (mBlueMeshBean != null && blueMeshBean.getMeshId().equals(mBlueMeshBean.getMeshId())) {
                        mBlueMeshBean = null;
                        iMeshDemoView.updateTip();

                    }
                }
            });
        }
    }


    public static BlueMeshBean getCurrentMeshBean() {
        return mBlueMeshBean;
    }

    public void destroyMesh() {
        TuyaHomeSdk.getTuyaBlueMeshClient().destroyMesh();
        mBlueMeshBean = null;
        iMeshDemoView.updateTip();

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
            //开启蓝牙

            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mContext.startActivityForResult(enableBtIntent, REQUEST_OPEN_BLE);
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

                } else {
                    //扫描
                    scan();
                    return true;
                }
            } else {
                Toast.makeText(mContext, "请先开启定位权限", Toast.LENGTH_LONG).show();
            }
        }

        return false;

    }


    private void scan() {
        final List<String> searchNameList = new ArrayList<>();

        final ArrayAdapter adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, searchNameList);
        final ArrayList<SearchDeviceBean> searchList = new ArrayList();


        SearchBuilder searchBuilder = new SearchBuilder().setMeshName("out_of_mesh")        //要扫描设备的名称（默认会是out_of_mesh，设备处于配网状态下的名称）
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

    public void showSearchList(final long homeId) {
        final List<String> searchNameList = new ArrayList<>();
        final List<SearchDeviceBean> searchList = new ArrayList<>();

        final ArrayAdapter adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, searchNameList);

        SearchBuilder searchBuilder = new SearchBuilder()
                .setMeshName("out_of_mesh")        //要扫描设备的名称（默认会是out_of_mesh，设备处于配网状态下的名称）
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


        View footView = LayoutInflater.from(mContext).inflate(R.layout.foot_tip_layout, null);
        Button wifiConfigBtn = footView.findViewById(R.id.bt_confim1);
        Button configBtn = footView.findViewById(R.id.bt_confim2);
        wifiConfigBtn.setText("网关设备配网");
        configBtn.setText("普通子设备配网");


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
                if (getCurrentMeshBean() == null) {
                    Toast.makeText(mContext, "请先初始化mesh", Toast.LENGTH_SHORT).show();
                } else if (searchList.isEmpty()) {
                    Toast.makeText(mContext, "未扫描到待配网设备", Toast.LENGTH_SHORT).show();
                } else {
                    //普通设备入网
                    doMeshConfig(searchList);
                    dialogPlus.dismiss();
                }

            }
        });

        wifiConfigBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mesh网关入网
                if (getCurrentMeshBean() == null) {
                    Toast.makeText(mContext, "请先初始化mesh", Toast.LENGTH_SHORT).show();
                } else if (searchList.isEmpty()) {
                    Toast.makeText(mContext, "未扫描到待配网设备", Toast.LENGTH_SHORT).show();
                } else {
                    doMeshWifiConfig(homeId, searchList, dialogPlus);

                }
            }
        });

        dialogPlus.show();


    }

    private void showWifiDialog(final long homeId, final ArrayList<SearchDeviceBean> searchBeanList) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_input, null);
        final EditText editText1 = view.findViewById(R.id.ed_input1);
        final EditText editText2 = view.findViewById(R.id.ed_input2);
        editText1.setHint("wifi name (2.4G)");
        editText2.setHint("wifi password");
        editText1.setText("Tuya-Test");
        editText2.setText("Tuya.140616");
        final DialogPlus dialogPlus = DialogPlus.newDialog(mContext)
                .setContentHolder(new ViewHolder(view))
                .setExpanded(false)
                .setPadding(60, 50, 60, 80)
                .setGravity(Gravity.CENTER)
                .create();

        view.findViewById(R.id.bt_confim).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String wifiName = editText1.getText().toString();
                String wifiPwd = editText2.getText().toString();

                if (TextUtils.isEmpty(wifiName) || TextUtils.isEmpty(wifiPwd)) {
                    Toast.makeText(mContext, "mesh不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(mContext, ConfigTipActivity.class);
                    Bundle bu = new Bundle();
                    bu.putParcelableArrayList("extra_found_device", searchBeanList);
                    bu.putString("extra_mesh_id", getCurrentMeshBean().getMeshId());
                    bu.putString("extra_wifi_ssid", wifiName);
                    bu.putString("extra_wifi_password", wifiPwd);
                    bu.putInt("extra_config_type", 2);
                    bu.putLong("extra_home_id", homeId);
                    intent.putExtras(bu);
                    mContext.startActivity(intent);
                    dialogPlus.dismiss();
                }

            }
        });
        dialogPlus.show();
    }

    //普通设备入网
    private void doMeshConfig(List<SearchDeviceBean> searchList) {
        ArrayList<SearchDeviceBean> searchBeanList = new ArrayList<>();

        for (SearchDeviceBean bean : searchList) {
            String venderId = Integer.toHexString(bean.getVendorId()).toUpperCase();
            Log.d(TAG, "bean.getVendorId():" + venderId);
            if (!venderId.endsWith("08")) {
                //单mesh
                searchBeanList.add(bean);
            }
        }

        if (searchBeanList.isEmpty()) {
            Toast.makeText(mContext, "扫描列表中 无普通mesh设备", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(mContext, ConfigTipActivity.class);
            Bundle bu = new Bundle();
            bu.putParcelableArrayList("extra_found_device", searchBeanList);
            bu.putInt("extra_config_type", 1);
            bu.putString("extra_mesh_id", getCurrentMeshBean().getMeshId());
            intent.putExtras(bu);
            mContext.startActivity(intent);
        }
    }


    //mesh网关入网
    private void doMeshWifiConfig(final long homeId, List<SearchDeviceBean> searchList, DialogPlus dialogPlus) {
        final ArrayList<SearchDeviceBean> searchBeanList = new ArrayList<>();

        for (SearchDeviceBean bean : searchList) {
            String venderId = Integer.toHexString(bean.getVendorId()).toUpperCase();
            Log.d(TAG, "VendorId:" + venderId);
            if (venderId.endsWith("08")) {
                //网关的大类是08  网关设备的话 要一个一个配网
                searchBeanList.clear();
                searchBeanList.add(bean);
                break;
            }
        }

        if (searchBeanList.isEmpty()) {
            Toast.makeText(mContext, "扫描列表中 无mesh网关设备", Toast.LENGTH_SHORT).show();
        } else {
            dialogPlus.dismiss();
            Observable.timer(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            showWifiDialog(homeId, searchBeanList);

                        }
                    });

        }

    }


}
