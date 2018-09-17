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
import java.util.List;

import static com.smart.tuya.meshdemo.adapter.MeshGroupDevListAdapter.InnerViewHolder.ADD_ACTION;
import static com.smart.tuya.meshdemo.adapter.MeshGroupDevListAdapter.InnerViewHolder.FOUND_ACTION;

/**
 * Created by zhusg on 2018/8/27.
 */

public class MeshGroupDevListAdapter extends RecyclerView.Adapter {
    private ArrayList<DeviceBean> mFoundData = new ArrayList<>();
    private ArrayList<DeviceBean> mAddData = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater;
    private OnClickSelectListener onClickSelectListener;

    private static final int HEAD_ADD = 1;
    private static final int HEAD_FOUND = 2;
    private static final int CONTENT = 3;

    public MeshGroupDevListAdapter(Context context, OnClickSelectListener listener) {
        this.mContext = context;
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.onClickSelectListener = listener;

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        if (viewType == CONTENT) {
            View convertView = mInflater.inflate(R.layout.bluemesh_list_item_mesh_group_device, parent, false);
            holder = new InnerViewHolder(convertView);
        }
        if (viewType == HEAD_ADD) {
            View convertView = mInflater.inflate(R.layout.bluemesh_recycle_item_grouplist_head, parent, false);
            HeadViewHolder headHolder = new HeadViewHolder(convertView);
            headHolder.initData("已添加");
            holder = headHolder;
        }

        if (viewType == HEAD_FOUND) {
            View convertView = mInflater.inflate(R.layout.bluemesh_recycle_item_grouplist_head, parent, false);
            HeadViewHolder headHolder = new HeadViewHolder(convertView);
            headHolder.initData("可添加");
            holder = headHolder;
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof InnerViewHolder) {
            InnerViewHolder innerViewHolder = (InnerViewHolder) holder;
            if (position <= mAddData.size()) {
                DeviceBean bean = mAddData.get(position - 1);
                innerViewHolder.initData(bean, ADD_ACTION);
            } else if (position >= mAddData.size() + 2) {
                DeviceBean bean = mFoundData.get(position - mAddData.size() - 2);
                innerViewHolder.initData(bean, FOUND_ACTION);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mFoundData.size() + mAddData.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return HEAD_ADD;
        if (position == mAddData.size() + 1)
            return HEAD_FOUND;
        return CONTENT;
    }

    public void setFoundData(List<DeviceBean> deviceBeanList) {
        mFoundData.clear();
        mFoundData.addAll(deviceBeanList);
        notifyDataSetChanged();
    }

    public void setAddData(List<DeviceBean> deviceBeanList) {
        mAddData.clear();
        mAddData.addAll(deviceBeanList);
        notifyDataSetChanged();
    }

    public class InnerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView selected;
        private ImageView deviceIcon;
        private TextView device;
        private TextView hint_status;
        private DeviceBean deviceBean;
        private int actionType;
        public static final int ADD_ACTION = 1;
        public static final int FOUND_ACTION = 2;

        public InnerViewHolder(View contentView) {
            super(contentView);
            selected = (ImageView) contentView.findViewById(R.id.iv_device_list_dot);
            deviceIcon = (ImageView) contentView.findViewById(R.id.iv_device_icon);
            device = (TextView) contentView.findViewById(R.id.tv_device);
            hint_status = (TextView) contentView.findViewById(R.id.tv_status);
            contentView.setOnClickListener(this);
        }

        public void initData(DeviceBean deviceBean, int type) {
            this.deviceBean = deviceBean;
            this.actionType = type;
            if (deviceBean.getIconUrl() == null || deviceBean.getIconUrl().isEmpty()) {
                Glide.with(mContext).load(R.drawable.bluemesh_icon_device_default).into(deviceIcon);
            } else {
                Glide.with(mContext).load(deviceBean.getIconUrl()).into(deviceIcon);
            }
            device.setText(deviceBean.getName());
            //|| (mTuyaBlueMesh.isCloudOnline() && deviceBean.getModuleMap().getBluetooth().getIsOnline()
            if (deviceBean.getIsOnline()) {
                selected.setVisibility(View.VISIBLE);
                hint_status.setVisibility(View.GONE);
                selected.setImageResource(type == ADD_ACTION
                        ? R.drawable.ty_group_selected : R.drawable.ty_group_un_select);
            } else {
                selected.setVisibility(View.GONE);
                hint_status.setVisibility(View.VISIBLE);
                hint_status.setText("无法管理");
            }
        }

        @Override
        public void onClick(View v) {
            onClickSelectListener.onClickSelect(actionType, deviceBean);
        }
    }

    public class HeadViewHolder extends RecyclerView.ViewHolder {

        private TextView mHeadTv;

        public HeadViewHolder(View contentView) {
            super(contentView);
            mHeadTv = (TextView) contentView.findViewById(R.id.tv_tip);
        }

        public void initData(String msg) {
            mHeadTv.setText(msg);
        }
    }

    public interface OnClickSelectListener {
        void onClickSelect(int actionType, DeviceBean bean);
    }

    public void onDestroy() {

    }


}

