package com.smart.tuya.meshdemo.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by zhusg on 2018/5/26.
 */

public class BluetoothUtils {
    private static BluetoothManager mBluetoothManager;
    private static BluetoothAdapter mBluetoothLeAdapter;
    private static BluetoothAdapter mBluetoothClassicAdapter;

    public BluetoothUtils() {
    }

    public static boolean isBleSupported(Context context) {
        return CheckPermissionUtils.checkIsRegistPer(context, "android.permission.BLUETOOTH_ADMIN") && CheckPermissionUtils.checkIsRegistPer(context, "android.permission.BLUETOOTH")? Build.VERSION.SDK_INT >= 18 && context.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le"):false;
    }

    public static boolean isBluetoothEnabled() {
        return getBluetoothState() == 12;
    }

    public static int getBluetoothState() {
        BluetoothAdapter adapter = getBluetoothClassicAdapter();
        return adapter != null?adapter.getState():0;
    }

    public static boolean openBluetooth() {
        BluetoothAdapter adapter = getBluetoothClassicAdapter();
        return adapter != null?adapter.enable():false;
    }

    public static boolean closeBluetooth() {
        BluetoothAdapter adapter = getBluetoothClassicAdapter();
        return adapter != null?adapter.disable():false;
    }

    public static BluetoothManager getBluetoothManager(Context context) {
        if(isBleSupported(context)) {
            if(mBluetoothManager == null) {
                mBluetoothManager = (BluetoothManager)context.getSystemService("bluetooth");
            }

            return mBluetoothManager;
        } else {
            return null;
        }
    }

    public static BluetoothAdapter getBluetoothLeAdapter(Context context) {
        if(mBluetoothLeAdapter == null) {
            BluetoothManager manager = getBluetoothManager(context);
            if(manager != null && Build.VERSION.SDK_INT >= 18) {
                mBluetoothLeAdapter = manager.getAdapter();
            }
        }

        return mBluetoothLeAdapter;
    }

    public static BluetoothAdapter getBluetoothClassicAdapter() {
        if(mBluetoothClassicAdapter == null) {
            mBluetoothClassicAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        return mBluetoothClassicAdapter;
    }

    public static BluetoothDevice getRemoteDevice(String mac, Context context) {
        if(!TextUtils.isEmpty(mac)) {
            BluetoothAdapter adapter = getBluetoothLeAdapter(context);
            if(adapter != null) {
                return adapter.getRemoteDevice(mac);
            }
        }

        return null;
    }

    public static List<BluetoothDevice> getConnectedBluetoothLeDevices(Context context) {
        List<BluetoothDevice> devices = new ArrayList();
        BluetoothManager manager = getBluetoothManager(context);
        if(manager != null && Build.VERSION.SDK_INT >= 18) {
            devices.addAll(manager.getConnectedDevices(7));
        }

        return devices;
    }

    public static List<BluetoothDevice> getBondedBluetoothClassicDevices() {
        BluetoothAdapter adapter = getBluetoothClassicAdapter();
        List<BluetoothDevice> devices = new ArrayList();
        if(adapter != null) {
            Set<BluetoothDevice> sets = adapter.getBondedDevices();
            if(sets != null) {
                devices.addAll(sets);
            }
        }

        return devices;
    }

    public static boolean isDeviceConnected(String mac, Context context) {
        if(!TextUtils.isEmpty(mac) && isBleSupported(context)) {
            BluetoothDevice device = getBluetoothLeAdapter(context).getRemoteDevice(mac);
            return getBluetoothManager(context).getConnectionState(device, 7) == 2;
        } else {
            return false;
        }
    }

}
