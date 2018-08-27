package com.smart.tuya.meshdemo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.tuya.meshdemo.R;
import com.smart.tuya.meshdemo.presenter.FamilyPresenter;
import com.smart.tuya.meshdemo.presenter.LoginPresenter;
import com.smart.tuya.meshdemo.presenter.MeshPresenter;
import com.smart.tuya.meshdemo.view.IMeshDemoView;
import com.tuya.smart.android.user.api.ILogoutCallback;
import com.tuya.smart.home.sdk.TuyaHomeSdk;

import static com.smart.tuya.meshdemo.presenter.MeshPresenter.GPS_REQUEST_CODE;
import static com.smart.tuya.meshdemo.presenter.MeshPresenter.REQUEST_OPEN_BLE;

public class MeshDemoActivity extends AppCompatActivity implements IMeshDemoView {
    private LoginPresenter mLoginPresenter;
    private FamilyPresenter mFamilyPresenter;
    private MeshPresenter mMeshPresenter;

    private TextView tv_tip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesh_demo);
        initData();
        initView();
        updateTip();
    }


    private void initView() {
        tv_tip = findViewById(R.id.tv_tip);
    }


    private void initData() {
        mLoginPresenter = new LoginPresenter(this, this);
        mFamilyPresenter = new FamilyPresenter(this, this);
        mMeshPresenter = new MeshPresenter(this, this);

    }

    public void doRegister(View view) {
        mLoginPresenter.showRegisterDialog();
    }

    public void doLogin(View view) {
        mLoginPresenter.checkLogin();

    }

    @Override
    public void updateTip() {
        StringBuilder tipBuilder = new StringBuilder();
        tipBuilder.append("当前登录用户：" + (TuyaHomeSdk.getUserInstance().getUser() != null ? TuyaHomeSdk.getUserInstance().getUser().getUsername() : ""));
        tipBuilder.append("\n当前家庭：" + (FamilyPresenter.getCurrentHomeBean() != null ? FamilyPresenter.getCurrentHomeBean().getName() : ""));
        tipBuilder.append("\n当前Mesh：" + (MeshPresenter.getCurrentMeshBean() != null ? MeshPresenter.getCurrentMeshBean().getName() : ""));

        tv_tip.setText(tipBuilder.toString());
    }

    public void doCreateFamily(View view) {
        mFamilyPresenter.showCreateDialog();
    }

    public void doChoiceFamily(View view) {
        mFamilyPresenter.queryHomeList();
    }

    public void doCreateMesh(View view) {
        if (mFamilyPresenter.getCurrentHomeBean() != null) {
            mMeshPresenter.showCreateDialog(mFamilyPresenter.getCurrentHomeBean().getHomeId());
        } else {
            Toast.makeText(this, "请先初始化家庭", Toast.LENGTH_SHORT).show();
        }
    }

    public void doRemoveMesh(View view) {
        if (mFamilyPresenter.getCurrentHomeBean() != null) {
            mMeshPresenter.showMeshList(mFamilyPresenter.getCurrentHomeBean().getHomeId(),1);
        } else {
            Toast.makeText(this, "请先初始化家庭", Toast.LENGTH_SHORT).show();
        }
    }

    public void doInitMesh(View view) {
        if (mFamilyPresenter.getCurrentHomeBean() != null) {
            mMeshPresenter.showMeshList(mFamilyPresenter.getCurrentHomeBean().getHomeId(),0);
        } else {
            Toast.makeText(this, "请先初始化家庭", Toast.LENGTH_SHORT).show();
        }
    }

    public void doDestroyMesh(View view) {
        mMeshPresenter.destroyMesh();
    }

    public void doSearch(View view) {
        mMeshPresenter.check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_REQUEST_CODE) {
            if (resultCode == 0) {
                mMeshPresenter.check();
            }
        } else if (requestCode == REQUEST_OPEN_BLE) {
            if (resultCode == RESULT_OK) {
                mMeshPresenter.check();
            }
        }
    }

    public void doMeshConfig(View view) {
        if (FamilyPresenter.getCurrentHomeBean() != null) {
            if(MeshPresenter.getCurrentMeshBean()!=null){
                mMeshPresenter.showSearchList(mFamilyPresenter.getCurrentHomeBean().getHomeId());
            }else {
                Toast.makeText(this, "请先初始化Mesh", Toast.LENGTH_SHORT).show();

            }
        } else {
            Toast.makeText(this, "请先初始化家庭", Toast.LENGTH_SHORT).show();
        }
    }

    public void doDeviceControl(View view) {
        if (FamilyPresenter.getCurrentHomeBean() != null) {
            if(MeshPresenter.getCurrentMeshBean()!=null){
                Intent intent=new Intent(this,MeshDevcieListActivity.class);
                intent.putExtra("extra_home_id",FamilyPresenter.getCurrentHomeBean().getHomeId());
                intent.putExtra("extra_mesh_id",MeshPresenter.getCurrentMeshBean().getMeshId());

                startActivity(intent);
            }else {
                Toast.makeText(this, "请先初始化Mesh", Toast.LENGTH_SHORT).show();

            }
        } else {
            Toast.makeText(this, "请先初始化家庭", Toast.LENGTH_SHORT).show();
        }
    }

    public void doLogout(View view) {
        mLoginPresenter.logout();
    }

    public void doGroupControl(View view) {
        if (FamilyPresenter.getCurrentHomeBean() != null) {
            if(MeshPresenter.getCurrentMeshBean()!=null){
                Intent intent=new Intent(this,MeshGroupActivity.class);
                intent.putExtra("extra_home_id",FamilyPresenter.getCurrentHomeBean().getHomeId());
                intent.putExtra("extra_mesh_id",MeshPresenter.getCurrentMeshBean().getMeshId());

                startActivity(intent);
            }else {
                Toast.makeText(this, "请先初始化Mesh", Toast.LENGTH_SHORT).show();

            }
        } else {
            Toast.makeText(this, "请先初始化家庭", Toast.LENGTH_SHORT).show();
        }
    }
}
