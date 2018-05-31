package com.smart.tuya.meshdemo.presenter;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ListHolder;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.smart.tuya.meshdemo.R;
import com.smart.tuya.meshdemo.utils.ProgressUtil;
import com.smart.tuya.meshdemo.view.IMeshDemoView;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.api.ITuyaHome;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhusg on 2018/5/26.
 */

public class FamilyPresenter {
    public Context mContext;
    private IMeshDemoView iMeshDemoView;
    public static HomeBean currentHomeBean;

    public FamilyPresenter(Context context, IMeshDemoView meshDemoView) {
        this.mContext = context;
        this.iMeshDemoView = meshDemoView;
    }

    public void showCreateDialog() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_input, null);
        final EditText editText1 = view.findViewById(R.id.ed_input1);
        final EditText editText2 = view.findViewById(R.id.ed_input2);
        editText1.setHint("familyName");
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
                String familyName = editText1.getText().toString();
                if (!TextUtils.isEmpty(familyName)) {
                    createHome(familyName, 0, 0, "浙江杭州", new ArrayList<String>());
                    dialogPlus.dismiss();
                } else {
                    Toast.makeText(mContext, "家庭名不能为空", Toast.LENGTH_SHORT).show();
                }

            }
        });
        dialogPlus.show();
    }

    /**
     * @param name    家庭名称
     * @param lon     经度
     * @param lat     纬度
     * @param geoName 家庭地理位置名称
     * @param rooms   房间列表
     */
    public void createHome(String name, double lon, double lat, String geoName, List<String> rooms) {
        TuyaHomeSdk.getHomeManagerInstance().createHome(name, lon, lat, geoName, rooms, new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                Toast.makeText(mContext, "创建家庭成功", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(String code, String error) {
                Toast.makeText(mContext, "code: " + code + " error:" + error, Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void queryHomeList() {
        ProgressUtil.showLoading(mContext, "loading");
        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> list) {
                ProgressUtil.hideLoading();
                showFamilyList(list);
            }

            @Override
            public void onError(String code, String error) {
                Toast.makeText(mContext, "获取家庭列表失败 code: " + code + " error:" + error, Toast.LENGTH_SHORT).show();
                ProgressUtil.hideLoading();

            }
        });
    }

    public void showFamilyList(final List<HomeBean> list) {
        if (list.isEmpty()) {
            Toast.makeText(mContext, "当前家庭为空  请先创建群组", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> homeNameList = new ArrayList<>();
        for (HomeBean bean : list) {
            homeNameList.add(bean.getName());
        }


        ArrayAdapter adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, homeNameList);
        final DialogPlus dialogPlus = DialogPlus.newDialog(mContext)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                        getHomeDetail(list.get(position));
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

    public void getHomeDetail(HomeBean homeBean) {
        ITuyaHome mTuyaHome = TuyaHomeSdk.newHomeInstance(homeBean.getHomeId());
        mTuyaHome.getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                Toast.makeText(mContext, "初始化家庭成功", Toast.LENGTH_SHORT).show();
                currentHomeBean = homeBean;
                iMeshDemoView.updateTip();
            }

            @Override
            public void onError(String code, String error) {
                Toast.makeText(mContext, "初始化家庭失败 code: " + code + " error:" + error, Toast.LENGTH_SHORT).show();

            }
        });
    }

    public static HomeBean getCurrentHomeBean() {
        return currentHomeBean;
    }

    public static void clearCurrentHomeBean() {
        currentHomeBean = null;
    }

}
