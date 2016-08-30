package com.scott.su.smusic.mvp.view;

import com.scott.su.smusic.entity.LocalSongBillEntity;
import com.su.scott.slibrary.view.BaseView;

/**
 * Created by asus on 2016/8/29.
 */
public interface LocalSongBillDetailView extends BaseView {

    LocalSongBillEntity getBillEntity();

    void setBillEntity(LocalSongBillEntity billEntity);

    void loadCover(String coverPath);

    void showFab();

    void hideFab();

    void goToLocalSongSelectionActivity();

    void showAddSongsUnsuccessfully(String msg);

    void showAddSongsSuccessfully(String msg);

    void refreshBillSongDisplay(LocalSongBillEntity billEntity);

}