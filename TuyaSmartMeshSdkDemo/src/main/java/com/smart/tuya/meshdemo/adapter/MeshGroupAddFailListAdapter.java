package com.smart.tuya.meshdemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.smart.tuya.meshdemo.R;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;

/**
 * Created by zsg on 17/11/16.
 */

public class MeshGroupAddFailListAdapter extends RecyclerView.Adapter {
    private ArrayList<DeviceBean> datas;
    private Context mContext;
    private final LayoutInflater mInflater;

    public MeshGroupAddFailListAdapter(Context context) {
        this.mContext = context;
        datas=new ArrayList<>();
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public void setData(ArrayList<DeviceBean> list) {
        datas.clear();
        datas.addAll(list);
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = mInflater.inflate(R.layout.bluemesh_recycle_item_add_group_fail, parent, false);
        RecyclerView.ViewHolder holder = new MyViewHolder(convertView);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DeviceBean bean =  datas.get(position);
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        myViewHolder.initData(bean);

    }

    @Override
    public int getItemCount() {
        return datas.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder  {
        private ImageView deviceIcon;
        private TextView device;


        public MyViewHolder(View contentView) {
            super(contentView);
            deviceIcon = (ImageView) contentView.findViewById(R.id.iv_device_icon);
            device = (TextView) contentView.findViewById(R.id.tv_device);
        }

        public void initData(DeviceBean deviceBean) {

            if (deviceBean.getIconUrl() == null || deviceBean.getIconUrl().isEmpty()) {
                Glide.with(mContext).load(R.drawable.bluemesh_icon_device).into(deviceIcon);
            } else {
                Glide.with(mContext).load(deviceBean.getIconUrl()).into(deviceIcon);
            }
            device.setText(deviceBean.getName());

        }

    }
}
