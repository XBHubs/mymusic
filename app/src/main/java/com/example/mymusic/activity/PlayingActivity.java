package com.example.mymusic.activity;


import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
//import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymusic.R;
import com.example.mymusic.model.Music;
import com.example.mymusic.service.AudioPlayer;
import com.example.mymusic.service.OnPlayerEventListener;
import com.example.mymusic.service.PlayService;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class PlayingActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView musicTitleView;
    private TextView musicArtistView;
    private ImageView btnPlayMode;
    private ImageView btnPlayPre;
    private ImageView btnPlayOrPause;
    private ImageView btnPlayNext;
    private SeekBar seekBar;
    public TextView nowTimeView;
    public TextView totalTimeView;
    MediaPlayer mediaPlayer = AudioPlayer.get().getMediaPlayer();
    private PlayService playService;
    private ServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
        //ViewBinder.bind(this);

        Log.d("PlayingActivity", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        //初始化
        initActivity();

        Log.d("PlayingActivity", "结束初始化");
        bindService();
    }

    private void initActivity() {
        Log.d("PlayingActivity", "进入初始化了");
        musicTitleView = findViewById(R.id.title);
        musicArtistView = findViewById(R.id.artist);
        btnPlayMode = findViewById(R.id.play_mode);
        btnPlayOrPause = findViewById(R.id.play_or_pause);
        btnPlayPre = findViewById(R.id.play_pre);
        btnPlayNext = findViewById(R.id.play_next);
        seekBar = findViewById(R.id.seekbar);
        nowTimeView = findViewById(R.id.current_time);
        totalTimeView = findViewById(R.id.total_time);

        // ToolBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setSupportActionBar(toolbar);

        // 设置监听
        btnPlayMode.setOnClickListener(this);
        btnPlayOrPause.setOnClickListener(this);
        btnPlayPre.setOnClickListener(this);
        btnPlayNext.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //拖动进度条时
                nowTimeView.setText(formatTime((long) progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AudioPlayer.get().seekTo(seekBar.getProgress());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }

    // 控件监听
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.play_mode:
                // 改变播放模式

                int mode = AudioPlayer.get().getPlayMode();
                switch (mode) {
                    case 11:
                        AudioPlayer.get().setPlayMode(22);
                        Toast.makeText(PlayingActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
                        btnPlayMode.setImageResource(R.drawable.ic_singlerecycler);
                        break;
                    case 22:
                        AudioPlayer.get().setPlayMode(33);
                        Toast.makeText(PlayingActivity.this, "随机播放", Toast.LENGTH_SHORT).show();
                        btnPlayMode.setImageResource(R.drawable.ic_random);
                        break;
                    case 33:
                        AudioPlayer.get().setPlayMode(11);
                        Toast.makeText(PlayingActivity.this, "列表循环", Toast.LENGTH_SHORT).show();
                        btnPlayMode.setImageResource(R.drawable.ic_playrecycler);
                        break;
                    default:
                }
                break;
            case R.id.play_pre:
                // 上一首
                AudioPlayer.get().prev();
                break;
            case R.id.play_next:
                // 下一首
                AudioPlayer.get().next();
                break;
            case R.id.play_or_pause:
                // 播放或暂停

                AudioPlayer.get().playPause();/*
                if(AudioPlayer.get().isPlaying())
                    btnPlayOrPause.setImageResource(R.drawable.ic_play);
                if(AudioPlayer.get().isPausing())
                    btnPlayOrPause.setImageResource(R.drawable.ic_pause);*/
                break;
            default:
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //格式化歌曲时间
    public static String formatTime(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
        Date data = new Date(time);
        return dateFormat.format(data);
    }
    //时刻更新进度条
    private void updateProgress() {
        // 使用Handler每间隔1s发送一次空消息，通知进度条更新
        Message msg = Message.obtain();// 获取一个现成的消息
        // 使用MediaPlayer获取当前播放时间除以总时间的进度
        int progress = mediaPlayer.getCurrentPosition();
        msg.arg1 = progress;
        mHandler.sendMessageDelayed(msg, 1000);
    }
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            // 展示给进度条和当前时间
            int progress = mediaPlayer.getCurrentPosition();
            seekBar.setProgress(progress);
            nowTimeView.setText(formatTime(progress));
            totalTimeView.setText(formatTime(mediaPlayer.getDuration()));//显示歌曲总时长
            // 继续定时发送数据
            updateProgress();
            return true;
        }
    });


    @Override
    public void finish() {
        super.finish();
        //界面退出时的动画
        overridePendingTransition(R.anim.bottom_silent,R.anim.bottom_out);
    }

    private void bindService() {
        Intent intent = new Intent();
        intent.setClass(this, PlayService.class);
        serviceConnection = new PlayServiceConnection();
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    //创建监听器
    private OnPlayerEventListener listener=new OnPlayerEventListener() {
        @Override
        public void onChange(Music music) {
            Log.d("PlayingActivity", "我在OnChange里");
            if (music == null) {
                return;
            }
            //切换音乐时，需要改变的标题、进度条设置
            musicTitleView.setText(music.getTitle());
            musicArtistView.setText(music.getArtist());
            seekBar.setMax((int) music.getDuration());
            seekBar.setProgress((int) AudioPlayer.get().getAudioPosition());
            updateProgress();
        }
        //播放开始和停止时，按钮的变化
        @Override
        public void onPlayerStart() {
            btnPlayOrPause.setImageResource(R.drawable.ic_pause);
        }

        @Override
        public void onPlayerPause() {
            btnPlayOrPause.setImageResource(R.drawable.ic_play);
        }
       //进度条的变化
        @Override
        public void onPublish(int progress) {
        }

        @Override
        public void onBufferingUpdate(int percent) {
        }
    };
    //开启服务
    private void onServiceBound() {
        Log.d("PlayingActivity", "服务开启");
        AudioPlayer.get().addOnPlayEventListener(listener);
        Log.d("PlayingActivity", "我在onserviceBound里");
        Music item = AudioPlayer.get().getPlayMusic();
        musicTitleView.setText(item.getTitle());
        musicArtistView.setText(item.getArtist());
        if(AudioPlayer.get().isPlaying())
            btnPlayOrPause.setImageResource(R.drawable.ic_pause);
        if(AudioPlayer.get().isPausing())
            btnPlayOrPause.setImageResource(R.drawable.ic_play);
        seekBar.setMax((int) item.getDuration());
        seekBar.setProgress((int) AudioPlayer.get().getAudioPosition());
        updateProgress();
    }
    //关闭服务
    private void onServiceUnbind() {
        AudioPlayer.get().removeOnPlayEventListener(listener);
    }
    //建立服务连接
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

}