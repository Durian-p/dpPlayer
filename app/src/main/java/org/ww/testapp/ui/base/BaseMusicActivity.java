package org.ww.testapp.ui.base;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import org.ww.testapp.R;
import org.ww.testapp.entity.Music;
import org.ww.testapp.player.MusicControlSender;
import org.ww.testapp.player.MusicService;
import org.ww.testapp.ui.main.MusicUpdater;
import org.ww.testapp.ui.widget.PlayPauseView;

public abstract class BaseMusicActivity  extends AppCompatActivity
{
    private ImageView albumCover;
    private TextView titleTextView;
    private TextView artistTextView;
    private PlayPauseView playPauseView;
    private MusicService musicService;
    private boolean isBound = false;


    private void onSwipeRight() {
        // 切换到上一首歌
        // 获取上一首歌曲信息
        Music pm = musicService.getPrevMusic();
        Toast.makeText(this, pm.getTitle() + " - " + pm.getArtist(), Toast.LENGTH_SHORT).show();
        // 这里添加切换到上一首歌的逻辑
        MusicControlSender.sendPreviousBroadcast(this);
    }

    private void onSwipeLeft() {
        // 切换到下一首歌
        Music nm = musicService.getNextMusic();
        Toast.makeText(this, nm.getTitle() + " - " + nm.getArtist(), Toast.LENGTH_SHORT).show();
        // 这里添加切换到下一首歌的逻辑
        MusicControlSender.sendNextBroadcast(this);
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(getContentId());
        initBottomBar();
        initService();
    }

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
        // 例如更新播放按钮、显示歌曲信息等
        if (playbackInfo.state == MusicService.PlayerState.None)
        {
            // 播放按钮显示为默认状态
            Music defaultMusic = new Music("<未播放>", "", "");
            new MusicUpdater(this, titleTextView, artistTextView, albumCover).updateMusicInfo(defaultMusic);
        }
        else
        {
            // 播放按钮显示为暂停状态
            new MusicUpdater(this, titleTextView, artistTextView, albumCover).updateMusicInfo(playbackInfo.music);
            // TODO: 更新播放进度条列表状态等
            if (playbackInfo.state == MusicService.PlayerState.PLAYING)
            {
                // 播放按钮显示为播放状态
                playPauseView.play();
            }
            else
            {
                // 播放按钮显示为暂停状态
                playPauseView.pause();
            }
        }
    }

    private void initBottomBar()
    {
        albumCover = findViewById(R.id.albumImageView);
        titleTextView = findViewById(R.id.titleTextView);
        artistTextView = findViewById(R.id.artistTextView);
        playPauseView = findViewById(R.id.playPauseView);

        Music defaultMusic = new Music("<未播放>", "", "");
        new MusicUpdater(this, titleTextView, artistTextView, albumCover).updateMusicInfo(defaultMusic);
        // 绑定播放暂停键的点击事件
        playPauseView.setOnClickListener(playPauseListener);

    }

    protected final View.OnClickListener playPauseListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            // 播放暂停按钮点击事件
            if (musicService.getPlaybackState().getValue().state == MusicService.PlayerState.PLAYING)
            {
                // 暂停播放
                MusicControlSender.sendPauseBroadcast(BaseMusicActivity.this);
            }
            else
            {
                // 播放
                MusicControlSender.sendPlayBroadcast(BaseMusicActivity.this);
            }
        }
    };


    protected abstract int getContentId();

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }
}

