package org.ww.dpplayer.ui.base;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.viewpager2.widget.ViewPager2;
import org.ww.dpplayer.R;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.player.MusicServiceController;
import org.ww.dpplayer.player.MusicService;
import org.ww.dpplayer.ui.adapter.MusicBarPagerAdapter;
import org.ww.dpplayer.ui.dialog.DialogPlaylist;
import org.ww.dpplayer.ui.widget.PlayPauseView;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseMusicActivity extends AppCompatActivity
{
    protected ViewPager2 musicInfoViewPager;
    protected PlayPauseView playPauseView;
    protected ImageButton playListIb;
    protected MusicService musicService;

    private boolean isBound = false;
    Runnable progressRunnable;
    private Handler progressHandler = new Handler();
    private MusicBarPagerAdapter adapter;
    protected List<Music> musicList = new ArrayList<>();

    // ---control---
    private boolean updateService = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(getContentId());
        Intent intent = getIntent();
        updateService = intent.getBooleanExtra("updateService", true);
        initBottomBar();
        initService();
    }

    private void initBottomBar()
    {
        musicInfoViewPager = findViewById(R.id.musicInfoViewPager);
        playPauseView = findViewById(R.id.playPauseView);
        playListIb = findViewById(R.id.playListIb);
        adapter = new MusicBarPagerAdapter(musicList);
        musicInfoViewPager.setAdapter(adapter);

        musicInfoViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback()
        {
            @Override
            public void onPageSelected(int position)
            {
                super.onPageSelected(position);
                if (isBound && updateService)
                {
                    musicService.setCurrentMusic(position);
                    //MusicServiceController.sendPlayBroadcast(BaseMusicActivity.this);
                }else if (!updateService)
                {
                    updateService = true;
                }

            }
        });


        playPauseView.setOnClickListener(playPauseListener);

        playListIb.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                showMusicListDialog();
            }
        });
    }


    private void showMusicListDialog() {
        if (musicService != null && musicService.getPlayingMusicList() != null && musicService.getPlayingMusicList().size() > 0) {
            DialogPlaylist dialogPlaylist = new DialogPlaylist(musicService.getPlayingMusicList(), musicService.getCurrentMusic().getId());
            dialogPlaylist.show(getSupportFragmentManager(), "DialogPlaylist");
        }
    }


    // ----------------MusicService------------------
    protected void initService()
    {
        // 绑定服务
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    protected ServiceConnection connection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            isBound = true;

            // 观察播放状态
            musicService.getPlaybackState().observe(BaseMusicActivity.this, new Observer<MusicService.PlaybackInfo>()
            {
                @Override
                public void onChanged(MusicService.PlaybackInfo playbackInfo)
                {
                    updateUI(playbackInfo);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            isBound = false;
            MusicServiceController.sendPauseBroadcast(BaseMusicActivity.this);
        }
    };

    private void updateUI(MusicService.PlaybackInfo playbackInfo)
    {
        // 根据播放状态更新 UI
        if (playbackInfo.state == MusicService.PlayerState.None)
        {
            // 显示默认信息
            Music defaultMusic = new Music("[无歌曲播放中]", "[无歌曲播放中]", "");
            updateUIMusicList(new ArrayList<Music>()
            {{
                add(defaultMusic);
            }});
            playPauseView.pause();
        }
        else
        {
            // 更新播放信息
            updateUIMusicList(musicService.getPlayingMusicList());
            musicInfoViewPager.setCurrentItem(musicService.getCurrentMusicIndex(), false);

//            progressRunnable = new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    if (musicService != null && musicService.isPlaying())
//                    {
//                        long currentPosition = musicService.getCurrentProgress();
//                        long duration = musicService.getDuration();
//                        updateUIProgress(currentPosition, duration);
//                    }
//                    // Schedule the next update after 0.5 second
//                    progressHandler.postDelayed(this, 500);
//                }
//            };

            if (playbackInfo.state == MusicService.PlayerState.PLAYING)
            {
                playPauseView.play();
            } else
            {
                playPauseView.pause();
            }
        }
    }

    private void updateUIProgress(long progress, long max)
    {
//        playPauseView.setBgColor(Color.RED);
//        playPauseView.setBtnColor(Color.BLUE);
//        playPauseView.setProgress((float) progress / max);
    }
    // ----------------------------------------------

    protected final View.OnClickListener playPauseListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            // 播放暂停按钮点击事件
            if (isBound && musicService != null)
            {
                if (musicService.getPlaybackState().getValue().state == MusicService.PlayerState.PLAYING)
                {
                    MusicServiceController.sendPauseBroadcast(BaseMusicActivity.this);
                } else
                {
                    MusicServiceController.sendPlayBroadcast(BaseMusicActivity.this);
                }
            }
        }
    };

    // ---------------------控制MusicService-------------------------
    // 通知MusicService更新MusicList，之后MusicService会触发updateMusicListUi更新UI
    public void updateServiceMusicList(List<Music> newMusicList)
    {
        musicService.setOriginalPlaylist(newMusicList);
    }

    public void updateServiceMusicList(List<Music> newMusicList, int position)
    {
        musicService.setPlaylist(newMusicList, position);
    }

    // ---------------------------------------------------------------

    // MusicService通知更新MusicList 只更新UI
    @SuppressLint("NotifyDataSetChanged")
    public void updateUIMusicList(List<Music> newMusicList)
    {
        musicList.clear();
        musicList.addAll(newMusicList);
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setCurrentMusic(int position)
    {
        musicService.setCurrentMusic(position);
        adapter.notifyDataSetChanged();
    }

    public void setToNextPlay(Music music)
    {
        // 插入歌曲至下一个播放
        musicService.setToNextPlay(music);
        adapter.setToNext(music, musicService.getCurrentMusicIndex());
        adapter.notifyDataSetChanged();
    }

    public void setPlayMode(MusicService.PlayMode playMode)
    {
        musicService.setPlayMode(playMode);
    }

    protected abstract int getContentId();

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (isBound)
        {
            unbindService(connection);
            isBound = false;
        }
    }


}
