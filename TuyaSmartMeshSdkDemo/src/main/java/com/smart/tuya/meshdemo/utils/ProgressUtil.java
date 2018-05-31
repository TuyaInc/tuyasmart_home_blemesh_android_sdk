package com.smart.tuya.meshdemo.utils;



import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.smart.tuya.meshdemo.R;


/**
 * Created by zhusg on 2018/5/26.
 */
public class ProgressUtil {
    private static Dialog progressDialog;

    public ProgressUtil() {
    }

    public static void showLoading(Context context, String message) {
        if(progressDialog == null) {
            progressDialog = getSimpleProgressDialog(context, "", new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    ProgressUtil.progressDialog = null;
                }
            });
        }

        if(!TextUtils.isEmpty(message)) {
            ((TextView)progressDialog.findViewById(R.id.progress_dialog_message)).setText(message);
        }

        if(!isShowLoading()) {
            progressDialog.show();
        }

    }

    public static void setAlpha(float dimAmount, float alpha) {
        LayoutParams lp = progressDialog.getWindow().getAttributes();
        lp.dimAmount = dimAmount;
        lp.alpha = alpha;
        progressDialog.getWindow().setAttributes(lp);
        progressDialog.getWindow().addFlags(2);
    }

    public static void showLoading(Context context, CharSequence title, CharSequence message) {
        showLoading(context, title, message, false);
    }

    public static void showLoading(Context context, CharSequence title, int resId) {
        showLoading(context, title, resId, false);
    }

    public static void showLoading(Context context, CharSequence title, CharSequence message, boolean isCancelable) {
        showLoading(context, title, message, isCancelable, false, (OnCancelListener)null);
    }

    public static void showLoading(Context context, CharSequence title, int resId, boolean isCancelable) {
        showLoading(context, title, resId, isCancelable, false, (OnCancelListener)null);
    }

    public static void showLoading(Context context, CharSequence title, CharSequence message, boolean isCancelable, OnCancelListener listener) {
        showLoading(context, title, message, isCancelable, false, listener);
    }

    public static void showLoading(Context context, CharSequence title, int resId, boolean isCancelable, OnCancelListener listener) {
        showLoading(context, title, resId, isCancelable, false, listener);
    }

    public static void showLoading(Context context, CharSequence title, int resId, boolean isCancelable, boolean isCancelOnTouchOutside, OnCancelListener listener) {
        if(context != null) {
            String message = context.getResources().getString(resId);
            showLoading(context, title, message, isCancelable, isCancelOnTouchOutside, listener);
        }
    }

    public static void showLoading(Context context, CharSequence title, CharSequence message, boolean isCancelable, boolean isCancelOnTouchOutside, OnCancelListener listener) {
        if(context != null) {
            if(progressDialog != null) {
                hideLoading();
            }

            progressDialog = ProgressDialog.show(context, title, message, false, isCancelable, listener);
            progressDialog.setCanceledOnTouchOutside(isCancelOnTouchOutside);
        }
    }

    public static void showLoading(Context context, int resId) {
        showLoading(context, context.getString(resId));
    }

    public static boolean isShowLoading() {
        return progressDialog == null?false:progressDialog.isShowing();
    }

    public static void hideLoading() {
        if(progressDialog != null && progressDialog.getContext() != null) {
            progressDialog.hide();

            try {
                progressDialog.dismiss();
            } catch (Exception var1) {
                var1.printStackTrace();
            }
        }

        progressDialog = null;
    }



    public static Dialog getSimpleProgressDialog(Context mContext, String msg) {
        return getSimpleProgressDialog(mContext, msg, (OnCancelListener)null);
    }

    public static Dialog getSimpleProgressDialog(Context mContext, String msg, OnCancelListener listener) {
        Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.ty_progress_dialog_h);
        if(!TextUtils.isEmpty(msg)) {
            ((TextView)dialog.findViewById(R.id.progress_dialog_message)).setText(msg);
        }

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        if(listener != null) {
            dialog.setOnCancelListener(listener);
        }

        return dialog;
    }
}

