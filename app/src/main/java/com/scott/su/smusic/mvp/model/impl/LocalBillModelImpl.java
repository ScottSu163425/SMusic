package com.scott.su.smusic.mvp.model.impl;

import android.content.Context;
import android.text.TextUtils;

import com.scott.su.smusic.R;
import com.scott.su.smusic.entity.LocalBillEntity;
import com.scott.su.smusic.entity.LocalSongEntity;
import com.scott.su.smusic.mvp.model.LocalBillModel;
import com.su.scott.slibrary.manager.DbUtilHelper;
import com.su.scott.slibrary.util.L;

import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus on 2016/8/21.
 */
public class LocalBillModelImpl implements LocalBillModel {
    private static final long BILL_ID_DEFAULT_BILL = 1111111;


    @Override
    public boolean isDefaultBill(LocalBillEntity billEntity) {
        return billEntity.getBillId() == BILL_ID_DEFAULT_BILL;
    }

    @Override
    public boolean isBillExist(Context context, LocalBillEntity billEntity) {
        for (LocalBillEntity entity : getBills(context)) {
            if (entity.getBillId() == billEntity.getBillId()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int getBillPosition(Context context, LocalBillEntity billEntity) {
        if (!isBillExist(context, billEntity)) {
            return -1;
        }

        List<LocalBillEntity> bills = getBills(context);
        for (int i = 0; i < bills.size(); i++) {
            if (bills.get(i).getBillId() == billEntity.getBillId()) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public boolean isBillTitleExist(Context context, LocalBillEntity billEntity) {
        for (LocalBillEntity entity : getBills(context)) {
            if (entity.getBillTitle().equals(billEntity.getBillTitle())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addBill(Context context, LocalBillEntity billEntity) {
        try {
            DbUtilHelper.getDefaultDbManager().saveOrUpdate(billEntity);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<LocalSongEntity> getBillSongs(Context context) {
        List<LocalSongEntity> billSongs = new ArrayList<>();
        try {
            billSongs = DbUtilHelper.getDefaultDbManager().findAll(LocalSongEntity.class);
            if (billSongs == null) {
                billSongs = new ArrayList<>();
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
        return billSongs;
    }

    @Override
    public LocalBillEntity getBill(Context context, long billId) {
        List<LocalBillEntity> bills = getBills(context);
        LocalBillEntity bill = null;

        if (bills == null) {
            return null;
        }

        for (LocalBillEntity billEntity : bills) {
            if (billEntity.getBillId() == billId) {
                bill = billEntity;
                break;
            }
        }

        return bill;
    }

    @Override
    public LocalSongEntity getBillSong(Context context, long songId) {
        for (LocalSongEntity billSongEntity : getBillSongs(context)) {
            if (billSongEntity.getSongId() == songId) {
                return billSongEntity;
            }
        }
        return null;
    }

    @Override
    public List<LocalBillEntity> getBills(Context context) {
        List<LocalBillEntity> result = null;
        try {
            result = DbUtilHelper.getDefaultDbManager().findAll(LocalBillEntity.class);
            if (result != null && result.size() > 0) {
//                for (LocalBillEntity billEntity : result) {
//                    billEntity.setBillSongs(getSongsByBillId(context, billEntity.getBillId()));
//                }
            } else {
                //If it is the first time to open, create one bill automatically;
                result = new ArrayList<>();
                LocalBillEntity defaultBill = new LocalBillEntity();
                defaultBill.setBillTitle(context.getString(R.string.my_favourite));
                defaultBill.setBillId(BILL_ID_DEFAULT_BILL);
                addBill(context, defaultBill);
                result.add(defaultBill);
            }

        } catch (DbException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void addSongToBill(Context context, LocalSongEntity songEntity, LocalBillEntity billToAddSong) {
        LocalBillEntity bill = getBill(context, billToAddSong.getBillId());
        LocalSongEntity billSong = getBillSong(context, songEntity.getSongId());
        LocalSongEntity song = billSong == null ? songEntity : billSong;

        if (isBillContainsSong(bill, songEntity)) {
            //Already contain this song.
            return;
        }

        bill.appendBillSongId(song.getSongId());
        song.appendBillId(bill.getBillId());

        try {
            DbUtilHelper.getDefaultDbManager().saveOrUpdate(bill);
            DbUtilHelper.getDefaultDbManager().saveOrUpdate(song);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addSongsToBill(Context context, List<LocalSongEntity> songEntities, LocalBillEntity billToAddSong) {
        for (LocalSongEntity songEntity : songEntities) {
            addSongToBill(context, songEntity, billToAddSong);
        }
    }

    @Override
    public List<LocalSongEntity> getSongsByBillId(Context context, long billId) {
        List<LocalSongEntity> billSongs = getBillSongs(context);
        List<LocalSongEntity> result = new ArrayList<>();
        LocalBillEntity billEntity = getBill(context, billId);

        if (billSongs == null || billEntity==null||billEntity.isBillEmpty()) {
            return null;
        }

        //May produce wrong order of song.
//        for (LocalSongEntity songEntity : billSongs) {
//            if (!TextUtils.isEmpty(songEntity.getBillIds()) && songEntity.getBillIds().contains(billId + "")) {
//                result.add(songEntity);
//            }
//        }
        long[] billSongIds = billEntity.getBillSongIdsLongArray();
        for (long songId : billSongIds) {
            for (LocalSongEntity songEntity : billSongs) {
                if (songEntity.getSongId() == songId) {
                    result.add(songEntity);
                }
            }
        }

        return result;
    }

    @Override
    public boolean isBillContainsSong(LocalBillEntity billEntity, LocalSongEntity songEntity) {
        if (billEntity.isBillEmpty()) {
            return false;
        }

        return billEntity.getBillSongIds().contains(songEntity.getSongId() + "");
    }

    @Override
    public boolean isBillContainsSongs(LocalBillEntity billEntity, List<LocalSongEntity> songEntities) {
        for (LocalSongEntity songEntity : songEntities) {
            if (!isBillContainsSong(billEntity, songEntity)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void deleteBill(Context context, LocalBillEntity billEntity) {
        if (!isBillExist(context, billEntity)) {
            return;
        }

        try {
            DbUtilHelper.getDefaultDbManager().delete(billEntity);
            if (!billEntity.isBillEmpty()) {
                clearBillSongs(context, billEntity);
            }

        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteBillSong(Context context, LocalBillEntity billEntity, LocalSongEntity songEntity) {
        LocalSongEntity billSongEntity = getBillSong(context, songEntity.getSongId());
        if (!isBillContainsSong(billEntity, billSongEntity)) {
            return;
        }

        billSongEntity.removeBillId(billEntity.getBillId());
        billEntity.removeSongId(billSongEntity.getSongId());

        try {
            DbUtilHelper.getDefaultDbManager().saveOrUpdate(billSongEntity);
            DbUtilHelper.getDefaultDbManager().saveOrUpdate(billEntity);
            //optional
//            if (!billSongEntity.isBelongingToAnyBill()) {
//                DbUtilHelper.getDefaultDbManager().delete(billSongEntity);
//            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearBillSongs(Context context, LocalBillEntity billEntity) {
//        List<LocalSongEntity> billSongs = getBillSongs(context);
//
//        try {
//            for (LocalSongEntity songEntity : billSongs) {
//                if (isBillContainsSong(billEntity, songEntity)) {
//                    songEntity.removeBillId(billEntity.getBillId());
//                    DbUtilHelper.getDefaultDbManager().saveOrUpdate(songEntity);
//                    //Delete the song if the song doesn`t belong to any bill; optional
//                    if (!songEntity.isBelongingToAnyBill()) {
//                        DbUtilHelper.getDefaultDbManager().delete(songEntity);
//                    }
//                }
//            }
//            billEntity.clearSongId();
//            //Update the bill;
//            DbUtilHelper.getDefaultDbManager().saveOrUpdate(billEntity);
//        } catch (DbException e) {
//            e.printStackTrace();
//        }
        List<LocalSongEntity> billSongs = getBillSongs(context);
        for (LocalSongEntity songEntity : billSongs) {
            deleteBillSong(context, billEntity, songEntity);
        }
    }


}
