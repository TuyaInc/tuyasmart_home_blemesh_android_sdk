package com.smart.tuya.meshdemo.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.smart.tuya.meshdemo.R;

/**
 * Created by zhusg on 2018/5/26.
 */

public class DialogUtils {
    public static void simpleConfirmDialog(Context context, String title, CharSequence msg, final DialogInterface.OnClickListener listener) {
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(listener != null) {
                    listener.onClick(dialog, which);
                }

            }
        };
        AlertDialog.Builder dialog =new AlertDialog.Builder(context, R.style.Dialog_Alert);
        dialog.setNegativeButton("取消", onClickListener);
        dialog.setPositiveButton("确定", onClickListener);
        dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.setCancelable(false);
        dialog.create().show();
    }
}
