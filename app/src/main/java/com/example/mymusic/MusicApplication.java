package com.example.mymusic;

import android.app.Application;
//import android.support.v4.util.LongSparseArray;
import android.os.Bundle;
import android.util.LongSparseArray;
import com.example.mymusic.model.Music;
import com.example.mymusic.preference.Preferences;
import com.example.mymusic.util.ToastUtils;

public class MusicApplication extends Application {
    private static MusicApplication sMusicApp;
    private final LongSparseArray<Music> mDownloadList = new LongSparseArray<>();
    private long idDownload2Play = -1;

    public static MusicApplication getsMusicApp() {
        return sMusicApp;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        sMusicApp = this;
        ToastUtils.init(this);
        Preferences.init(this);
    }

    public LongSparseArray<Music> getDownloadList() {
        return mDownloadList;
    }

    public long getIdDownload2Play() {
        return idDownload2Play;
    }

    public void setIdDownload2Play(long idDownload2Play) {
        this.idDownload2Play = idDownload2Play;
    }
}
