package com.example.mymusic.http;

import androidx.annotation.NonNull;
import android.util.Log;

import com.example.mymusic.model.DownloadInfo;
import com.example.mymusic.model.SearchMusic;
import com.zhy.http.okhttp.OkHttpUtils;
import okhttp3.Call;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class HttpClient {
    private static final String BASE_URL = "http://tingapi.ting.baidu.com/v1/restserver/ting";
    private static final String METHOD_SEARCH_MUSIC = "baidu.ting.search.catalogSug";
    private static final String METHOD_DOWNLOAD_MUSIC = "baidu.ting.song.play";
    private static final String PARAM_METHOD = "method";
    private static final String PARAM_QUERY = "query";
    private static final String PARAM_SONG_ID = "songid";

    static {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(new HttpInterceptor())
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }

    public static void searchMusic(String keyword, @NonNull final HttpCallback<SearchMusic> callback) {
        Log.d("HttpClient:","进入searchMusic");
        OkHttpUtils.get().url(BASE_URL)
                .addParams(PARAM_METHOD, METHOD_SEARCH_MUSIC)
                .addParams(PARAM_QUERY, keyword)
                .build()
                .execute(new JsonCallback<SearchMusic>(SearchMusic.class) {
                    @Override
                    public void onResponse(SearchMusic response, int id) {
                        Log.d("SearchMusicActivity:","url:"+OkHttpUtils.get());
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }

    public static void getMusicDownloadInfo(String songId, @NonNull final HttpCallback<DownloadInfo> callback) {
        //根据歌曲的id获得歌曲的详细信息，url=http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.song.play&songid=
        OkHttpUtils.get().url(BASE_URL)
                .addParams(PARAM_METHOD, METHOD_DOWNLOAD_MUSIC)
                .addParams(PARAM_SONG_ID, songId)
                .build()
                .execute(new JsonCallback<DownloadInfo>(DownloadInfo.class) {
                    @Override
                    public void onResponse(DownloadInfo response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }
}
