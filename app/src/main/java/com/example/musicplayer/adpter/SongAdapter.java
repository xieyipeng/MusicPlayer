package com.example.musicplayer.adpter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.musicplayer.R;
import com.example.musicplayer.bean.Song;
import java.util.List;

/**
 *
 * Created by 解奕鹏 on 2018/1/22.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder>{

    private Context mContext;
    private List<Song> mSongList;

    /**
     * 构造函数
     * @param mContext 环境
     * @param mSongList Song
     */
    public SongAdapter(Context mContext, List<Song> mSongList) {
        this.mContext = mContext;
        this.mSongList = mSongList;
    }
    public void addSongs(List<Song> songs){
        mSongList.addAll(songs);
        notifyDataSetChanged();
    }
    //继承 RecyclerView.Adapter<SongAdapter.SongViewHolder> 而来的三个函数
    /**
     * 传数据给ViewHolder
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_song,parent,false);
        return new SongViewHolder(view);
    }
    /**
     * 绑定
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(SongViewHolder holder, int position) {
        holder.bindHolder(mSongList.get(position),position);
    }
    /**
     * 大小
     * @return
     */
    @Override
    public int getItemCount() {
        return mSongList.size();
    }
    //1、建立管理器
    public class SongViewHolder extends RecyclerView.ViewHolder {

        private TextView numberTextView;
        private TextView songNameTextView;
        private TextView infoTextView;
        private ImageView moreInfoImageView;

        public SongViewHolder(View itemView) {
            super(itemView);
            numberTextView = itemView.findViewById(R.id.number_text_view);
            songNameTextView = itemView.findViewById(R.id.song_name_text_view);
            infoTextView = itemView.findViewById(R.id.info_text_view);
            moreInfoImageView = itemView.findViewById(R.id.more_info_image_view);
        }
        //2、
        public void bindHolder(final Song song, int position) {
            numberTextView.setText((position + 1) + "");
            songNameTextView.setText(song.getTitle());
            String info = song.getSinger() + " - " + song.getAlbum();
            infoTextView.setText(info);
            /**
             * 点击三点水显示信息
             */
            moreInfoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog alertDialog=new AlertDialog.Builder(mContext)
                            .setTitle(song.getTitle())
                            .setMessage(song.toString())
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            });

            if(onItemClickListener!=null){
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.onClick(song);
                    }
                });
            }
        }
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        public void onClick(Song song);
    }
}