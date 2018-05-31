package com.smart.tuya.meshdemo.bean;

/**
 * Created by zhusg on 2018/5/29.
 */

public class DeviceUiBean {
    private String iconUrl;
    private String name;
    private boolean isOnline;
    private String devId;

    public DeviceUiBean(String iconUrl, String name, boolean isOnline, String devId) {
        this.iconUrl = iconUrl;
        this.name = name;
        this.isOnline = isOnline;
        this.devId = devId;
    }

    public DeviceUiBean() {
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean status) {
        this.isOnline = status;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }
}
