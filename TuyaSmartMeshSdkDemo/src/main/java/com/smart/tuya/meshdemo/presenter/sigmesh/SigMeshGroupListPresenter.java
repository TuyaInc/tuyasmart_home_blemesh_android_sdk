package com.smart.tuya.meshdemo.presenter.sigmesh;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.smart.tuya.meshdemo.R;
import com.smart.tuya.meshdemo.callback.IGetEnableGroupIdCallback;
import com.smart.tuya.meshdemo.presenter.IMeshGroupListPresenter;
import com.smart.tuya.meshdemo.utils.DialogUtils;
import com.smart.tuya.meshdemo.view.IMeshGroupListView;
import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshDevice;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.api.ITuyaHome;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDataCallback;
import com.tuya.smart.sdk.api.ITuyaGroup;
import com.tuya.smart.sdk.api.bluemesh.IAddGroupCallback;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.sdk.bean.SigMeshBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author aze
 * @Des
 * @date 2019-07-18.
 */

public class SigMeshGroupListPresenter implements IMeshGroupListPresenter {
    private static final String TAG = "SigMeshListPresenter";
    public Activity mContext;
    private long homeId;
    private String meshId;
    private SigMeshBean meshBean;

    private ITuyaHome mTuyaHome;
    private IMeshGroupListView mView;


    public SigMeshGroupListPresenter(Activity context, IMeshGroupListView view, long homeId, String meshId) {
        this.mContext = context;
        this.homeId = homeId;
        mTuyaHome = TuyaHomeSdk.newHomeInstance(homeId);
        this.mView = view;
        this.meshId = meshId;
        meshBean = TuyaHomeSdk.getSigMeshInstance().getSigMeshBean(meshId);

    }

    @Override
    public void getDataFromServer() {
        mTuyaHome.getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                mView.updateUi(TuyaHomeSdk.getDataInstance().getMeshGroupList(meshId));
                mView.loadFinish();
            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                Toast.makeText(mContext, errorCode + " " + errorMsg, Toast.LENGTH_LONG).show();
                mView.loadFinish();
            }
        });
    }

    @Override
    public void showCreateDialog() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_input, null);
        final EditText editText1 = view.findViewById(R.id.ed_input1);
        final EditText editText2 = view.findViewById(R.id.ed_input2);
        editText1.setHint("群组");
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
                String groupName = editText1.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(mContext, "群组名不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (groupName.length() > 25) {
                    Toast.makeText(mContext, "群组名过长", Toast.LENGTH_SHORT).show();
                    return;
                }
                createMeshGroup(groupName);
                dialogPlus.dismiss();


            }
        });
        dialogPlus.show();
    }

    private void createMeshGroup(final String groupName) {
        //检查群组是否已满

        final ITuyaBlueMeshDevice mITuyaBlueMesh = TuyaHomeSdk.newSigMeshDeviceInstance(meshId);
        //创建五路灯群组
        getEnableGroupId(meshId, new IGetEnableGroupIdCallback() {
            @Override
            public void onSuccess(String enableGroupId) {
                mITuyaBlueMesh.addGroup(groupName, "1510", enableGroupId, new IAddGroupCallback() {
                    @Override
                    public void onSuccess(long groupId) {
                        Toast.makeText(mContext, "创建群组成功", Toast.LENGTH_SHORT).show();
                        getDataFromServer();
                    }

                    @Override
                    public void onError(String errorCode, String errorMsg) {
                        Toast.makeText(mContext, "创建群组失败", Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(mContext, "获取群组localId失败", Toast.LENGTH_SHORT).show();
            }
        });




    }

    private void getEnableGroupId(String meshId, final IGetEnableGroupIdCallback callback) {
        Map<String, Object> postData = new HashMap<>();
        postData.put("meshId", meshId);
        postData.put("type", 1);
        // 可以通过 tuya.m.device.ble.mesh.local.id.alloc 获取指定mesh下可用的groupId
        // 云端分配 每次分配都会加1  保证groupId 的唯一性
        // 由于云端分配是从16进制 8000开始的  但是sigmesh 群组范围是从  C000 - FEFF  所以结果要加上 4000
        TuyaHomeSdk.getRequestInstance().requestWithApiName("tuya.m.device.ble.mesh.local.id.alloc", "2.0", postData, String.class, new ITuyaDataCallback<String>() {
            @Override
            public void onSuccess(String localId) {
                int newLocalId = Integer.parseInt(localId, 16)+0x4000;
                callback.onSuccess(String.format("%04x", newLocalId));
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                callback.onError(errorCode,errorMessage);

            }
        });

    }

    @Override
    public void showDismissDialog(final long groupId) {
        GroupBean groupBean = TuyaHomeSdk.getDataInstance().getGroupBean(groupId);
        if (groupBean != null) {
            DialogUtils.simpleConfirmDialog(mContext, "Tip", "是否删除群组 " + groupBean.getName(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == -1) {
                        ITuyaGroup mGroup = TuyaHomeSdk.newSigMeshGroupInstance(groupId);
                        mGroup.dismissGroup(new IResultCallback() {
                            @Override
                            public void onError(String code, String errorMsg) {
                                Toast.makeText(mContext, "解散群组失败 " + errorMsg, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onSuccess() {
                                mView.updateUi(TuyaHomeSdk.getDataInstance().getMeshGroupList(meshId));
                                Toast.makeText(mContext, "解散群组成功 ", Toast.LENGTH_LONG).show();
                            }
                        });


                    }
                }
            });
        } else {
            Toast.makeText(mContext, "device not exist", Toast.LENGTH_LONG).show();
        }
    }
}
