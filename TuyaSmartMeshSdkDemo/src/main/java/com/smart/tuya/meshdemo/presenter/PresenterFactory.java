package com.smart.tuya.meshdemo.presenter;

import android.app.Activity;
import android.content.Context;

import com.smart.tuya.meshdemo.Config.MeshTypeConfig;
import com.smart.tuya.meshdemo.activity.MeshGroupDevListActivity;
import com.smart.tuya.meshdemo.presenter.bluemesh.BlueMeshControlPresenter;
import com.smart.tuya.meshdemo.presenter.bluemesh.BlueMeshGroupDeviceListPresenter;
import com.smart.tuya.meshdemo.presenter.bluemesh.BlueMeshGroupListPresenter;
import com.smart.tuya.meshdemo.presenter.bluemesh.BlueMeshListPresenter;
import com.smart.tuya.meshdemo.presenter.bluemesh.BlueMeshPresenter;
import com.smart.tuya.meshdemo.presenter.sigmesh.SigMeshControlPresenter;
import com.smart.tuya.meshdemo.presenter.sigmesh.SigMeshGroupDeviceListPresenter;
import com.smart.tuya.meshdemo.presenter.sigmesh.SigMeshGroupListPresenter;
import com.smart.tuya.meshdemo.presenter.sigmesh.SigMeshListPresenter;
import com.smart.tuya.meshdemo.presenter.sigmesh.SigMeshPresenter;
import com.smart.tuya.meshdemo.view.IControlView;
import com.smart.tuya.meshdemo.view.IMeshDemoView;
import com.smart.tuya.meshdemo.view.IMeshDeviceListView;
import com.smart.tuya.meshdemo.view.IMeshGroupDeviceListView;
import com.smart.tuya.meshdemo.view.IMeshGroupListView;

/**
 * @author aze
 * @Des
 * @date 2019-07-18.
 */
public class PresenterFactory {
    private PresenterFactory(){

    }

    public static IMeshListPresenter getMeshListPresenter(Activity context, IMeshDeviceListView view, long homeId, String meshId){
        if (MeshTypeConfig.getType()==MeshTypeConfig.TYPE_BLUEMESH){
            return new BlueMeshListPresenter(context,view,homeId,meshId);
        }else if (MeshTypeConfig.getType()==MeshTypeConfig.TYPE_SIGMESH){
            return new SigMeshListPresenter(context,view,homeId,meshId);
        }else {
            return null;
        }
    }

    public static IMeshPresenter getMeshPresenter(Activity context, IMeshDemoView meshDemoView, int meshType){
        MeshTypeConfig.setType(meshType);
        if (MeshTypeConfig.getType()==MeshTypeConfig.TYPE_BLUEMESH) {
            return new BlueMeshPresenter(context, meshDemoView);
        }else if(MeshTypeConfig.getType()==MeshTypeConfig.TYPE_SIGMESH)
                return new SigMeshPresenter(context,meshDemoView);
        else {
            return null;
        }
    }

    public static IMeshGroupDeviceListPresenter  getMeshGroupDeviceListPresenter(MeshGroupDevListActivity activity, IMeshGroupDeviceListView view){
        if (MeshTypeConfig.getType()==MeshTypeConfig.TYPE_BLUEMESH){
            return new BlueMeshGroupDeviceListPresenter(activity,view);
        }else if (MeshTypeConfig.getType()==MeshTypeConfig.TYPE_SIGMESH){
            return new SigMeshGroupDeviceListPresenter(activity,view);
        }else {
            return null;
        }
    }
    public static IMeshGroupListPresenter getMeshGroupListPresenter(Activity context, IMeshGroupListView view, long homeId, String meshId){
        if (MeshTypeConfig.getType()==MeshTypeConfig.TYPE_BLUEMESH){
            return new BlueMeshGroupListPresenter(context,view,homeId,meshId);
        }else if (MeshTypeConfig.getType()==MeshTypeConfig.TYPE_SIGMESH){
            return new SigMeshGroupListPresenter(context,view,homeId,meshId);
        }else {
            return null;
        }
    }
    public static IControlPresenter getControlPresenter(Context context, String devId, IControlView view){
        if (MeshTypeConfig.getType()==MeshTypeConfig.TYPE_BLUEMESH){
            return new BlueMeshControlPresenter(context,devId,view);
        }else if (MeshTypeConfig.getType()==MeshTypeConfig.TYPE_SIGMESH){
            return new SigMeshControlPresenter(context,devId,view);
        }else {
            return null;
        }
    }
}
