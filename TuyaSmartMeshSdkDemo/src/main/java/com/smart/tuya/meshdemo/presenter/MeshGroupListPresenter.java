package com.smart.tuya.meshdemo.presenter;

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
import com.smart.tuya.meshdemo.utils.DialogUtils;
import com.smart.tuya.meshdemo.view.IMeshGroupListView;
import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshDevice;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.api.ITuyaHome;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaGroup;
import com.tuya.smart.sdk.api.bluemesh.IAddGroupCallback;
import com.tuya.smart.sdk.bean.BlueMeshBean;
import com.tuya.smart.sdk.bean.GroupBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhusg on 2018/8/22.
 */

public class MeshGroupListPresenter {
    private static final String TAG = "MeshDeviceListPresenter";
    public Activity mContext;
    private long homeId;
    private String meshId;
    private BlueMeshBean meshBean;

    private ITuyaHome mTuyaHome;
    private IMeshGroupListView mView;


    public MeshGroupListPresenter(Activity context, IMeshGroupListView view, long homeId, String meshId) {
        this.mContext = context;
        this.homeId = homeId;
        mTuyaHome = TuyaHomeSdk.newHomeInstance(homeId);
        this.mView = view;
        this.meshId = meshId;
        meshBean = TuyaHomeSdk.getMeshInstance().getBlueMeshBean(meshId);

    }

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
                    Toast.makeText(mContext,"群组名不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (groupName.length() > 25) {
                    Toast.makeText(mContext,"群组名过长",Toast.LENGTH_SHORT).show();
                    return;
                }
                createMeshGroup(groupName);
                dialogPlus.dismiss();


            }
        });
        dialogPlus.show();
    }

    public void createMeshGroup(String groupName) {
        //检查群组是否已满
        String enableLocalId = getEnableGroupId(meshId);
        if (TextUtils.isEmpty(enableLocalId)) {
            Toast.makeText(mContext,"群组已满",Toast.LENGTH_SHORT).show();
        }else{
            ITuyaBlueMeshDevice mITuyaBlueMesh = TuyaHomeSdk.newBlueMeshDeviceInstance(meshId);
            //跨小类创建群组
            mITuyaBlueMesh.addGroup(groupName, "FF01", enableLocalId, new IAddGroupCallback() {
                @Override
                public void onSuccess(long groupId) {
                    Toast.makeText(mContext,"创建群组成功",Toast.LENGTH_SHORT).show();
                    getDataFromServer();
                }

                @Override
                public void onError(String errorCode, String errorMsg) {
                    Toast.makeText(mContext,"创建群组失败",Toast.LENGTH_SHORT).show();

                }
            });
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

    public void showDismissDialog(final long groupId) {
        GroupBean groupBean=TuyaHomeSdk.getDataInstance().getGroupBean(groupId);
        if (groupBean != null) {
            DialogUtils.simpleConfirmDialog(mContext, "Tip", "是否删除群组 " + groupBean.getName(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == -1) {
                        ITuyaGroup mGroup = TuyaHomeSdk.newBlueMeshGroupInstance(groupId);
                        mGroup.dismissGroup(new IResultCallback() {
                            @Override
                            public void onError(String code, String errorMsg) {
                                Toast.makeText(mContext, "解散群组失败 "+ errorMsg, Toast.LENGTH_LONG).show();
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
