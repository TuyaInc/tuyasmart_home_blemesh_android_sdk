package com.smart.tuya.meshdemo.activity;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.smart.tuya.meshdemo.R;
import com.smart.tuya.meshdemo.fragment.ConfigTipFragment;

public class ConfigTipActivity extends AppCompatActivity {
    public static final String TAG="UpgradeTipActivity";
    private ConfigTipFragment mBindDeviceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_tip);
        initView();


    }

    private void initView() {
        setTitle("");
        mBindDeviceFragment = new ConfigTipFragment();
        mBindDeviceFragment.setArguments(getIntent().getExtras());

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager. beginTransaction();
        transaction.replace(R.id.content_fragment, mBindDeviceFragment);
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
