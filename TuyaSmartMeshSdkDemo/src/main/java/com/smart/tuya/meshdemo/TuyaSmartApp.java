package com.smart.tuya.meshdemo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.INeedLoginListener;

/**
 * Created by zhusg on 2018/5/25.
 */

public class TuyaSmartApp extends MultiDexApplication {
    public static final String TAG="TuyaSmartApp";
    @Override
    public void onCreate() {
        super.onCreate();
        TuyaHomeSdk.init(this);
        TuyaHomeSdk.setDebugMode(true);
        TuyaHomeSdk.setOnNeedLoginListener(new INeedLoginListener() {
            @Override
            public void onNeedLogin(Context context) {
                Log.e(TAG,"Session 失效");
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

    }
}
