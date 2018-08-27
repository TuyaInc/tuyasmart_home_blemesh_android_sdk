package com.smart.tuya.meshdemo.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.smart.tuya.meshdemo.R;
import com.smart.tuya.meshdemo.adapter.MeshGroupDevListAdapter;
import com.smart.tuya.meshdemo.presenter.MeshGroupDeviceListPresenter;
import com.smart.tuya.meshdemo.view.IMeshGroupDeviceListView;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.List;

public class MeshGroupDevListActivity extends AppCompatActivity implements MeshGroupDevListAdapter.OnClickSelectListener ,IMeshGroupDeviceListView,View.OnClickListener {
    private MeshGroupDevListAdapter mGroupDeviceAdapter;
    private RecyclerView mGroupListView;
    private MeshGroupDeviceListPresenter mGroupDeviceListPresenter;

    private Button btnConfim;

    public static final String EXTRA_GROUP_ID = "extra_groupId";
    public static final String EXTRA_MESH_ID = "extra_meshId";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesh_group_dev_list);
        initView();
    }

    private void initView() {
        mGroupListView = (RecyclerView) findViewById(R.id.lv_group_device_list);
        mGroupDeviceAdapter = new MeshGroupDevListAdapter(this, this);
        mGroupListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mGroupListView.setAdapter(mGroupDeviceAdapter);

        btnConfim=findViewById(R.id.btn_confirm);
        btnConfim.setOnClickListener(this);
        mGroupDeviceListPresenter = new MeshGroupDeviceListPresenter(this, this);

    }

    /**
     * 启动添加群组设备列表界面
     *
     */
    public static void startEdit(Context context, String meshId, long groupId) {
        Intent intent = new Intent(context, MeshGroupDevListActivity.class);
        intent.putExtra(EXTRA_MESH_ID, meshId);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        context.startActivity(intent);
    }

    @Override
    public void onClickSelect(int actionType, DeviceBean bean) {
        mGroupDeviceListPresenter.onClickSelect(actionType, bean);
    }

    @Override
    public void updateAddDeviceList(List<DeviceBean> addBeanList, List<DeviceBean> foundBeanList) {
        if (addBeanList.size() > 0 || foundBeanList.size() > 0) {
            mGroupDeviceAdapter.setAddData(addBeanList);
            mGroupDeviceAdapter.setFoundData(foundBeanList);
        }

    }

    @Override
    public void refreshList() {
        if (mGroupDeviceAdapter != null) {
            mGroupDeviceAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void setConfimBtnClickable(boolean b) {

    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_confirm) {
            mGroupDeviceListPresenter.doConfirm();
        }
    }


    public void doOpen(View v){
        mGroupDeviceListPresenter.doOpen();
    }

    public void doClose(View v){
        mGroupDeviceListPresenter.doClose();

    }
}
