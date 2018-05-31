package com.smart.tuya.meshdemo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.smart.tuya.meshdemo.R;
import com.smart.tuya.meshdemo.bean.DeviceUiBean;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by zsg on 17/8/1.
 */

public class DeviceListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private LayoutInflater mInflater;
    private DeviceItemClickListener listener;
    private ArrayList<DeviceUiBean> datas = new ArrayList<>();


    public DeviceListAdapter(Context context, DeviceItemClickListener listener) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.device_item, parent, false);
        DeviceViewHolder holder = new DeviceViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DeviceViewHolder deviceViewHolder = (DeviceViewHolder) holder;
        DeviceUiBean bean = datas.get(position);
        deviceViewHolder.update(bean);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }


    public void updateData(List<DeviceUiBean> deviceBeanList) {
        datas.clear();
        datas.addAll(deviceBeanList);
        notifyDataSetChanged();
    }


    public void updateData(DeviceUiBean deviceUiBean) {
        for (int i = 0; i < datas.size(); i++) {
            DeviceUiBean uiBean = datas.get(i);
            if (uiBean.getDevId().equals(deviceUiBean.getDevId())) {
                uiBean.setIconUrl(deviceUiBean.getIconUrl());
                uiBean.setName(deviceUiBean.getName());
                uiBean.setIsOnline(deviceUiBean.getIsOnline());
                notifyItemChanged(i);
                break;
            }
        }

    }

    public List<DeviceUiBean> getDeviceDatas() {
        return datas;
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_status;
        DeviceUiBean uiBean;

        public DeviceViewHolder(View itemView) {
            super(itemView);
            iv_icon = (ImageView) itemView.findViewById(R.id.iv_device_icon);
            tv_name = (TextView) itemView.findViewById(R.id.tv_device_name);
            tv_status = (TextView) itemView.findViewById(R.id.iv_device_status);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void update(DeviceUiBean bean) {
            this.uiBean = bean;
            if (!TextUtils.isEmpty(bean.getIconUrl())) {
                Glide.with(mContext).load(bean.getIconUrl()).into(iv_icon);
            }

            tv_name.setText(bean.getName());

            String status = bean.getIsOnline() ? "online" : "offline";

            tv_status.setText(status);

            if (bean.getIsOnline()) {
                tv_status.setTextColor(Color.parseColor("#00ff00"));
            } else {
                tv_status.setTextColor(Color.parseColor("#ff0000"));
            }


        }

        @Override
        public void onClick(View v) {
            if (uiBean != null)
                listener.itemOnClick(uiBean.getDevId());
        }

        @Override
        public boolean onLongClick(View view) {
            if (uiBean != null)
                listener.itemOnLongClick(uiBean.getDevId());
            return true;
        }
    }

    public interface DeviceItemClickListener {
        void itemOnClick(String devId);

        void itemOnLongClick(String devId);
    }


}
