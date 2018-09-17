package com.smart.tuya.meshdemo.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.smart.tuya.meshdemo.R;
import com.smart.tuya.meshdemo.adapter.DeviceListAdapter;
import com.smart.tuya.meshdemo.adapter.GroupListAdapter;
import com.smart.tuya.meshdemo.bean.DeviceUiBean;
import com.smart.tuya.meshdemo.presenter.MeshDeviceListPresenter;
import com.smart.tuya.meshdemo.presenter.MeshGroupListPresenter;
import com.smart.tuya.meshdemo.utils.DialogUtils;
import com.smart.tuya.meshdemo.view.IMeshDeviceListView;
import com.smart.tuya.meshdemo.view.IMeshGroupListView;
import com.tuya.smart.android.common.utils.NetworkUtil;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class MeshGroupActivity extends AppCompatActivity implements IMeshGroupListView,GroupListAdapter.GroupItemClickListener {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private GroupListAdapter mDeviceAdapter;
    private RecyclerView mDevListView;
    private MeshGroupListPresenter meshGroupListPresenter;
    private long homeId;
    private String meshId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_mesh_group);
        initData();
        initView();

    }


    private void initData() {
        homeId=getIntent().getLongExtra("extra_home_id",0);
        meshId=getIntent().getStringExtra("extra_mesh_id");
        //这里获取的是meshId下的群组
        List<GroupBean> deviceBeanList= TuyaHomeSdk.getDataInstance().getMeshGroupList(meshId);

        updateUi(deviceBeanList);

        meshGroupListPresenter=new MeshGroupListPresenter(this,this,homeId,meshId);
    }

    private void initView() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mDevListView = (RecyclerView) findViewById(R.id.lv_group_list);

        initSwipeRefreshLayout();

        mDeviceAdapter = new GroupListAdapter(this, this);
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
                if (NetworkUtil.isNetworkAvailable(MeshGroupActivity.this)) {
                    meshGroupListPresenter.getDataFromServer();
                } else {
                    Toast.makeText(MeshGroupActivity.this, "网络错误,请检查网络", Toast.LENGTH_SHORT).show();
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
    public void updateUi(List<GroupBean> beanList) {
        final List<DeviceUiBean> uiBeanList=new ArrayList<>();

        for(GroupBean groupBean:beanList){
            DeviceUiBean uiBean=new DeviceUiBean(groupBean.getIconUrl(),groupBean.getName(),true,String.valueOf(groupBean.getId()));
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
    public void updateUi(GroupBean groupBean) {
        final DeviceUiBean uiBean=new DeviceUiBean(groupBean.getIconUrl(),groupBean.getName(),groupBean.getIsOnline(),groupBean.getId()+"");
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        mDeviceAdapter.updateData(uiBean);

                    }
                });

    }


    public void doCreateGroup(View view) {
        if (getEnableGroupId(meshId)) {
            //ProgressUtil.hideLoading();
            meshGroupListPresenter.showCreateDialog();
        } else {
            //ProgressUtil.hideLoading();
            Toast.makeText(this,"群组分配已满",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void itemOnClick(String groupId) {
        //meshGroupListPresenter.
        MeshGroupDevListActivity.startEdit(this,meshId,Long.valueOf(groupId));
    }

    @Override
    public void itemOnLongClick(String groupId) {
        meshGroupListPresenter.showDismissDialog(Long.valueOf(groupId));
    }

    /**
     * 得到可以创建的 groupID
     *
     * @return
     */
    public boolean getEnableGroupId(String meshId) {

        List<GroupBean> groupBeanList = TuyaHomeSdk.getDataInstance().getMeshGroupList(meshId);

        if (groupBeanList == null || groupBeanList.size() == 0) {
            return true;
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

            return localIdList.size() != 0;
        }

    }
}
