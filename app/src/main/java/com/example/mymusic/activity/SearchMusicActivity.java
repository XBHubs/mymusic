package com.example.mymusic.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
//import android.support.v7.app.AlertDialog;
import androidx.appcompat.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView;
//import android.support.v7.widget.SearchView;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.mymusic.R;
import com.example.mymusic.adapter.SearchMusicAdapter;
import com.example.mymusic.executor.DownloadMusic;
import com.example.mymusic.http.HttpCallback;
import com.example.mymusic.http.HttpClient;
import com.example.mymusic.model.DownloadInfo;
import com.example.mymusic.model.SearchMusic;
import com.example.mymusic.service.AudioPlayer;
import com.example.mymusic.util.FileUtils;
import com.example.mymusic.util.PermissionUtils;
import com.example.mymusic.util.ToastUtils;
import com.example.mymusic.util.ViewUtils;
import com.example.mymusic.util.ViewUtils.LoadStateEnum;
import com.example.mymusic.util.viewbind.Bind;
import com.example.mymusic.util.viewbind.ViewBinder;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/*
* 搜索音乐searchMusic
* 点击某项歌曲，会有下载选项（本地有，就直接播放；本地没有，就下载
* 下载音乐到本地，响应过程DownLoadSearchedMusic
* 显示下载过程  CustomProgressDialog
* */
public class SearchMusicActivity extends AppCompatActivity {
    @Bind(R.id.rv_search_result)
    private RecyclerView mMusicRecyclerView;

    @Bind(R.id.tv_loading)
    private TextView tvLoading;

    @Bind(R.id.tv_load_fail)
    private TextView tvLoadFail;

    private List<SearchMusic.Song> searchMusicList = new ArrayList<>();
    private SearchMusicAdapter mAdapter = new SearchMusicAdapter(searchMusicList);
    private Handler handler = new Handler(Looper.getMainLooper());

    public static Intent newIntent(Context context) {
        return new Intent(context, SearchMusicActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_music);
        ViewBinder.bind(this);
        //允许读写手机内存，没有改句，无法下载歌曲到手机里，也无法读入歌曲
        PermissionUtils.verifyStoragePermissions(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMusicRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMusicRecyclerView.setAdapter(mAdapter);//为listview设置适配器

        tvLoadFail.setText(R.string.search_empty);

        mAdapter.setOnItemClickListener(position -> {
            final SearchMusic.Song song = searchMusicList.get(position);//如果点击某首歌，转换成SearchMusic类
            //定义被点击歌曲的路径
            String path = FileUtils.getMusicDir() + FileUtils.getMp3FileName(song.getArtistname(), song.getSongname());
            File file = new File(path);
            //如果文件存在，添加到我的列表然后播放
            if (file.exists()) {
                AudioPlayer.get().addAndPlay(song);
            } else {
                //下载
                download(song, true);
            }
        });
        //设置菜单选项
        mAdapter.setOnMoreClickListener(position -> {
            String[] items = new String[]{"下载"};//仅有下载功能
            final SearchMusic.Song song = searchMusicList.get(position);
            //显示下载对话框
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(song.getSongname());
            //先查该文件是否存在
            String path = FileUtils.getMusicDir() + FileUtils.getMp3FileName(song.getArtistname(), song.getSongname());
            File file = new File(path);
            dialog.setItems(items, (dialog1, which) -> {
                if (file.exists()) {
                    ToastUtils.show("该歌曲已下载过");
                } else {
                    download(song, false);
                }
            });
            dialog.show();
        });
    }
    /*显示下载进程，并调用DownloadSearchedMusic*/
    private void download(final SearchMusic.Song song, boolean isPlayAfterDownload) {
        CustomProgressDialog progressDialog = new CustomProgressDialog(this);
        new DownloadSearchedMusic(this, song, isPlayAfterDownload) {
            @Override
            public void onPrepare() {
                progressDialog.showProgress();
            }

            @Override
            public void onExecuteSuccess(Void aVoid) {
                progressDialog.cancelProgress();
                ToastUtils.show(getString(R.string.now_download, song.getSongname()));
            }

            @Override
            public void onExecuteFail(Exception e) {
                progressDialog.cancelProgress();
                ToastUtils.show(R.string.unable_to_download);
            }
        }.execute();
    }
    /*点击搜索框，搜索*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_music, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.onActionViewExpanded();
        searchView.setQueryHint(getString(R.string.search_tips));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("SearchMusicActivity","进入搜索了");
                Log.d("SearchMusicActivity","输入的歌曲名叫做"+query);
                ViewUtils.changeViewState(mMusicRecyclerView, tvLoading, tvLoadFail, LoadStateEnum.LOADING);
                //查找音乐
                searchMusic(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                mPlaylistAdapter.getFilter().filter(newText);
                return false;
            }
        });
        //如果点了提交按钮
        searchView.setSubmitButtonEnabled(true);
        try {
            Field field = searchView.getClass().getDeclaredField("mGoButton");
            field.setAccessible(true);
            ImageView mGoButton = (ImageView) field.get(searchView);
            mGoButton.setImageResource(R.drawable.ic_menu_search);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }
//返回按钮
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //搜索音乐
    private void searchMusic(String keyword) {
        //Log.d("SearchMusicActivity:","进入searchMusic");
        HttpClient.searchMusic(keyword, new HttpCallback<SearchMusic>() {
            @Override
            public void onSuccess(SearchMusic response) {
                if (response == null || response.getSong() == null) {
                    //Log.d("SearchMusicActivity:","url内部:"+keyword);
                    //显示加载失败，没有找到歌曲
                    ViewUtils.changeViewState(mMusicRecyclerView, tvLoading, tvLoadFail, LoadStateEnum.LOAD_FAIL);
                    return;
                }
               //Log.d("SearchMusicActivity:","url:"+keyword);

                ViewUtils.changeViewState(mMusicRecyclerView, tvLoading, tvLoadFail, LoadStateEnum.LOAD_SUCCESS);
                searchMusicList.clear();
                //把获得的所有歌曲全部添加到链表searchMusicList
                searchMusicList.addAll(response.getSong());
                //重新设置适配器
                mAdapter.notifyDataSetChanged();
                mMusicRecyclerView.requestFocus();
                handler.post(() -> mMusicRecyclerView.scrollToPosition(0));
            }

            @Override
            public void onFail(Exception e) {
                ViewUtils.changeViewState(mMusicRecyclerView, tvLoading, tvLoadFail, LoadStateEnum.LOAD_FAIL);
            }
        });
    }

    public static abstract class DownloadSearchedMusic extends DownloadMusic {
        private SearchMusic.Song mSong;
        private boolean isPlayAfterDownload;//是否下载之后播放

        public DownloadSearchedMusic(Context context, SearchMusic.Song song, boolean isPlayAfterDownload) {
            super(context);
            mSong = song;
            this.isPlayAfterDownload = isPlayAfterDownload;
        }

        @Override
        protected void download() {
            final String artist = mSong.getArtistname();
            final String title = mSong.getSongname();

            // 获取歌曲下载链接
            HttpClient.getMusicDownloadInfo(mSong.getSongid(), new HttpCallback<DownloadInfo>() {
                @Override
                public void onSuccess(DownloadInfo response) {
                    //响应失败
                    if (response == null || response.getBitrate() == null) {
                        onFail(null);
                        return;
                    }
                    //响应成功，下载音乐，传入下载的链接url，和DownLoadMusic里参数对应
                    downloadMusic(response.getBitrate().getFile_link(), artist, title, null, isPlayAfterDownload);
                    onExecuteSuccess(null);
                }

                @Override
                public void onFail(Exception e) {
                    onExecuteFail(e);
                }
            });
        }
    }
    //下载进度条
    public static class CustomProgressDialog {
        private ProgressDialog progressDialog;
        private Context context;

        public CustomProgressDialog(Context context) {
            this.context = context;
        }

        public void showProgress() {
            showProgress(context.getString(R.string.loading));
        }

        public void showProgress(String message) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(context);
                progressDialog.setCancelable(false);
            }
            progressDialog.setMessage(message);
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        }

        public void cancelProgress() {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.cancel();
            }
        }
    }
}
