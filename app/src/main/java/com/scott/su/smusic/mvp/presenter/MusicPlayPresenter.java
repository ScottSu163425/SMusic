package com.scott.su.smusic.mvp.presenter;


import com.scott.su.smusic.callback.MusicPlayMainFragmentCallback;
import com.su.scott.slibrary.presenter.BasePresenter;

/**
 * Created by asus on 2016/9/4.
 */
public interface MusicPlayPresenter extends BasePresenter ,MusicPlayMainFragmentCallback{
    void onAddToBillMenuItemClick();
}
