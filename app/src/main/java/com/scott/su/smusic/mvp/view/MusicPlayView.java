package com.scott.su.smusic.mvp.view;

import android.graphics.Bitmap;

import com.scott.su.smusic.constant.PlayMode;
import com.scott.su.smusic.entity.LocalSongEntity;
import com.su.scott.slibrary.view.BaseView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus on 2016/9/4.
 */
public interface MusicPlayView extends BaseView, MusicPlayServiceView {

    void showBillSelectionDialog(LocalSongEntity songEntity);

    void setCurrentPlayingSong(LocalSongEntity songEntity);

    void setPlayingMusicTitle(String title);

    void setPlayingMusicArtist(String artist);

    void setSeekBarMax(long max);

    void setSeekBarCurrentPosition(long currentPosition);

    void setTotalPlayTime(String totalPlayTime);

    void loadCover(String path, boolean needReveal);

    void loadBlurCover(Bitmap bitmap);

    boolean isPlayRepeatAll();

    boolean isPlayRepeatOne();

    boolean isPlayShuffle();

    void setPlayRepeatAll(boolean needAnim);

    void setPlayRepeatOne(boolean needAnim);

    void setPlayRepeatShuffle(boolean needAnim);

    PlayMode getCurrentPlayMode();

    boolean isMusicPlaying();

    boolean isMusicPause();

    boolean isMusicStop();

    void setPlayButtonPlaying();

    void setPlayButtonPause();

}
