package com.example.mymusic.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
//import android.support.v4.app.Fragment;
//import android.support.v7.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mymusic.R;
import com.example.mymusic.executor.ControlPanel;
import com.example.mymusic.fragment.LocalMusicFragment;
import com.example.mymusic.fragment.PlayListFragment;
import com.example.mymusic.service.AudioPlayer;
import com.example.mymusic.service.PlayService;
import com.example.mymusic.util.viewbind.Bind;
import com.example.mymusic.util.viewbind.ViewBinder;

public class MusicMainActivity extends AppCompatActivity implements  View.OnClickListener{
    private PlayService playService;
    private ServiceConnection serviceConnection;

    private boolean isLocalListShown = false;
    private PlayListFragment mPlayListFragment;
    private LocalMusicFragment mLocalMusicFragment;
    private ControlPanel controlPanel;
    @Bind(R.id.fl_play_bar)
    private FrameLayout flPlayBar;
    @Bind(R.id.v_play_bar_playlist)
    private ImageView vPlayBarPlaylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_main);
        ViewBinder.bind(this);
        //开启服务
        bindService();
        switchFragment();

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fl_play_bar:
                // 进入播放器
                Log.d("MusicMainActivity","点击了下方");
                Toast.makeText(MusicMainActivity.this, "进入详情页面", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MusicMainActivity.this, PlayingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.bottom_in, R.anim.bottom_silent);
                break;
            default:
        }
    }
    //切换“本地歌曲”和“播放列表”
    private void switchFragment() {
        isLocalListShown = !isLocalListShown;
        Fragment fragment;
        int resId;
        if (isLocalListShown) {
            if (mLocalMusicFragment == null) {
                mLocalMusicFragment = new LocalMusicFragment();
            }
            fragment = mLocalMusicFragment;
            resId = R.drawable.ic_play_bar_btn_playlist;
        } else {
            if (mPlayListFragment == null) {
                mPlayListFragment = new PlayListFragment();
            }
            fragment = mPlayListFragment;
            resId = R.drawable.ic_play_bar_btn_locallist;
        }
        vPlayBarPlaylist.setImageResource(resId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void bindService() {
        Intent intent = new Intent();
        intent.setClass(this, PlayService.class);
        serviceConnection = new PlayServiceConnection();
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    //开启服务
    private void onServiceBound() {
        controlPanel = new ControlPanel(flPlayBar);
        vPlayBarPlaylist.setOnClickListener(v -> {
            switchFragment();
        });
        flPlayBar.setOnClickListener(this);
        AudioPlayer.get().addOnPlayEventListener(controlPanel);
        if (mLocalMusicFragment != null) {
            AudioPlayer.get().addOnPlayEventListener(mLocalMusicFragment);
        }
        if (mPlayListFragment != null) {
            AudioPlayer.get().addOnPlayEventListener(mPlayListFragment);
        }
    }
    //关闭服务
    private void onServiceUnbind() {
        AudioPlayer.get().removeOnPlayEventListener(controlPanel);
    }
    //建立连接
    private class PlayServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playService = ((PlayService.PlayBinder) service).getService();
            onServiceBound();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(getClass().getSimpleName(), "service disconnected");
            onServiceUnbind();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }
}
