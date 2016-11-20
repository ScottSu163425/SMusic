package com.scott.su.smusic.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceCallback;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.scott.su.smusic.R;
import com.scott.su.smusic.mvp.presenter.MusicPlaySecondPresenter;
import com.scott.su.smusic.mvp.presenter.impl.MusicPlaySecondPresenterImpl;
import com.scott.su.smusic.mvp.view.MusicPlaySecondView;
import com.su.scott.slibrary.fragment.BaseFragment;

/**
 * Created by asus on 2016/11/20.
 */

public class MusicPlaySecondFragment extends BaseFragment implements MusicPlaySecondView {
    private MusicPlaySecondPresenter mMusicPlaySecondPresenter;
    private AudioManager mAudioManager;
    private View mRootView;
    private AppCompatSeekBar mVolumeSeekBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_music_play_second, container, false);

        return mRootView;
    }

    @Override
    protected void onFirstTimeCreateView() {
        mMusicPlaySecondPresenter = new MusicPlaySecondPresenterImpl(this);
        mMusicPlaySecondPresenter.onViewFirstTimeCreated();
    }

    @Override
    public View getSnackbarParent() {
        return null;
    }

    @Override
    public void initPreData() {

    }

    @Override
    public void initView() {
        mVolumeSeekBar = (AppCompatSeekBar) mRootView.findViewById(R.id.seek_bar_volume_fragment_music_play_second);
    }

    @Override
    public void initData() {
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*5;
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)*5;

        mVolumeSeekBar.setMax(max);
        mVolumeSeekBar.setProgress(current);
    }

    @Override
    public void initListener() {
        registerVolumeReceiver();

        mVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress/5, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void registerVolumeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        getActivity().registerReceiver(mVolumeReceiver, filter);
    }

    private BroadcastReceiver mVolumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                int currVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);// 当前的媒体音量
                mVolumeSeekBar.setProgress(currVolume*5);
            }
        }
    };


}
