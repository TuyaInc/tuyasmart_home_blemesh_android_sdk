package com.smart.tuya.meshdemo.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.smart.tuya.meshdemo.R;
import com.smart.tuya.meshdemo.utils.DialogUtils;
import com.smart.tuya.meshdemo.view.IMeshDemoView;
import com.tuya.smart.android.user.api.ILoginCallback;
import com.tuya.smart.android.user.api.ILogoutCallback;
import com.tuya.smart.android.user.api.IRegisterCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;

/**
 * Created by zhusg on 2018/5/25.
 */

public class LoginPresenter {
    public Context mContext;
    private IMeshDemoView iMeshDemoView;

    public LoginPresenter(Context context,IMeshDemoView meshDemoView) {
        this.mContext = context;
        this.iMeshDemoView=meshDemoView;
    }

    public void registerAccountWithEmail(final String countryCode, final String email, final String passwd){
        //邮箱密码注册  注册后会直接登录
        TuyaHomeSdk.getUserInstance().registerAccountWithEmail(countryCode, email,passwd, new IRegisterCallback() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(mContext, "注册成功", Toast.LENGTH_SHORT).show();
                iMeshDemoView.updateTip();
            }

            @Override
            public void onError(String code, String error) {
                Toast.makeText(mContext, "code: " + code + "error:" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void loginWithEmail(String countryCode, String email, String passwd){
        //邮箱密码登陆
        TuyaHomeSdk.getUserInstance().loginWithEmail(countryCode, email, passwd, new ILoginCallback() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(mContext, "登录成功",Toast.LENGTH_SHORT).show();
                iMeshDemoView.updateTip();
            }

            @Override
            public void onError(String code, String error) {
                Toast.makeText(mContext, "code: " + code + "error:" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 这里只做了邮箱登录
     */
    public void showRegisterDialog(){
        View view= LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_input,null);
        final EditText editText1=view.findViewById(R.id.ed_input1);
        final EditText editText2=view.findViewById(R.id.ed_input2);
        editText1.setHint("username");
        editText2.setHint("password");
        final DialogPlus dialogPlus=DialogPlus.newDialog(mContext)
                .setContentHolder(new ViewHolder(view))
                //.setHeader(R.layout.dialog_custom_input)
                .setExpanded(false)
//                .setContentWidth(600)
//                .setContentHeight(600)
                .setPadding(60, 50, 60, 80)
                .setGravity(Gravity.CENTER)
                .create();

        view.findViewById(R.id.bt_confim).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName=editText1.getText().toString();
                String pwd=editText2.getText().toString();
                if(!TextUtils.isEmpty(userName)&&!TextUtils.isEmpty(pwd)){
                    registerAccountWithEmail("86",userName,pwd);
                    dialogPlus.dismiss();
                }else{
                    Toast.makeText(mContext, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                }

            }
        });
        dialogPlus.show();
    }

    public void checkLogin(){
        if(TuyaHomeSdk.getUserInstance().isLogin()){
            DialogUtils.simpleConfirmDialog(mContext, "Tip", "已处于登录状态,是否先退出登录", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(i==-1){
                        logout();
                    }
                }
            });
        }else{
            showLoginDialog();
        }
    }

    public void showLoginDialog() {
        View view= LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_input,null);
        final EditText editText1=view.findViewById(R.id.ed_input1);
        final EditText editText2=view.findViewById(R.id.ed_input2);
        editText1.setHint("username");
        editText2.setHint("password");
        final DialogPlus dialogPlus=DialogPlus.newDialog(mContext)
                .setContentHolder(new ViewHolder(view))
                .setExpanded(false)
                .setPadding(60, 50, 60, 80)
                .setGravity(Gravity.CENTER)
                .create();

        view.findViewById(R.id.bt_confim).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName=editText1.getText().toString();
                String pwd=editText2.getText().toString();
                if(!TextUtils.isEmpty(userName)&&!TextUtils.isEmpty(pwd)){
                    loginWithEmail("86",userName,pwd);
                    dialogPlus.dismiss();
                }else{
                    Toast.makeText(mContext, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                }

            }
        });
        dialogPlus.show();
    }


    public void logout(){
        //退出登录
        TuyaHomeSdk.getUserInstance().logout(new ILogoutCallback() {
            @Override
            public void onSuccess() {
                //退出登录成功
                Toast.makeText(mContext, "退出登录成功",Toast.LENGTH_SHORT).show();
                FamilyPresenter.clearCurrentHomeBean();
                iMeshDemoView.updateTip();
            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                Toast.makeText(mContext, "退出登录失败",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
