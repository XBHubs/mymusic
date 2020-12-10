package com.example.mymusic.adapter;

//import android.support.v7.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.mymusic.R;
import com.example.mymusic.model.Music;
import com.example.mymusic.service.AudioPlayer;
import com.example.mymusic.util.FileUtils;
import com.example.mymusic.util.viewbind.Bind;
import com.example.mymusic.util.viewbind.ViewBinder;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地音乐列表适配器
 * Created by wcy on 2015/11/27.
 */
public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> implements Filterable {
    private List<Music> musicList;
    private List<Music> mFilteredList;
    private OnItemClickListener onItemClickListener;
    private OnMoreClickListener onMoreClickListener;

    public PlaylistAdapter(List<Music> musicList) {
        this.musicList = musicList;
        this.mFilteredList = musicList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnMoreClickListener(OnMoreClickListener listener) {
        this.onMoreClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.view_holder_music, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Music music = mFilteredList.get(position);
        if (AudioPlayer.get().getMusicList().size() > 0) {
            Music currentMusic = AudioPlayer.get().getMusicList().get(AudioPlayer.get().getPlayPosition());
            holder.vPlaying.setVisibility(music.equals(currentMusic) ? View.VISIBLE : View.INVISIBLE);
        }
        holder.tvTitle.setText(music.getTitle());
        String artist = FileUtils.getArtistAndAlbum(music.getArtist(), music.getAlbum());
        holder.tvArtist.setText(artist);
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
        holder.ivMore.setOnClickListener(v -> {
            if (onMoreClickListener != null) {
                onMoreClickListener.onMoreClick(position);
            }
        });
        holder.vDivider.setVisibility(isShowDivider(position) ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<Music> getFilteredList() {
        return mFilteredList;
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            //定义过滤规则
            protected FilterResults performFiltering(CharSequence constraint) {//CharSequence用于定义字符串，它的值是可读可写的
                if (TextUtils.isEmpty(constraint)) {//判断输入的字符串是否为空
                    mFilteredList = musicList;
                } else {
                    mFilteredList = new ArrayList<>();
                    for (Music music :
                            musicList) {
                        //如果音乐列表中的标题或者歌手包含输入的字符串，就把该音乐加到过滤的链表里
                        if (music.getTitle().contains(constraint) || music.getArtist().contains(constraint)) {
                            mFilteredList.add(music);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = mFilteredList;//把过滤后的结果添加到results里
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values instanceof List) {
                    notifyDataSetChanged();
                }
            }
        };
    }

    private boolean isShowDivider(int position) {
        return position != mFilteredList.size() - 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.v_playing)
        private View vPlaying;
        @Bind(R.id.iv_cover)
        private ImageView ivCover;
        @Bind(R.id.tv_title)
        private TextView tvTitle;
        @Bind(R.id.tv_artist)
        private TextView tvArtist;
        @Bind(R.id.iv_more)
        private ImageView ivMore;
        @Bind(R.id.v_divider)
        private View vDivider;

        public ViewHolder(View view) {
            super(view);
            ViewBinder.bind(this, view);
        }
    }
}
