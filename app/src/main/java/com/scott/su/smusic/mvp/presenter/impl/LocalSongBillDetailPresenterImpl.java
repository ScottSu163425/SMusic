package com.scott.su.smusic.mvp.presenter.impl;

import com.scott.su.smusic.R;
import com.scott.su.smusic.entity.LocalSongBillEntity;
import com.scott.su.smusic.entity.LocalSongEntity;
import com.scott.su.smusic.mvp.model.AppConfigModel;
import com.scott.su.smusic.mvp.model.LocalSongBillModel;
import com.scott.su.smusic.mvp.model.LocalSongModel;
import com.scott.su.smusic.mvp.model.impl.AppConfigModelImpl;
import com.scott.su.smusic.mvp.model.impl.LocalSongBillModelImpl;
import com.scott.su.smusic.mvp.model.impl.LocalSongModelImpl;
import com.scott.su.smusic.mvp.presenter.LocalSongBillDetailPresenter;
import com.scott.su.smusic.mvp.view.LocalSongBillDetailView;

import java.util.List;

/**
 * Created by asus on 2016/8/29.
 */
public class LocalSongBillDetailPresenterImpl implements LocalSongBillDetailPresenter {
    private LocalSongBillDetailView mBillDetailView;
    private LocalSongBillModel mBillModel;
    private LocalSongModel mSongModel;
    private AppConfigModel mAppConfigModel;

    public LocalSongBillDetailPresenterImpl(LocalSongBillDetailView mBillDetailView) {
        this.mBillDetailView = mBillDetailView;
        this.mBillModel = new LocalSongBillModelImpl();
        this.mSongModel = new LocalSongModelImpl();
        this.mAppConfigModel = new AppConfigModelImpl();
    }

    @Override
    public void onAddSongsMenuClick() {
        mBillDetailView.goToLocalSongSelectionActivity();
    }

    @Override
    public void onSelectedLocalSongsResult(LocalSongBillEntity billToAddSong, List<LocalSongEntity> songsToAdd) {
        if (songsToAdd.size() == 1) {
            //Only select one song to add;
            LocalSongEntity songToAdd = songsToAdd.get(0);
            if (mBillModel.isBillContains(billToAddSong, songToAdd)) {
                mBillDetailView.showAddSongsUnsuccessfully(mBillDetailView.getViewContext().getString(R.string.error_already_exist));
                return;
            }
            mBillModel.addSongToBill(mBillDetailView.getViewContext(), songToAdd, billToAddSong);
            mBillDetailView.showAddSongsSuccessfully(mBillDetailView.getViewContext().getString(R.string.success_add_songs_to_bill));
            mBillDetailView.setBillEntity(mBillModel.getBill(mBillDetailView.getViewContext(), billToAddSong.getBillId()));
            loadCover();
            mBillDetailView.refreshBillSongDisplay(mBillModel.getBill(mBillDetailView.getViewContext(),
                    billToAddSong.getBillId()));
            //When back to main activity ,the bill display show be updated
            mAppConfigModel.setNeedToRefreshLocalBillDisplay(mBillDetailView.getViewContext(), true);
        } else {
            //More than one song was selected to be added into current bill;
            if (mBillModel.isBillContainsAll(billToAddSong, songsToAdd)) {
                mBillDetailView.showAddSongsUnsuccessfully(mBillDetailView.getViewContext().getString(R.string.error_already_exist));
                return;
            }
            mBillModel.addSongsToBill(mBillDetailView.getViewContext(), songsToAdd, billToAddSong);
            mBillDetailView.showAddSongsSuccessfully(mBillDetailView.getViewContext().getString(R.string.success_add_songs_to_bill));
            mBillDetailView.setBillEntity(mBillModel.getBill(mBillDetailView.getViewContext(), billToAddSong.getBillId()));
            loadCover();
            mBillDetailView.refreshBillSongDisplay(mBillModel.getBill(mBillDetailView.getViewContext(),
                    billToAddSong.getBillId()));
            mAppConfigModel.setNeedToRefreshLocalBillDisplay(mBillDetailView.getViewContext(), true);
        }
    }


    @Override
    public void onViewFirstTimeCreated() {
        mBillDetailView.initPreData();
        mBillDetailView.initToolbar();
        mBillDetailView.initView();
        mBillDetailView.initData();
        mBillDetailView.initListener();

        loadCover();

        if (mBillDetailView.getBillEntity().isBillEmpty()) {
            mBillDetailView.hideFab();
        } else {
            mBillDetailView.showFab();
        }
    }

    private void loadCover() {
        String path = mBillDetailView.getBillEntity().isBillEmpty() ? "" :
                mSongModel.getAlbumCoverPath(mBillDetailView.getViewContext(),
                        mBillDetailView.getBillEntity().getLatestSong().getAlbumId());
        mBillDetailView.loadCover(path);
    }

    @Override
    public void onViewResume() {

    }

    @Override
    public void onViewWillDestroy() {

    }


}