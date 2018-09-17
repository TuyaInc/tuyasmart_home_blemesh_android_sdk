package com.smart.tuya.meshdemo.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

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

    public static void customerListDialogTitleCenter(Context context, String title, RecyclerView.Adapter adapter,
                                                     DialogInterface.OnClickListener listener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        dialog.setPositiveButton("confirm", listener);
        View contentView =  LayoutInflater.from(context).inflate(
                R.layout.bluemesh_dialog_custom_list, null);
        TextView tvTitle= (TextView) contentView.findViewById(R.id.dialog_title);
        tvTitle.setText(title);
        RecyclerView listView = (RecyclerView) contentView.findViewById(R.id.dialog_simple_listview);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(adapter);

        dialog.setView(contentView);
        final AlertDialog create = dialog.create();
        create.setCanceledOnTouchOutside(true);
        create.show();
    }



}
