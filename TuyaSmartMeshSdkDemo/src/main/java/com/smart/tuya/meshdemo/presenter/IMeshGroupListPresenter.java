package com.smart.tuya.meshdemo.presenter;

/**
 * @author aze
 * @Des
 * @date 2019-07-18.
 */
public interface IMeshGroupListPresenter {
    void showDismissDialog(final long groupId);
    void getDataFromServer();
    void showCreateDialog();
}
