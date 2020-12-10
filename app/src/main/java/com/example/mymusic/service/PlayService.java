package com.example.mymusic.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import androidx.annotation.Nullable;
import android.util.Log;

import com.example.mymusic.model.Music;

import java.util.List;

/**
 * 音乐播放后台服务
 * Created by wcy on 2015/11/27.
 */
public class PlayService extends Service {
    private static final String TAG = "PlayService";
    private static final String ACTION_STOP = "me.wcy.music.ACTION_STOP";
    private MediaPlayer player;
    public class PlayBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: " + getClass().getSimpleName());
        AudioPlayer.get().init(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    public static void startCommand(Context context, String action) {
        Intent intent = new Intent(context, PlayService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_STOP:
                    stop();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    private void stop() {
        AudioPlayer.get().stopPlayer();
    }
}
