package com.smart.tuya.meshdemo.utils;

import android.text.TextUtils;

/**
 * @author aze
 * @Des
 * @date 2019-07-18.
 */
public class DeviceTypeUtils {
    //获取设备主venderId   1510 公版五路灯   第四位和第一位 01表示大类  第二位表示子类 第三位表示设备类型  公版还是vendor
    public static String getSigMeshProductType(String pcc) {
        pcc = ParseMeshUtils.getDeviceMainVenderId(pcc);
        if (!TextUtils.isEmpty(pcc) && pcc.length() == 4) {
            return pcc.substring(3, 4) + pcc.substring(0, 1);
        } else {
            return null;
        }
    }


}
