package com.smart.tuya.meshdemo.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by zhusg on 2018/5/26.
 */

public class CheckPermissionUtils {
    public static final int ALL_REQUEST_CODE = 16;
    private static final int PHONE_REQUEST_CODE = 17;
    private static final int STORAGE_REQUEST_CODE = 18;
    private static final int LOCATION_REQUEST_CODE = 19;
    private static final int CONTACTS_REQUEST_CODE = 20;
    private static final int WRITE_SETTINGS_REQUEST_CODE = 21;
    private static final int OVERLAY_REQUEST_CODE = 22;
    private Activity mActivity;
    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    public CheckPermissionUtils(Activity activity) {
        this.mActivity = activity;
    }

    public boolean checkPermission(int requestCode) {
        return Build.VERSION.SDK_INT < 23 || this.permissionCheck();
    }

    public boolean hasPermission(String permission) {
        int targetSdkVersion = 0;

        try {
            PackageInfo info = this.mActivity.getPackageManager().getPackageInfo(this.mActivity.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException var4) {
            var4.printStackTrace();
        }

        boolean result = true;
        if(Build.VERSION.SDK_INT >= 23) {
            if(targetSdkVersion >= 23) {
                result = ContextCompat.checkSelfPermission(this.mActivity, permission) == 0;
            } else {
                result = PermissionChecker.checkSelfPermission(this.mActivity, permission) == 0;
            }
        }

        return result;
    }

    @TargetApi(23)
    private boolean addPermission(List<String> permissionsList, String permission) {
        boolean has = this.hasPermission(permission);
        if(!has) {
            permissionsList.add(permission);
            if(!ActivityCompat.shouldShowRequestPermissionRationale(this.mActivity, permission)) {
                return false;
            }
        }

        return has;
    }

    private boolean permissionCheck() {
        List<String> permissionsNeeded = new ArrayList();
        List<String> permissionsList = new ArrayList();
        if(permissionsList.size() > 0) {
            if(permissionsNeeded.size() > 0) {
                ActivityCompat.requestPermissions(this.mActivity, (String[])permissionsList.toArray(new String[permissionsList.size()]), 124);
                return false;
            } else {
                ActivityCompat.requestPermissions(this.mActivity, (String[])permissionsList.toArray(new String[permissionsList.size()]), 124);
                return false;
            }
        } else {
            return true;
        }
    }

    public boolean checkSinglePermissionWithoutRequest(String permission) {
        return Build.VERSION.SDK_INT < 23?true:this.hasPermission(permission);
    }

    public boolean checkSinglePermissionWhitFragment(Fragment fragment, String permission, int resultCode) {
        if(Build.VERSION.SDK_INT < 23) {
            return true;
        } else {
            boolean hasPermission = this.hasPermission(permission);
            if(!hasPermission) {
                if(!ActivityCompat.shouldShowRequestPermissionRationale(this.mActivity, permission)) {
                    fragment.requestPermissions(new String[]{permission}, resultCode);
                    return false;
                } else {
                    fragment.requestPermissions(new String[]{permission}, resultCode);
                    return false;
                }
            } else {
                return true;
            }
        }
    }

    public boolean checkSinglePermission(String permission, int resultCode) {
        if(Build.VERSION.SDK_INT < 23) {
            return true;
        } else {
            boolean hasPermission = this.hasPermission(permission);
            if(!hasPermission) {
                if(!ActivityCompat.shouldShowRequestPermissionRationale(this.mActivity, permission)) {
                    ActivityCompat.requestPermissions(this.mActivity, new String[]{permission}, resultCode);
                    return false;
                } else {
                    ActivityCompat.requestPermissions(this.mActivity, new String[]{permission}, resultCode);
                    return false;
                }
            } else {
                return true;
            }
        }
    }

    public boolean onRequestPermissionsResult(String[] permissions, int[] grantResults) {
        return grantResults.length == 0?false:grantResults[0] == 0;
    }



    private void requestSystemSettingPermission() {
    }

//    public boolean onActivityResult(int requestCode) {
//        if(Build.VERSION.SDK_INT >= 23 && requestCode == 21) {
//            String errorPermission = "";
//            TypedArray a = this.mActivity.obtainStyledAttributes(new int[]{attr.is_open_system_setting_limit});
//            boolean isOpen = a.getBoolean(0, false);
//            if(!isOpen && !Settings.System.canWrite(this.mActivity)) {
//                errorPermission = "WRITE_SETTINGS";
//            }
//
//            if(!TextUtils.isEmpty(errorPermission)) {
//                this.requestSystemSettingPermission();
//                return false;
//            } else {
//                return true;
//            }
//        } else {
//            return false;
//        }
//    }

    private static boolean selfPermissionGranted(Context context, String permission) {
        int targetSdkVersion = 0;

        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException var4) {
            var4.printStackTrace();
        }

        boolean result = true;
        Log.d("huohuo", "selfPermissionGranted targetSdkVersion " + targetSdkVersion);
        if(Build.VERSION.SDK_INT >= 23) {
            if(targetSdkVersion >= 23) {
                result = context.checkSelfPermission(permission) == 0 && ContextCompat.checkSelfPermission(context, permission) == 0;
                Log.d("huohuo", "targetSdkVersion >= Android M, we can Context#checkSelfPermission " + result);
            } else {
                result = PermissionChecker.checkSelfPermission(context, permission) == 0;
                Log.d("huohuo", "targetSdkVersion < Android M, we have to use PermissionChecker " + result);
            }
        }

        return result;
    }

    public static boolean requestPermission(Activity context, String permission, int requestCode, String tip) {
        if(Build.VERSION.SDK_INT < 23) {
            return true;
        } else if(!selfPermissionGranted(context, permission)) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                if(!TextUtils.isEmpty(tip)) {
                    Toast.makeText(context, tip, Toast.LENGTH_LONG).show();
                }
            } else {
                ActivityCompat.requestPermissions(context, new String[]{permission}, requestCode);
            }

            return false;
        } else {
            return true;
        }
    }

    public static boolean requestPermission(Activity context, String permission, int requestCode) {
        return requestPermission(context, permission, requestCode, "");
    }

    public static boolean requestDrawOverLays(Activity activity) {
        if(Build.VERSION.SDK_INT < 23) {
            return true;
        } else if(!Settings.canDrawOverlays(activity)) {
            Toast.makeText(activity, "can not DrawOverlays", 0).show();
            Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            return false;
        } else {
            return true;
        }
    }


    public static boolean checkIsRegistPer(Context context, String permission) {
        PackageManager pm = context.getPackageManager();
        boolean isRegist = 0 == pm.checkPermission(permission, context.getPackageName());
        return isRegist;
    }

    public interface OnRequestDrawOverLaysListener {
        void onGranted(boolean var1);
    }
}
