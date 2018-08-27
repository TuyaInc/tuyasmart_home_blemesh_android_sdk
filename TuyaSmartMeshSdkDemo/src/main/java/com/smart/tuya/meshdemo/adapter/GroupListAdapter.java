package com.smart.tuya.meshdemo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.smart.tuya.meshdemo.R;
import com.smart.tuya.meshdemo.bean.DeviceUiBean;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by zsg on 17/8/1.
 */

public class GroupListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private LayoutInflater mInflater;
    private GroupItemClickListener listener;
    private ArrayList<DeviceUiBean> datas = new ArrayList<>();


    public GroupListAdapter(Context context, GroupItemClickListener listener) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.group_item, parent, false);
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
        TextView tv_name;
        DeviceUiBean uiBean;

        public DeviceViewHolder(View itemView) {
            super(itemView);
            tv_name = (TextView) itemView.findViewById(R.id.tv_device_name);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void update(DeviceUiBean bean) {
            this.uiBean = bean;

            tv_name.setText(bean.getName());

            String status = bean.getIsOnline() ? "online" : "offline";


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

    public interface GroupItemClickListener {
        void itemOnClick(String groupId);

        void itemOnLongClick(String groupId);
    }


}
