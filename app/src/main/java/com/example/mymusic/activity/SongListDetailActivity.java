package com.example.mymusic.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
import androidx.fragment.app.Fragment;
import com.example.mymusic.fragment.PlayListFragment;
import com.example.mymusic.model.SongList;

public class SongListDetailActivity extends SingleFragmentActivity {
    public static final String SONG_LIST = "songList";

    public static Intent newIntent(Context context, SongList songList) {
        Intent intent = new Intent(context, SongListDetailActivity.class);
        intent.putExtra(SONG_LIST, songList);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        SongList songList = (SongList) getIntent().getSerializableExtra(SONG_LIST);
        return PlayListFragment.newInstance(songList);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
