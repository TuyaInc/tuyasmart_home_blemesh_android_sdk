package com.smart.tuya.meshdemo.activity;

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
import com.smart.tuya.meshdemo.presenter.ControlPresenter;
import com.smart.tuya.meshdemo.view.IControlView;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.LinkedHashMap;
import java.util.Map;

public class LightControlActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener ,IControlView {
    private Switch btn_switch;
    private RadioGroup modeRadioGroup;

    private SeekBar lightSeekbar;
    private SeekBar wcSeekbar;

    private SeekBar redSeekbar;
    private SeekBar greenSeekbar;
    private SeekBar blueSeekbar;

    private ControlPresenter mControlPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_control);

        initView();
        initData();
    }

    private void initData() {
        String devId = getIntent().getStringExtra("devId");
        mControlPresenter = new ControlPresenter(this, devId,this);

        //获取当前设备最新信息
        mControlPresenter.requestDeviceInfo();

    }

    private void initView() {
        btn_switch = findViewById(R.id.btn_switch);
        btn_switch.setOnCheckedChangeListener(this);

        modeRadioGroup = findViewById(R.id.rg_mode);
        modeRadioGroup.setOnCheckedChangeListener(this);


        lightSeekbar = findViewById(R.id.sb_light);
        wcSeekbar = findViewById(R.id.sb_wc);
        redSeekbar = findViewById(R.id.sb_color_red);
        greenSeekbar = findViewById(R.id.sb_color_green);
        blueSeekbar = findViewById(R.id.sb_color_blue);

        lightSeekbar.setOnSeekBarChangeListener(this);
        wcSeekbar.setOnSeekBarChangeListener(this);
        redSeekbar.setOnSeekBarChangeListener(this);
        greenSeekbar.setOnSeekBarChangeListener(this);
        blueSeekbar.setOnSeekBarChangeListener(this);
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
            case R.id.sb_color_red:
            case R.id.sb_color_green:
            case R.id.sb_color_blue:
                sendRGBCommand(redSeekbar.getProgress(), greenSeekbar.getProgress(), blueSeekbar.getProgress());
                break;
        }
    }

    private void sendRGBCommand(int r, int g, int b) {
        LinkedHashMap<String, Object> dpMap = new LinkedHashMap<>();
        dpMap.put("101", r);
        dpMap.put("102", g);
        dpMap.put("103", b);
        JSONObject jsonObject = new JSONObject(dpMap);
        sendCommand(jsonObject.toJSONString());
    }

    private void sendModeCommand(String mode) {
        LinkedHashMap<String, Object> dpMap = new LinkedHashMap<>();
        dpMap.put("109", mode);
        JSONObject jsonObject = new JSONObject(dpMap);
        sendCommand(jsonObject.toJSONString());
    }


    private void sendWCCommand(int progress) {
        LinkedHashMap<String, Object> dpMap = new LinkedHashMap<>();
        dpMap.put("104", progress);
        dpMap.put("105", 255 - progress);
        JSONObject jsonObject = new JSONObject(dpMap);
        sendCommand(jsonObject.toJSONString());
    }

    private void sendSwitchCommand(final boolean isOpen) {
        LinkedHashMap<String, Object> dpMap = new LinkedHashMap<>();
        dpMap.put("1", isOpen);
        JSONObject jsonObject = new JSONObject(dpMap);
        sendCommand(jsonObject.toJSONString());
    }

    private void sendLightCommand(int light) {
        LinkedHashMap<String, Object> dpMap = new LinkedHashMap<>();
        dpMap.put("3", light);
        JSONObject jsonObject = new JSONObject(dpMap);
        sendCommand(jsonObject.toJSONString());
    }

    public void sendCommand(String command) {
        if(mControlPresenter==null) return;
        mControlPresenter.sendDps(command, new IResultCallback() {
            @Override
            public void onError(String s, String s1) {
                Toast.makeText(LightControlActivity.this, "contral fail " + s + "  " + s1, Toast.LENGTH_SHORT).show();
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
        redSeekbar.setOnSeekBarChangeListener(null);
        greenSeekbar.setOnSeekBarChangeListener(null);
        blueSeekbar.setOnSeekBarChangeListener(null);
        btn_switch.setOnCheckedChangeListener(null);

        LinkedHashMap<String, Object> dpMap = JSONObject.parseObject(dps, new TypeReference<LinkedHashMap<String, Object>>() {
        });

        for (Map.Entry<String, Object> entry : dpMap.entrySet()) {
            Object value=entry.getValue();
            switch(entry.getKey()){
                case "1":
                    btn_switch.setChecked((Boolean) value);
                    break;
                case "3":
                    lightSeekbar.setProgress((Integer) value);
                    break;
                case "101":
                    redSeekbar.setProgress((Integer) value);
                    break;
                case "102":
                    greenSeekbar.setProgress((Integer) value);
                    break;
                case "103":
                    blueSeekbar.setProgress((Integer) value);
                    break;
                case "104":
                    wcSeekbar.setProgress((Integer) value);
                    break;
                case "109":
                    if("white".equals(value))
                        modeRadioGroup.check(R.id.rb_mode_white);
                    else if("colour".equals(value))
                        modeRadioGroup.check(R.id.rb_mode_color);
                    break;

            }
        }

        modeRadioGroup.setOnCheckedChangeListener(this);
        lightSeekbar.setOnSeekBarChangeListener(this);
        wcSeekbar.setOnSeekBarChangeListener(this);
        redSeekbar.setOnSeekBarChangeListener(this);
        greenSeekbar.setOnSeekBarChangeListener(this);
        blueSeekbar.setOnSeekBarChangeListener(this);
        btn_switch.setOnCheckedChangeListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mControlPresenter.onDestroy();
    }

}
