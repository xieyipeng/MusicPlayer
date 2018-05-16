package com.example.musicplayer.Fragment;

import android.Manifest;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import com.example.musicplayer.R;
import com.example.musicplayer.adpter.SongAdapter;
import com.example.musicplayer.bean.Song;
import com.example.musicplayer.utils.AudioUtils;
import com.example.musicplayer.utils.RequestPermissions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * Created by 解奕鹏 on 2018/1/22.
 */

public class PlayLocalMusicFragment extends Fragment implements View.OnClickListener,SeekBar.OnSeekBarChangeListener{

    private RecyclerView mSongRecyclerView;
    private SeekBar mSeekBar;
    private ImageView mPlayModeImageView;
    private ImageView mSikpPreviousImageView;
    private ImageView mSikpNextImageView;
    private ImageView mPlayOrPauseImageView;

    private MediaPlayer mediaPlayer=new MediaPlayer();

    private int songIndex=-1;
    private int RANDOM_MODE=R.drawable.ic_random;
    private int QUEUE_MODE=R.drawable.ic_queue;
    private int REPEAD_MODE=R.drawable.ic_repeat_one;
    @DrawableRes
    private int playMode=RANDOM_MODE;

    private SongAdapter mAdapter;
    private List<Song> mSongList=new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter=new SongAdapter(getContext(),mSongList);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play_music,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSongRecyclerView=view.findViewById(R.id.song_recycler_view);
        mSeekBar=view.findViewById(R.id.seek_bar);
        mPlayModeImageView=view.findViewById(R.id.play_mode_image_view);
        mSikpNextImageView=view.findViewById(R.id.skip_next_image_view);
        mSikpPreviousImageView=view.findViewById(R.id.skip_previous_image_view);
        mPlayOrPauseImageView=view.findViewById(R.id.play_or_pause_image_view);

        RequestPermissions.requestPermissions(getActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                new RequestPermissions.OnPermissionsRequestListener() {
                    @Override
                    public void onGranted() {
                        loadLocalMusic();
                        initEvnt();
                    }

                    @Override
                    public void onDenied(List<String> deniedList) {

                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null!=mediaPlayer){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private void loadLocalMusic(){
        //不要在UI线程下耗时操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Song> songs = AudioUtils.getAllSongs(getContext());
                //不在非UI线程下更新UI
                //匿名内部类访问局部变量要加 final
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.addSongs(songs);
                    }
                });
            }
        }).start();
    }

    private void initEvnt(){
        mSongRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mSongRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new SongAdapter.OnItemClickListener() {
            @Override
            public void onClick(Song song) {
                playMusic(song);
            }
        });

        ChangePlayMode(playMode);

        mSikpPreviousImageView.setOnClickListener(this);
        mSikpNextImageView.setOnClickListener(this);
        mPlayOrPauseImageView.setOnClickListener(this);
        mPlayModeImageView.setOnClickListener(this);

        mSeekBar.setOnSeekBarChangeListener(this);

    }

    //ctrl+F12
    private void playMusic(Song song){
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getFileUrl());
            mediaPlayer.prepare();
            mediaPlayer.start();
            songIndex=mSongList.indexOf(song);

            mPlayOrPauseImageView.setImageResource(R.drawable.ic_pause);

            //设置播放 activity bar
            AppCompatActivity appCompatActivity=(AppCompatActivity)getActivity();
            appCompatActivity.getSupportActionBar().setTitle(song.getTitle());
            appCompatActivity.getSupportActionBar().setSubtitle(song.getSinger()+"-"+song.getAlbum());

            //设置seekBar的最大值
            mSeekBar.setMax(song.getDuration());

            //开始更新进度条
            new Thread(mRunnable).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        RequestPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.skip_next_image_view:
                PlayNextSong();
                break;
            case R.id.skip_previous_image_view:
                if (!mediaPlayer.isPlaying()){
                    return;
                }
                if (songIndex==0){
                    songIndex=mSongList.size()-1;
                    Toast.makeText(getActivity(), "最后一首", Toast.LENGTH_SHORT).show();
                }else {
                    songIndex--;
                    Toast.makeText(getActivity(), "上一首", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.play_mode_image_view:
                if (playMode==RANDOM_MODE){
                    playMode=QUEUE_MODE;
                    Toast.makeText(getActivity(), "列表循环", Toast.LENGTH_SHORT).show();
                }else {
                    if (playMode==QUEUE_MODE){
                        playMode=REPEAD_MODE;
                        Toast.makeText(getActivity(), "单曲循环", Toast.LENGTH_SHORT).show();
                    }else {
                        if (playMode==REPEAD_MODE){
                            playMode=RANDOM_MODE;
                            Toast.makeText(getActivity(), "随机播放", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                ChangePlayMode(playMode);
                break;
            case R.id.play_or_pause_image_view:
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    mPlayOrPauseImageView.setImageResource(R.drawable.ic_play);
                    //Toast.makeText(getActivity(), "暂停", Toast.LENGTH_SHORT).show();
                }else {
                    if (songIndex==-1&&mSongList.isEmpty()){
                        if (playMode==RANDOM_MODE){
                            songIndex=new Random().nextInt(mSongList.size());
                        }else {
                            songIndex=0;
                        }
                        playMusic(mSongList.get(songIndex));
                    }
                    mPlayOrPauseImageView.setImageResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                    //Toast.makeText(getActivity(), "播放", Toast.LENGTH_SHORT).show();
                    new Thread(mRunnable).start();
                }
                break;
        }
    }

    private void PlayNextSong() {
        if (!mediaPlayer.isPlaying()){
            return;
        }

        if (playMode== RANDOM_MODE){
            songIndex=new Random().nextInt(mSongList.size()-1);
            Toast.makeText(getActivity(), "随机下一首", Toast.LENGTH_SHORT).show();
        }else {
            if(songIndex==mSongList.size()-1){
                songIndex=0;
                Toast.makeText(getActivity(), "第一首", Toast.LENGTH_SHORT).show();
            }else {
                songIndex++;
                Toast.makeText(getActivity(), "下一首", Toast.LENGTH_SHORT).show();
            }
        }
        playMusic(mSongList.get(songIndex));
    }

    private void ChangePlayMode(final int playMode){
        if (playMode==REPEAD_MODE){
            mediaPlayer.setLooping(true);
        }
        if (playMode==RANDOM_MODE){
            mediaPlayer.setLooping(false);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playMusic(mSongList.get(new Random().nextInt(mSongList.size())));
                }
            });
        }
        if (playMode==QUEUE_MODE){
            mediaPlayer.setLooping(false);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playMusic(mSongList.get((songIndex+1)%mSongList.size()));
                }
            });
        }
        mPlayModeImageView.setImageResource(playMode);
    }

    //实时根据音乐时间更新进度条
    //子线程
    Runnable mRunnable=new Runnable() {
        @Override
        public void run() {
            while (mediaPlayer.isPlaying()) {
                mSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            }
        }
    };

    //seek bar 接口里的三个方法
    /**
     * 当进度更改的时候调用
     * @param seekBar
     * @param progress
     * @param fromUser
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    /**
     * 开始改变进度的时候调用
     * @param seekBar
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    /**
     * 结束拖拽进度时调用
     * @param seekBar
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(songIndex==-1){
            return;
        }
        if (seekBar.getProgress()==seekBar.getMax()){
            if (playMode!=REPEAD_MODE){
                PlayNextSong();
            }
            mSeekBar.setProgress(0);
        }
        mediaPlayer.seekTo(seekBar.getProgress());
        //改变进度后如果是终止状态则变更为播放状态
        if (!mediaPlayer.isPlaying()){
            mediaPlayer.start();
            mPlayOrPauseImageView.setImageResource(R.drawable.ic_pause);
            new Thread(mRunnable).start();
        }
    }
}