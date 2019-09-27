package com.smart.tuya.meshdemo.presenter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.smart.tuya.meshdemo.Config.MeshTypeConfig;
import com.smart.tuya.meshdemo.R;
import com.smart.tuya.meshdemo.view.IMeshDemoView;


/**
 * @author aze
 * @Des
 * @date 2019-07-16.
 */
public class MeshTypePresenter {
    private Context mConetxt;
    private IMeshDemoView mIMeshDemoView;

    public MeshTypePresenter(Context context, IMeshDemoView iMeshDemoView) {
        mIMeshDemoView = iMeshDemoView;
        mConetxt = context;
    }

    public void choice() {
        View mView = LayoutInflater.from(mConetxt).inflate(R.layout.dialog_choice_type, null);
        TextView textView = new TextView(mConetxt);
        textView.setText("您必须先选择Mesh类型");
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(16);
        final DialogPlus dialogPlus = DialogPlus.newDialog(mConetxt)
                .setContentHolder(new ViewHolder(mView))
                .setExpanded(false)
                .setPadding(60, 50, 60, 80)
                .setGravity(Gravity.CENTER)
                .setCancelable(false)
                .setHeader(textView)
                .create();
        mView.findViewById(R.id.bt_choice_bluemesh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MeshTypeConfig.setType(MeshTypeConfig.TYPE_BLUEMESH);
                dialogPlus.dismiss();
                mIMeshDemoView.initMeshPresenter();
                mIMeshDemoView.updateTip();
            }
        });
        mView.findViewById(R.id.bt_choice_sigmesh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MeshTypeConfig.setType(MeshTypeConfig.TYPE_SIGMESH);
                dialogPlus.dismiss();
                mIMeshDemoView.initMeshPresenter();
                mIMeshDemoView.updateTip();
            }
        });
        dialogPlus.show();
    }

    public String getMeshTypeString() {
        switch (MeshTypeConfig.getType()) {
            case MeshTypeConfig.TYPE_BLUEMESH:
                return "蓝牙MESH(涂鸦)";
            case MeshTypeConfig.TYPE_SIGMESH:
                return "SigMesh(SIG)";
            default:
                return "";
        }
    }
}
