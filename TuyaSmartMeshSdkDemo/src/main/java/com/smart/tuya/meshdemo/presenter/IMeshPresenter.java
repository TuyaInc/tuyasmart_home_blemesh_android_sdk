package com.smart.tuya.meshdemo.presenter;

/**
 * @author aze
 * @Des 对外提供的一些的操作Mesh方法
 * @date 2019-07-16.
 */
public interface IMeshPresenter {
    void destroyMesh();

    void showCreateDialog(final long homeId);

    void showMeshList(long homeId, final int type);

    void showSearchList(final long homeId);

    void check();

    String getMeshName();

    String getMeshId();
}
