package com.smart.tuya.meshdemo.presenter;

/**
 * @author aze
 * @Des
 * @date 2019-07-18.
 */
public interface IMeshListPresenter {
    void doOpenAll();

    void getStatusAll();

    void itemOnClick(String devId);

    void startClient();

    void stopClient();

    void onDestroy();

    void getDataFromServer();

    void doCloseAll();

    void itemOnLongClick(final String devId);
}
