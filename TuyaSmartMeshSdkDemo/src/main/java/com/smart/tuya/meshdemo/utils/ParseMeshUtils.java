package com.smart.tuya.meshdemo.utils;

import android.text.TextUtils;

import com.tuya.smart.android.common.utils.L;

public class ParseMeshUtils {
    //获取设备主venderId
    public static String getDeviceMainVenderId(String pcc) {
        L.d("ParseMeshUtils", "getDeviceMainVenderId " + pcc);

        if (!TextUtils.isEmpty(pcc)) {
            return pcc.split(",")[0];
        } else {
            return "";
        }

    }
}
