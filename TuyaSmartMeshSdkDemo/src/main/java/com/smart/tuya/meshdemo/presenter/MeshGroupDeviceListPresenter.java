package com.smart.tuya.meshdemo.presenter;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Message;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.smart.tuya.meshdemo.R;
import com.smart.tuya.meshdemo.activity.MeshGroupDevListActivity;
import com.smart.tuya.meshdemo.adapter.MeshGroupAddFailListAdapter;
import com.smart.tuya.meshdemo.callback.IMeshOperateGroupListener;
import com.smart.tuya.meshdemo.model.MeshGroupDeviceListModel;
import com.smart.tuya.meshdemo.utils.DialogUtils;
import com.smart.tuya.meshdemo.view.IMeshGroupDeviceListView;
import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshDevice;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.mvp.presenter.BasePresenter;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaGroup;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.smart.tuya.meshdemo.adapter.MeshGroupDevListAdapter.InnerViewHolder.ADD_ACTION;

/**
 * Created by zhusg on 2018/8/27.
 */

public class MeshGroupDeviceListPresenter extends BasePresenter {
    private static final String TAG = "GroupDeviceListPresenter";

    private MeshGroupDevListActivity mActivity;
    private IMeshGroupDeviceListView mView;
    private ITuyaGroup mGroup;
    private MeshGroupDeviceListModel mMeshGroupDeviceListModel;
    private ITuyaBlueMeshDevice mTuyaBlueMeshDevice;


    //暂时写死
    private String mVendorId;
    private GroupBean groupBean;
    private String mMeshId;
    private long mGroupId;

    private ArrayList<DeviceBean> mFoundDeviceBean = new ArrayList<>();
    private ArrayList<DeviceBean> mAddDeviceBean = new ArrayList<>();


    private ArrayList<DeviceBean> mOldFoundDeviceBean = new ArrayList<>();
    private ArrayList<DeviceBean> mOldAddDeviceBean = new ArrayList<>();




    public MeshGroupDeviceListPresenter(MeshGroupDevListActivity activity, IMeshGroupDeviceListView view) {
        mActivity = activity;
        mView = view;
        mMeshGroupDeviceListModel = new MeshGroupDeviceListModel(activity);
        getData();
    }

    public void getData() {
        mGroupId = mActivity.getIntent().getLongExtra(mActivity.EXTRA_GROUP_ID, -1);
        mMeshId = mActivity.getIntent().getStringExtra(mActivity.EXTRA_MESH_ID);
        groupBean = TuyaHomeSdk.getDataInstance().getGroupBean(mGroupId);
        mVendorId = groupBean.getCategory();
        mTuyaBlueMeshDevice=TuyaHomeSdk.newBlueMeshDeviceInstance(mMeshId);

        //mesh设备
        groupBean = TuyaHomeSdk.getDataInstance().getGroupBean(mGroupId);

        if (groupBean != null) {
            mGroup = TuyaHomeSdk.newBlueMeshGroupInstance(mGroupId);
            queryDevicesByGroupId();
        } else {
            Toast.makeText(mActivity, "group init fail", Toast.LENGTH_SHORT).show();
        }

    }


    private void queryDevicesByGroupId() {
        mFoundDeviceBean.clear();
        mAddDeviceBean.clear();
        List<DeviceBean> devList = TuyaHomeSdk.getDataInstance().getMeshDeviceList(mMeshId);
        for (DeviceBean bean : devList) {
            //TODO 不同大小类的设备  部分命令不通用 在这里可以过滤出相同的大小类的设备
//            if (!bean.getCategory().equals(mVendorId)) {
//                continue;
//            }

            mFoundDeviceBean.add(bean);
            L.d("huohuo", "bean--->  nodeid:" + bean.getNodeId() + "  productId:" + bean.getProductId() + "   devId:" + bean.getDevId());
        }

        List<DeviceBean> deviceBeanList = TuyaHomeSdk.getDataInstance().getGroupDeviceList(mGroupId);
        if (deviceBeanList != null && deviceBeanList.size() > 0) {
            List<DeviceBean> mTempList = new ArrayList<>();
            mTempList.addAll(mFoundDeviceBean);
            for (DeviceBean groupBean : mTempList) {
                for (DeviceBean subBean : deviceBeanList) {
                    if (subBean.getDevId().equals(groupBean.getDevId())) {
                        mAddDeviceBean.add(groupBean);
                        mFoundDeviceBean.remove(groupBean);
                    }
                }
            }
            mOldAddDeviceBean.addAll(mAddDeviceBean);
            mOldFoundDeviceBean.addAll(mFoundDeviceBean);
        }
        updateDeviceList();
    }

    private void updateDeviceList() {
        mView.updateAddDeviceList(mAddDeviceBean, mFoundDeviceBean);
    }

    public void onClickSelect(int actionType, DeviceBean bean) {
        //&& !(mTuyaBlueMesh.isCloudOnline() && bean.getModuleMap().getBluetooth().getIsOnline())
        if (!bean.getIsOnline()) {
            Toast.makeText(mActivity, "设备离线，无法添加", Toast.LENGTH_SHORT).show();
            return;
        }

        if (actionType == ADD_ACTION) {
            removeDeviceToMeshGroup(bean);
        } else {
            //添加bean到group
            addDeviceToMeshGroup(bean);
        }
    }

    public void removeDeviceToMeshGroup(final DeviceBean bean) {

        mAddDeviceBean.remove(bean);
        mFoundDeviceBean.add(bean);
        updateDeviceList();

    }


    public void addDeviceToMeshGroup(final DeviceBean groupDevice) {
        mFoundDeviceBean.remove(groupDevice);
        mAddDeviceBean.add(groupDevice);
        updateDeviceList();
    }

    public void doConfirm() {
        if (getAddDevice().isEmpty() && getDeleteDevice().isEmpty()) {
            Toast.makeText(mActivity,"无修改操作",Toast.LENGTH_SHORT).show();
        } else {
            operateDevice();
        }
    }

    public ArrayList<DeviceBean> getAddDevice() {
        ArrayList<DeviceBean> addDeviceTemp = new ArrayList<>();
        //找到需要添加的设备
        for (DeviceBean addBean : mAddDeviceBean) {
            if (!mOldAddDeviceBean.contains(addBean)) {
                addDeviceTemp.add(addBean);
            }
        }

        L.e(TAG, "getAddDevice:" + addDeviceTemp.size());
        return addDeviceTemp;
    }

    public ArrayList<DeviceBean> getDeleteDevice() {
        ArrayList<DeviceBean> deleteDeviceTemp = new ArrayList<>();
        //找到需要添加的设备
        for (DeviceBean deleteBean : mOldAddDeviceBean) {
            if (!mAddDeviceBean.contains(deleteBean)) {
                deleteDeviceTemp.add(deleteBean);
            }
        }

        L.e(TAG, "getDeleteDevice:" + deleteDeviceTemp.size());
        return deleteDeviceTemp;
    }

    public void operateDevice() {
        //找出要操作的数据
        final ArrayList<DeviceBean> addBeans = getAddDevice();
        final ArrayList<DeviceBean> removeBeans = getDeleteDevice();
        final int subCount = addBeans.size() + removeBeans.size();
        final String msg = "%s/" + subCount + "  修改生效中..请勿关闭应用";

        final Dialog dialog = new Dialog(mActivity);
        dialog.setContentView(R.layout.bluemesh_dialog_progress);

        final TextView tvMsg = (TextView) dialog.findViewById(R.id.progress_dialog_message);
        tvMsg.setText(String.format(msg, "0"));

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);


        dialog.show();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMeshGroupDeviceListModel.operateDevice(mGroup, addBeans, removeBeans, mVendorId, new IMeshOperateGroupListener() {
                    @Override
                    public void operateSuccess(DeviceBean bean, final int index) {
                        L.d(TAG, "operateSuccess bean:" + bean.getName() + "success " + index);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                tvMsg.setText(String.format(msg, index + ""));

                            }
                        });

                    }

                    @Override
                    public void operateFinish(ArrayList<DeviceBean> failList) {
                        if (failList == null || failList.isEmpty()) {
                            Toast.makeText(mActivity,"群组操作成功",Toast.LENGTH_SHORT).show();
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mView.finishActivity();
                                }
                            },100);
                        } else {
                            showOperateFaileDialog(failList, addBeans.size() + removeBeans.size());
                        }
                        if (failList != null) {
                            for (DeviceBean b : failList) {
                                L.e(TAG, "operateFinish fail:" + b.getName());
                            }
                        }
                        dialog.cancel();
                    }

                    @Override
                    public void operateFail(DeviceBean bean, final int index) {
                        L.d(TAG, "operateFail bean:" + bean.getName() + "success " + index);

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                tvMsg.setText(String.format(msg, index + ""));
                            }
                        });

                    }
                });
            }
        }, 1000);

    }

    public void showOperateFaileDialog(final ArrayList<DeviceBean> failList, final int subOperateCount) {
        MeshGroupAddFailListAdapter adapter = new MeshGroupAddFailListAdapter(mActivity);
        adapter.setData(failList);

        DialogUtils.customerListDialogTitleCenter(mActivity,
                "以下设备执行失败",
                adapter, null);
    }


    public void doOpen() {
        LinkedHashMap<String, Object> dpMap = new LinkedHashMap<>();
        dpMap.put("1", true);
        JSONObject jsonObject = new JSONObject(dpMap);
        String command=jsonObject.toJSONString();
        mTuyaBlueMeshDevice.multicastDps(groupBean.getLocalId(), groupBean.getCategory(), command, new IResultCallback() {
            @Override
            public void onError(String s, String s1) {
                L.e(TAG, "rn send onError :" + s + "  " + s1);
                Toast.makeText(mActivity,"send onError  "+s1,Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onSuccess() {
                L.d(TAG, "rn send onSuccess ");
                Toast.makeText(mActivity,"send onSuccess",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void doClose() {
        LinkedHashMap<String, Object> dpMap = new LinkedHashMap<>();
        dpMap.put("1", false);
        JSONObject jsonObject = new JSONObject(dpMap);
        String command=jsonObject.toJSONString();
        mTuyaBlueMeshDevice.multicastDps(groupBean.getLocalId(), groupBean.getCategory(), command, new IResultCallback() {
            @Override
            public void onError(String s, String s1) {
                L.e(TAG, "rn send onError :" + s + "  " + s1);
                Toast.makeText(mActivity,"send onError  "+s1,Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onSuccess() {
                L.d(TAG, "rn send onSuccess ");
                Toast.makeText(mActivity,"send onSuccess",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
