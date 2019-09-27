package com.smart.tuya.meshdemo.Config;

/**
 * @author aze
 * @Des
 * @date 2019-07-16.
 */
public class MeshTypeConfig {
    private static int mType=-1;
    public static final int TYPE_BLUEMESH=1;
    public static final int TYPE_SIGMESH=2;

    public static int getType() {
        return mType;
    }

    public static void setType(int type) {
        mType = type;
    }
}
