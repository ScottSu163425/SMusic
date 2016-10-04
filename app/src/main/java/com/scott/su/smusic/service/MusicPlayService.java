package com.scott.su.smusic.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;

import com.scott.su.smusic.R;
import com.scott.su.smusic.callback.MusicPlayCallback;
import com.scott.su.smusic.config.AppConfig;
import com.scott.su.smusic.constant.Constants;
import com.scott.su.smusic.constant.PlayMode;
import com.scott.su.smusic.constant.PlayStatus;
import com.scott.su.smusic.entity.LocalSongEntity;
import com.scott.su.smusic.mvp.model.impl.LocalAlbumModelImpl;
import com.scott.su.smusic.mvp.view.MusicPlayServiceView;
import com.scott.su.smusic.ui.activity.MainActivity;
import com.scott.su.smusic.util.MusicPlayUtil;
import com.su.scott.slibrary.util.TimeUtil;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/9/9.
 */
public class MusicPlayService extends Service implements MusicPlayServiceView {
    public static final String EXTRA_OPERATION_NOTIFICATION = "EXTRA_OPERATION_NOTIFICATION";
    public static final int OPERATION_NOTIFICATION_PLAY_NEXT = 1;
    public static final int OPERATION_NOTIFICATION_PLAY_PREVIOUS = 2;
    public static final int OPERATION_NOTIFICATION_PLAY_PAUSE = 3;

    public static final int ID_NOTIFICATION = 123;
    public static final long DURATION_TIMER_DELAY = TimeUtil.MILLISECONDS_OF_SECOND / 10;

    private MediaPlayer mMediaPlayer;
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private MusicPlayCallback mMusicPlayCallback;
    private LocalSongEntity mCurrentPlaySong;
    private boolean mPlaySameSong;
    private ArrayList<LocalSongEntity> mPlaySongs;

    private PlayStatus mCurrentPlayStatus = PlayStatus.Stop;
    private PlayMode mCurrentPlayMode = PlayMode.RepeatAll;
    private Handler mTimerHandler = new Handler();
    private Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMusicPlayCallback != null) {
                mMusicPlayCallback.onPlayProgressUpdate(mMediaPlayer.getCurrentPosition());
            }
            mTimerHandler.postDelayed(this, DURATION_TIMER_DELAY);
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MusicPlayServiceBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mMusicPlayCallback != null) {
                    mMusicPlayCallback.onPlayComplete();
                }
                playNext();
            }
        });

        mCurrentPlayMode = AppConfig.isPlayRepeatOne(this) ? PlayMode.RepeatOne
                : (AppConfig.isPlayRepeatAll(this) ? PlayMode.RepeatAll : PlayMode.Shuffle);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getIntExtra(EXTRA_OPERATION_NOTIFICATION, -1) == OPERATION_NOTIFICATION_PLAY_NEXT) {
            playNext();
        } else if (intent.getIntExtra(EXTRA_OPERATION_NOTIFICATION, -1) == OPERATION_NOTIFICATION_PLAY_PREVIOUS) {
            playPrevious();
        } else if (intent.getIntExtra(EXTRA_OPERATION_NOTIFICATION, -1) == OPERATION_NOTIFICATION_PLAY_PAUSE) {
            if (isPlaying()) {
                pause();
            } else {
                play();
            }
            updateNotifycation();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        super.onDestroy();
    }

    @Override
    public void setPlaySong(LocalSongEntity currentPlaySong, ArrayList<LocalSongEntity> playSongs) {
        if (mCurrentPlaySong == null) {
            mPlaySameSong = false;
        } else if (mCurrentPlaySong.getSongId() == currentPlaySong.getSongId()) {
            mPlaySameSong = true;
        } else {
            mPlaySameSong = false;
        }
        this.mCurrentPlaySong = currentPlaySong;
        this.mPlaySongs = playSongs;
    }

    @Override
    public void setPlayMode(PlayMode playMode) {
        this.mCurrentPlayMode = playMode;
    }

    @Override
    public void play() {
        if (mMediaPlayer != null && mCurrentPlaySong != null) {
            if (mPlaySameSong) {
                playResume();
            } else {
                //Play different song.
                playNew();
            }
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null && isPlaying()) {
            stopTimer();
            mMediaPlayer.pause();
            mCurrentPlayStatus = PlayStatus.Pause;
            mPlaySameSong = true;
            if (isRegisterCallback()) {
                mMusicPlayCallback.onPlayPause(mMediaPlayer.getCurrentPosition());
            }
            updateNotifycation();
        }
    }

    @Override
    public void seekTo(int position) {
        if (isPause()) {
            mMediaPlayer.seekTo(position);
        } else {
//            pause();
            mMediaPlayer.seekTo(position);
        }
        mMusicPlayCallback.onPlayProgressUpdate(mMediaPlayer.getCurrentPosition());
        playResume();
    }

    @Override
    public void playPrevious() {
        mCurrentPlaySong = MusicPlayUtil.getPreviousSong(mCurrentPlaySong, mPlaySongs, mCurrentPlayMode);
        playNew();
    }

    @Override
    public void playNext() {
        mCurrentPlaySong = MusicPlayUtil.getNextSong(mCurrentPlaySong, mPlaySongs, mCurrentPlayMode);
        playNew();
    }

    private void playResume() {
        if (isPause()) {
            startMediaPlayer();
            updateNotifycation();
        }
    }

    private void playNew() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(mCurrentPlaySong.getPath());
            mMediaPlayer.prepare();
            startMediaPlayer();
            if (mMusicPlayCallback != null) {
                mMusicPlayCallback.onPlaySongChanged(mCurrentPlaySong);
            }
            updateNotifycation();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int mCurrentRequestCode = 0;

    private void updateNotifycation() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(mCurrentPlaySong.getTitle());
        builder.setContentText(mCurrentPlaySong.getArtist());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeFile(new LocalAlbumModelImpl().getAlbumCoverPath(this, mCurrentPlaySong.getAlbumId())));
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        Intent intentGoToMusicPlay = new Intent(this, MainActivity.class);
        intentGoToMusicPlay.putExtra(Constants.KEY_IS_FROM_NOTIFICATION, true);
        intentGoToMusicPlay.putExtra(Constants.KEY_EXTRA_LOCAL_SONG, mCurrentPlaySong);
        intentGoToMusicPlay.putParcelableArrayListExtra(Constants.KEY_EXTRA_LOCAL_SONGS, mPlaySongs);
        intentGoToMusicPlay.addFlags(/*Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK
                |*/Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        PendingIntent pendingIntentGoToMusicPlay = PendingIntent.getActivity(this, mCurrentRequestCode, intentGoToMusicPlay, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPlayNext = new Intent(this, MusicPlayService.class);
        intentPlayNext.putExtra(EXTRA_OPERATION_NOTIFICATION, OPERATION_NOTIFICATION_PLAY_NEXT);
        PendingIntent pendingIntentPlayNext = PendingIntent.getService(this, OPERATION_NOTIFICATION_PLAY_NEXT, intentPlayNext, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPlayPrevious = new Intent(this, MusicPlayService.class);
        intentPlayPrevious.putExtra(EXTRA_OPERATION_NOTIFICATION, OPERATION_NOTIFICATION_PLAY_PREVIOUS);
        PendingIntent pendingIntentPlayPrevious = PendingIntent.getService(this, OPERATION_NOTIFICATION_PLAY_PREVIOUS, intentPlayPrevious, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPlayPause = new Intent(this, MusicPlayService.class);
        intentPlayPause.putExtra(EXTRA_OPERATION_NOTIFICATION, OPERATION_NOTIFICATION_PLAY_PAUSE);
        PendingIntent pendingIntentPlayPause = PendingIntent.getService(this, OPERATION_NOTIFICATION_PLAY_PAUSE, intentPlayPause, PendingIntent.FLAG_UPDATE_CURRENT);


        builder.setContentIntent(pendingIntentGoToMusicPlay);
        //第一个参数是图标资源id 第二个是图标显示的名称，第三个图标点击要启动的PendingIntent
        builder.addAction(R.drawable.ic_skip_previous_notification_36dp, "", pendingIntentPlayPrevious);
        builder.addAction(isPlaying() ? R.drawable.ic_pause_36dp : R.drawable.ic_play_arrow_notification_36dp, "",
                pendingIntentPlayPause);
        builder.addAction(R.drawable.ic_skip_next_notification_36dp, "", pendingIntentPlayNext);

        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();
        style.setMediaSession(new MediaSessionCompat(this, "MediaSession",
                new ComponentName(MusicPlayService.this, Intent.ACTION_MEDIA_BUTTON), null).getSessionToken());
        //CancelButton在5.0以下的机器有效
        style.setCancelButtonIntent(pendingIntentGoToMusicPlay);
        style.setShowCancelButton(true);
        //设置要现实在通知右方的图标 最多三个
        style.setShowActionsInCompactView(0, 1, 2);

        builder.setStyle(style);
        builder.setShowWhen(false);
        mNotification = builder.build();
        mNotificationManager.notify(ID_NOTIFICATION, mNotification);
        startForeground(ID_NOTIFICATION, mNotification);
        mCurrentRequestCode++;
    }

    private void startMediaPlayer() {
        mMediaPlayer.start();
        startTimer();
        mCurrentPlayStatus = PlayStatus.Playing;
        if (isRegisterCallback()) {
            mMusicPlayCallback.onPlayStart();
        }
    }

    private void startTimer() {
        stopTimer();
        mTimerHandler.post(mTimerRunnable);
    }

    private void stopTimer() {
        mTimerHandler.removeCallbacks(mTimerRunnable);
    }

    private boolean isRegisterCallback() {
        return mMusicPlayCallback != null;
    }

    private boolean isStop() {
        return mCurrentPlayStatus == PlayStatus.Stop;
    }

    private boolean isPlaying() {
        return mCurrentPlayStatus == PlayStatus.Playing;
    }

    private boolean isPause() {
        return mCurrentPlayStatus == PlayStatus.Pause;
    }

    @Override
    public PlayStatus getCurrentPlayStatus() {
        return mCurrentPlayStatus;
    }

    @Override
    public void registerPlayCallback(@NonNull MusicPlayCallback callback) {
        this.mMusicPlayCallback = callback;
    }

    @Override
    public void unregisterPlayCallback() {
        this.mMusicPlayCallback = null;
    }

    public class MusicPlayServiceBinder extends Binder implements MusicPlayServiceView {

        @Override
        public PlayStatus getCurrentPlayStatus() {
            return MusicPlayService.this.getCurrentPlayStatus();
        }

        @Override
        public void setPlaySong(LocalSongEntity currentPlaySong, ArrayList<LocalSongEntity> playSongs) {
            MusicPlayService.this.setPlaySong(currentPlaySong, playSongs);
        }

        @Override
        public void setPlayMode(PlayMode playMode) {
            MusicPlayService.this.setPlayMode(playMode);
        }

        @Override
        public void play() {
            MusicPlayService.this.play();
        }

        @Override
        public void pause() {
            MusicPlayService.this.pause();
        }

        @Override
        public void seekTo(int position) {
            MusicPlayService.this.seekTo(position);
        }

        @Override
        public void playPrevious() {
            MusicPlayService.this.playPrevious();
        }

        @Override
        public void playNext() {
            MusicPlayService.this.playNext();
        }

        @Override
        public void registerPlayCallback(@NonNull MusicPlayCallback callback) {
            MusicPlayService.this.registerPlayCallback(callback);
        }

        @Override
        public void unregisterPlayCallback() {
            MusicPlayService.this.unregisterPlayCallback();
        }
    }


}
