package org.ww.testapp.ui.base;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.viewpager2.widget.ViewPager2;
import org.ww.testapp.R;
import org.ww.testapp.entity.Music;
import org.ww.testapp.player.MusicControlSender;
import org.ww.testapp.player.MusicService;
import org.ww.testapp.ui.player.PlayerActivity;
import org.ww.testapp.ui.widget.PlayPauseView;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseMusicActivity extends AppCompatActivity
{
    private ViewPager2 musicInfoViewPager;
    private PlayPauseView playPauseView;
    protected MusicService musicService;

    private boolean isBound = false;
    private MusicBarPagerAdapter adapter;
    private List<Music> musicList = new ArrayList<>();

    // ---control---
    private boolean updateService = true;

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
                    //MusicControlSender.sendPlayBroadcast(BaseMusicActivity.this);
                }
            }
        });


        playPauseView.setOnClickListener(playPauseListener);
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
            MusicControlSender.sendPauseBroadcast(BaseMusicActivity.this);
        }
    };

    private void updateUI(MusicService.PlaybackInfo playbackInfo)
    {
        // 根据播放状态更新 UI
        if (playbackInfo.state == MusicService.PlayerState.None)
        {
            // 显示默认信息
            Music defaultMusic = new Music("<未播放>", "", "");
            updateUIMusicList(new ArrayList<Music>()
            {{
                add(defaultMusic);
            }});
            playPauseView.pause();
        } else
        {
            // 更新播放信息
            updateUIMusicList(musicService.getMusicList());
            musicInfoViewPager.setCurrentItem(musicService.getCurrentMusicIndex(), false);

            if (playbackInfo.state == MusicService.PlayerState.PLAYING)
            {
                playPauseView.play();
            } else
            {
                playPauseView.pause();
            }
        }
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
                    MusicControlSender.sendPauseBroadcast(BaseMusicActivity.this);
                } else
                {
                    MusicControlSender.sendPlayBroadcast(BaseMusicActivity.this);
                }
            }
        }
    };

    // ---------------------控制MusicService-------------------------
    // 通知MusicService更新MusicList，之后MusicService会触发updateMusicListUi更新UI
    public void updateServiceMusicList(List<Music> newMusicList)
    {
        musicService.setPlaylist(newMusicList);
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
