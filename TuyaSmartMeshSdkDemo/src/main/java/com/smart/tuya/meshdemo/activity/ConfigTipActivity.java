package com.smart.tuya.meshdemo.activity;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.smart.tuya.meshdemo.Config.MeshTypeConfig;
import com.smart.tuya.meshdemo.R;
import com.smart.tuya.meshdemo.fragment.BlueMeshTipFragment;
import com.smart.tuya.meshdemo.fragment.SigMeshTipFragment;

public class ConfigTipActivity extends AppCompatActivity {
    public static final String TAG="UpgradeTipActivity";
    private BlueMeshTipFragment mBindDeviceFragment;
    private SigMeshTipFragment mSigMeshTipFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_tip);
        initView();


    }

    private void initView() {
        setTitle("");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager. beginTransaction();
        switch (MeshTypeConfig.getType()){
            case MeshTypeConfig.TYPE_BLUEMESH:
                mBindDeviceFragment = new BlueMeshTipFragment();
                mBindDeviceFragment.setArguments(getIntent().getExtras());
                transaction.replace(R.id.content_fragment, mBindDeviceFragment);
                break;
            case MeshTypeConfig.TYPE_SIGMESH:
                mSigMeshTipFragment=new SigMeshTipFragment();
                mSigMeshTipFragment.setArguments(getIntent().getExtras());
                transaction.replace(R.id.content_fragment, mSigMeshTipFragment);
                break;
        }
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected String getPageName(){
        return TAG;
    }
}
