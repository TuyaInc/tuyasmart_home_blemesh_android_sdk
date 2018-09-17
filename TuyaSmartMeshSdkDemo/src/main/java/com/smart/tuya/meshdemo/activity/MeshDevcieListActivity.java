package com.smart.tuya.meshdemo.activity;

import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.smart.tuya.meshdemo.R;
import com.smart.tuya.meshdemo.adapter.DeviceListAdapter;
import com.smart.tuya.meshdemo.bean.DeviceUiBean;
import com.smart.tuya.meshdemo.presenter.MeshDeviceListPresenter;
import com.smart.tuya.meshdemo.view.IMeshDeviceListView;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.common.utils.NetworkUtil;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.bean.BlueMeshBean;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class MeshDevcieListActivity extends AppCompatActivity implements DeviceListAdapter.DeviceItemClickListener,IMeshDeviceListView {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DeviceListAdapter mDeviceAdapter;
    private RecyclerView mDevListView;
    private MeshDeviceListPresenter meshDeviceListPresenter;
    private long homeId;
    private String meshId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesh_devcie_list);
        initView();
        initData();
    }

    private void initData() {
        homeId=getIntent().getLongExtra("extra_home_id",0);
        meshId=getIntent().getStringExtra("extra_mesh_id");
        //这里获取的是当前home下缓存的所有设备
        List<DeviceBean> deviceBeanList=TuyaHomeSdk.getDataInstance().getHomeDeviceList(homeId);

        //只获取缓存下的mesh设备
        //List<DeviceBean> deviceBeanList==TuyaHomeSdk.getDataInstance().getMeshDeviceList(meshId);


        updateUi(deviceBeanList);

        meshDeviceListPresenter=new MeshDeviceListPresenter(this,this,homeId,meshId);
    }

    public void updateUi(List<DeviceBean> deviceBeanList) {
        final List<DeviceUiBean> uiBeanList=new ArrayList<>();

        for(DeviceBean devBean:deviceBeanList){
            DeviceUiBean uiBean=new DeviceUiBean(devBean.getIconUrl(),devBean.getName(),devBean.getIsOnline(),devBean.getDevId());
            uiBeanList.add(uiBean);
        }

        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        mDeviceAdapter.updateData(uiBeanList);

                    }
                });
    }

    @Override
    public void updateUi(DeviceBean devBean) {
        final DeviceUiBean uiBean=new DeviceUiBean(devBean.getIconUrl(),devBean.getName(),devBean.getIsOnline(),devBean.getDevId());
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        mDeviceAdapter.updateData(uiBean);

                    }
                });

    }

    private void initView() {
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
        mDevListView = (RecyclerView) findViewById(R.id.lv_device_list);

        initSwipeRefreshLayout();

        mDeviceAdapter = new DeviceListAdapter(this, this);
        mDevListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mDevListView.setAdapter(mDeviceAdapter);


    }

    private void initSwipeRefreshLayout() {
        mSwipeRefreshLayout.setColorSchemeColors(Color.parseColor("#ff0000"),
                Color.parseColor("#00ff00"),
                Color.parseColor("#0000ff"));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetworkUtil.isNetworkAvailable(MeshDevcieListActivity.this)) {
                    meshDeviceListPresenter.getDataFromServer();
                } else {
                    Toast.makeText(MeshDevcieListActivity.this,"网络错误,请检查网络",Toast.LENGTH_SHORT).show();
                    loadFinish();
                }
            }
        });
    }

    public void loadStart() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    public void loadFinish() {
        mSwipeRefreshLayout.setRefreshing(false);
    }



    @Override
    public void itemOnClick(String devId) {
        meshDeviceListPresenter.itemOnClick(devId);
    }

    @Override
    public void itemOnLongClick(String devId) {
        meshDeviceListPresenter.itemOnLongClick(devId);
    }

    public void doStartClient(View view) {
        meshDeviceListPresenter.startClient();
    }

    public void doStopClient(View view) {
        meshDeviceListPresenter.stopClient();
    }

    public void doGetStatus(View view){
        meshDeviceListPresenter.getStatusAll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        meshDeviceListPresenter.onDestroy();
    }

    public void doCloseAll(View view) {
        meshDeviceListPresenter.doCloseAll();
    }

    public void doOpenAll(View view) {
        meshDeviceListPresenter.doOpenAll();
    }
}
