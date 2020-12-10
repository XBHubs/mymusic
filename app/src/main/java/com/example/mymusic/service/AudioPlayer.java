package com.example.mymusic.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.example.mymusic.database.PlaylistLab;
import com.example.mymusic.model.Music;
import com.example.mymusic.model.SearchMusic;
import com.example.mymusic.preference.Preferences;
import com.example.mymusic.util.MusicUtils;
import com.example.mymusic.util.ToastUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by hzwangchenyan on 2018/1/26.
 */
public class AudioPlayer extends Service {
    private static final String TAG = "AudioPlayer";
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_PAUSE = 3;

    private static final long TIME_UPDATE = 300L;
    private int playMode=11;  // 播放模式
    //播放模式
    public static final int TYPE_ORDER = 11;  //顺序播放
    public static final int TYPE_SINGLE = 22; //单曲循环
    public static final int TYPE_RANDOM = 33; //随机播放
    private Context context;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private List<Music> musicList;
    private final List<OnPlayerEventListener> listeners = new ArrayList<>();
    private int state = STATE_IDLE;


    public class AudioPlayerBinder extends Binder {
        public AudioPlayer getService() {
            return AudioPlayer.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return new AudioPlayerBinder();
    }
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();

        musicList.clear();
        listeners.clear();
        handler.removeMessages(66);
    }
    public static AudioPlayer get() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static AudioPlayer instance = new AudioPlayer();
    }

    private AudioPlayer() {
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
        musicList = PlaylistLab.get(context).getMusics();

        mediaPlayer = new MediaPlayer();
        handler = new Handler(Looper.getMainLooper());
        mediaPlayer.setOnCompletionListener(mp -> next());
        mediaPlayer.setOnPreparedListener(mp -> {
            if (isPreparing()) {
                startPlayer();
            }
        });
        mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
            for (OnPlayerEventListener listener : listeners) {
                listener.onBufferingUpdate(percent);
            }
        });
    }



    public void addOnPlayEventListener(OnPlayerEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeOnPlayEventListener(OnPlayerEventListener listener) {
        listeners.remove(listener);
    }

    public void addAndPlay(Music music) {
        int position = musicList.indexOf(music);
        if (position < 0) {
            musicList.add(music);
            PlaylistLab.get(context).addMusic(music);
            ToastUtils.show("已添加到播放列表");
            position = musicList.size() - 1;
        } else {
            ToastUtils.show("播放列表已存在该音乐");
        }
        play(position);
    }

    public void addAndPlay(SearchMusic.Song song) {
        addAndPlay(song.getSongname());
    }

    public void addAndPlay(String title) {
        Music music = MusicUtils.getMusic(context, title);
        if (music != null) {
            addAndPlay(music);
        } else {
            Log.d(TAG, "歌曲" + title + "不存在");
        }
    }

    public void play(int position) {
        if (musicList.isEmpty()) {
            return;
        }

        if (position < 0) {
            position = musicList.size() - 1;
        } else if (position >= musicList.size()) {
            position = 0;
        }

        setPlayPosition(position);
        Music music = getPlayMusic();

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(music.getPath());
            mediaPlayer.prepareAsync();
            state = STATE_PREPARING;
            for (OnPlayerEventListener listener : listeners) {
                listener.onChange(music);
            }
        } catch (IOException e) {
            e.printStackTrace();
            ToastUtils.show("当前歌曲无法播放");
            next();
        }
    }

    public void delete(int position) {
        int playPosition = getPlayPosition();
        Music music = musicList.remove(position);
        PlaylistLab.get(context).deleteMusic(music);
        if (playPosition > position) {
            setPlayPosition(playPosition - 1);
        } else if (playPosition == position) {
            if (isPlaying() || isPreparing()) {
                setPlayPosition(playPosition - 1);
                next();
            } else {
                stopPlayer();
                for (OnPlayerEventListener listener : listeners) {
                    listener.onChange(getPlayMusic());
                }
            }
        }
    }

    public void playPause() {
        if (isPreparing()) {
            stopPlayer();
        } else if (isPlaying()) {
            pausePlayer();
        } else if (isPausing()) {
            startPlayer();
        } else {
            play(getPlayPosition());
        }
    }

    public void startPlayer() {
        if (!isPreparing() && !isPausing()) {
            return;
        }

        mediaPlayer.start();
        state = STATE_PLAYING;
        handler.post(mPublishRunnable);

        for (OnPlayerEventListener listener : listeners) {
            listener.onPlayerStart();
        }
    }

    public void pausePlayer() {
        pausePlayer(true);
    }

    public void pausePlayer(boolean abandonAudioFocus) {
        if (!isPlaying()) {
            return;
        }

        mediaPlayer.pause();
        state = STATE_PAUSE;
        handler.removeCallbacks(mPublishRunnable);

        for (OnPlayerEventListener listener : listeners) {
            listener.onPlayerPause();
        }
    }

    public void stopPlayer() {
        if (isIdle()) {
            return;
        }

        pausePlayer();
        mediaPlayer.reset();
        state = STATE_IDLE;
    }

    public void next() {
        if (musicList.isEmpty()) {
            return;
        }



        switch (playMode) {
            case TYPE_RANDOM:
                play(new Random().nextInt(musicList.size()));
                break;
            case TYPE_SINGLE:
                play(getPlayPosition());
                break;
            case TYPE_ORDER:
            default:
                play(getPlayPosition() + 1);
                break;
        }
    }

    public void prev() {
        if (musicList.isEmpty()) {
            return;
        }

//
        switch (playMode) {
            case TYPE_RANDOM:
                play(new Random().nextInt(musicList.size()));
                break;
            case TYPE_SINGLE:
                play(getPlayPosition());
                break;
            case TYPE_ORDER:
            default:
                play(getPlayPosition() - 1);
                break;
        }
    }

    public void seekTo(int msec) {
        if (isPlaying() || isPausing()) {
            mediaPlayer.seekTo(msec);
            for (OnPlayerEventListener listener : listeners) {
                listener.onPublish(msec);
            }
        }
    }

    private Runnable mPublishRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying()) {
                for (OnPlayerEventListener listener : listeners) {
                    listener.onPublish(mediaPlayer.getCurrentPosition());
                }
            }
            handler.postDelayed(this, TIME_UPDATE);
        }
    };

    public int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }

    public long getAudioPosition() {
        if (isPlaying() || isPausing()) {
            return mediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    public Music getPlayMusic() {
        if (musicList.isEmpty()) {
            return null;
        }
        return musicList.get(getPlayPosition());
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public List<Music> getMusicList() {
        return musicList;
    }

    public boolean isPlaying() {
        return state == STATE_PLAYING;
    }

    public boolean isPausing() {
        return state == STATE_PAUSE;
    }

    public boolean isPreparing() {
        return state == STATE_PREPARING;
    }

    public boolean isIdle() {
        return state == STATE_IDLE;
    }

    public int getPlayPosition() {
        int position = Preferences.getPlayPosition();
        if (position < 0 || position >= musicList.size()) {
            position = 0;
            Preferences.savePlayPosition(position);
        }
        return position;
    }

    private void setPlayPosition(int position) {
        Preferences.savePlayPosition(position);
    }
    // 获取当前播放模式
    public int getPlayMode(){
        return getPlayModeInner();
    }

    // 设置播放模式
    public void setPlayMode(int mode){
        setPlayModeInner(mode);
    }
    private int getPlayModeInner(){
        return playMode;
    }

    private void setPlayModeInner(int mode){
        playMode = mode;
    }



}
