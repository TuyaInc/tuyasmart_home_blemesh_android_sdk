package com.smart.tuya.meshdemo.activity;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.smart.tuya.meshdemo.R;
import com.smart.tuya.meshdemo.presenter.IControlPresenter;
import com.smart.tuya.meshdemo.presenter.PresenterFactory;
import com.smart.tuya.meshdemo.view.IControlView;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.LinkedHashMap;
import java.util.Map;

public class SigMeshControlActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, IControlView {


    private static final String TAG = "SigMeshControlActivity";
    private Switch btn_switch;
    private RadioGroup modeRadioGroup;

    private SeekBar lightSeekbar;
    private SeekBar wcSeekbar;

    private SeekBar hSeekbar;
    private SeekBar sSeekbar;
    private SeekBar vSeekbar;

    private IControlPresenter mControlPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sigmesh_light_control);
        initView();
        initData();
    }

    private void initData() {
        String devId = getIntent().getStringExtra("devId");
        mControlPresenter = PresenterFactory.getControlPresenter(this, devId, this);

        //获取当前设备最新信息
        mControlPresenter.requestDeviceInfo();

    }

    protected void initView() {
        btn_switch = findViewById(R.id.btn_switch);
        btn_switch.setOnCheckedChangeListener(this);

        modeRadioGroup = findViewById(R.id.rg_mode);
        modeRadioGroup.setOnCheckedChangeListener(this);


        lightSeekbar = findViewById(R.id.sb_light);
        wcSeekbar = findViewById(R.id.sb_wc);
        hSeekbar = findViewById(R.id.sb_color_H);
        sSeekbar = findViewById(R.id.sb_color_S);
        vSeekbar = findViewById(R.id.sb_color_V);

        lightSeekbar.setOnSeekBarChangeListener(this);
        wcSeekbar.setOnSeekBarChangeListener(this);
        hSeekbar.setOnSeekBarChangeListener(this);
        sSeekbar.setOnSeekBarChangeListener(this);
        vSeekbar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == R.id.btn_switch) {
            //开关
            sendSwitchCommand(b);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int id) {
        if (id == R.id.rb_mode_white) {
            //白光
            sendModeCommand("white");
        } else if (id == R.id.rb_mode_color) {
            //彩光
            sendModeCommand("colour");
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int id = seekBar.getId();
        switch (id) {
            case R.id.sb_light:
                sendLightCommand(seekBar.getProgress());
                break;
            case R.id.sb_wc:
                sendWCCommand(seekBar.getProgress());
                break;
            case R.id.sb_color_H:
            case R.id.sb_color_S:
            case R.id.sb_color_V:
                sendRGBCommand(hSeekbar.getProgress(), sSeekbar.getProgress(), vSeekbar.getProgress());
                break;
        }
    }

    public void sendCommand(String command) {
        if (mControlPresenter == null) return;
        mControlPresenter.sendDps(command, new IResultCallback() {
            @Override
            public void onError(String s, String s1) {
                Toast.makeText(SigMeshControlActivity.this, "contral fail " + s + "  " + s1, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {

            }
        });
    }

    public void updateView(String dps) {
        modeRadioGroup.setOnCheckedChangeListener(null);
        lightSeekbar.setOnSeekBarChangeListener(null);
        wcSeekbar.setOnSeekBarChangeListener(null);
        hSeekbar.setOnSeekBarChangeListener(null);
        sSeekbar.setOnSeekBarChangeListener(null);
        vSeekbar.setOnSeekBarChangeListener(null);
        btn_switch.setOnCheckedChangeListener(null);

        LinkedHashMap<String, Object> dpMap = JSONObject.parseObject(dps, new TypeReference<LinkedHashMap<String, Object>>() {
        });

        for (Map.Entry<String, Object> entry : dpMap.entrySet()) {
            Object value = entry.getValue();
            switch (entry.getKey()) {
                case "1":
                    // 开关
                    btn_switch.setChecked((Boolean) value);

                    break;
                case "2":
                    // 模式
                    if ("white".equals(value))
                        modeRadioGroup.check(R.id.rb_mode_white);
                    else if ("colour".equals(value))
                        modeRadioGroup.check(R.id.rb_mode_color);
                    break;
                case "3":
                    // 亮度
                    lightSeekbar.setProgress((Integer) value);
                    break;
                case "4":
                    // 冷暖
                    wcSeekbar.setProgress((Integer) value);
                    break;
                case "5":
                    // hsv
                    String data= (String) value;
                    if (data.length() != 12) {
                        L.e(TAG, "hsv value is not format " + value);
                        break;
                    }
                    int h = Integer.parseInt(data.substring(0, 4), 16);
                    int s = Integer.parseInt(data.substring(4, 8), 16);
                    int v = Integer.parseInt(data.substring(8, 12), 16);
                    hSeekbar.setProgress(h);
                    sSeekbar.setProgress(s);
                    vSeekbar.setProgress(v);
                    break;
            }
        }

        modeRadioGroup.setOnCheckedChangeListener(this);
        lightSeekbar.setOnSeekBarChangeListener(this);
        wcSeekbar.setOnSeekBarChangeListener(this);
        hSeekbar.setOnSeekBarChangeListener(this);
        sSeekbar.setOnSeekBarChangeListener(this);
        vSeekbar.setOnSeekBarChangeListener(this);
        btn_switch.setOnCheckedChangeListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mControlPresenter.onDestroy();
    }

    protected void sendRGBCommand(int h, int s, int v) {
        // hsv 模型
        LinkedHashMap<String, Object> dpMap = new LinkedHashMap<>();
        dpMap.put("5", String.format("%04d%04d%04d", h, s, v));
        JSONObject jsonObject = new JSONObject(dpMap);
        sendCommand(jsonObject.toJSONString());
    }

    protected void sendModeCommand(String mode) {
        LinkedHashMap<String, Object> dpMap = new LinkedHashMap<>();
        dpMap.put("2", mode);
        JSONObject jsonObject = new JSONObject(dpMap);
        sendCommand(jsonObject.toJSONString());
    }


    protected void sendWCCommand(int progress) {
        LinkedHashMap<String, Object> dpMap = new LinkedHashMap<>();
        dpMap.put("4", progress);
        JSONObject jsonObject = new JSONObject(dpMap);
        sendCommand(jsonObject.toJSONString());
    }

    protected void sendSwitchCommand(final boolean isOpen) {
        LinkedHashMap<String, Object> dpMap = new LinkedHashMap<>();
        dpMap.put("1", isOpen);
        JSONObject jsonObject = new JSONObject(dpMap);
        sendCommand(jsonObject.toJSONString());
    }

    protected void sendLightCommand(int light) {
        LinkedHashMap<String, Object> dpMap = new LinkedHashMap<>();
        dpMap.put("3", light);
        JSONObject jsonObject = new JSONObject(dpMap);
        sendCommand(jsonObject.toJSONString());
    }

}
