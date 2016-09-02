package com.scott.su.smusic.mvp.model;

import android.content.Context;

import com.scott.su.smusic.entity.LocalBillEntity;
import com.scott.su.smusic.entity.LocalSongEntity;

import java.util.List;

/**
 * Created by asus on 2016/8/19.
 */
public interface LocalBillModel {

    boolean isDefaultBill(LocalBillEntity billEntity);

    boolean isBillExist(Context context, LocalBillEntity billEntity);

    boolean isBillTitleExist(Context context, LocalBillEntity billEntity);

    void addBill(Context context, LocalBillEntity billEntity);

    List<LocalSongEntity> getBillSongs(Context context);

    LocalBillEntity getBill(Context context, long billId);

    LocalSongEntity getBillSong(Context context, long songId);

    List<LocalBillEntity> getBills(Context context);

    void addSongToBill(Context context, LocalSongEntity songEntity, LocalBillEntity billToAddSong);

    void addSongsToBill(Context context, List<LocalSongEntity> songEntities, LocalBillEntity billToAddSong);

    List<LocalSongEntity> getSongsByBillId(Context context, long billId);

    boolean isBillContainsSong(LocalBillEntity billEntity, LocalSongEntity songEntity);

    /**
     * Return true if bill contains all these songs;
     *
     * @param billEntity
     * @param songEntities
     * @return
     */
    boolean isBillContainsSongs(LocalBillEntity billEntity, List<LocalSongEntity> songEntities);

    void deleteBill(Context context, LocalBillEntity billEntity);

    void deleteBillSong(Context context, LocalBillEntity billEntity, LocalSongEntity songEntity);

    void clearBillSongs(Context context, LocalBillEntity billEntity);

}
