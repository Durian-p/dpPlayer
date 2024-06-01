package org.ww.testapp.ui.player;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.viewpager2.widget.ViewPager2;
import org.ww.testapp.R;
import org.ww.testapp.entity.Music;
import org.ww.testapp.player.MusicService;
import org.ww.testapp.ui.player.fragment.CoverFragment;
import org.ww.testapp.ui.player.fragment.LyricFragment;
import org.ww.testapp.ui.widget.DepthPageTransformer;
import org.ww.testapp.ui.widget.PlayPauseView;
import org.ww.testapp.util.FormatUtil;

import java.util.ArrayList;
import java.util.List;

/* TODO:
 *   *** 收藏
 *   *** 播放模式
 *   ** 背景优化
 *   ** 歌词显示
 *
 */
public class PlayerActivity extends AppCompatActivity
{
    private Music playingMusic;
    private CoverFragment coverFragment;
    private LyricFragment lyricFragment;
    private List<Fragment> fragments = new ArrayList<>();

    private ImageView playModeIv;
    private ImageView collectIv;
    private SeekBar progressSb;
    private PlayPauseView playPauseIv;
    private ViewPager2 viewPager;
    private LinearLayout detailView;
    private ImageView searchLyricIv;
    private ImageView operateSongIv;
    private TextView progressTv;
    private TextView durationTv;
    private TextView titleTv;
    private TextView subTitleTv;
    private MusicService musicService;
    private LinearLayout bottomOpView;

    private CheckedTextView rightTv;
    private CheckedTextView leftTv;
    private ImageView backIv;
    private boolean isServiceBound = false;
    private boolean isCoverFragmentReady = false;
    private MusicService.PlaybackInfo pendingPlaybackInfo;

    private Handler progressHandler = new Handler();
    private Runnable progressRunnable;


    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            isServiceBound = true;

            LiveData<MusicService.PlaybackInfo> playbackInfo = musicService.getPlaybackState();
            playbackInfo.observe(PlayerActivity.this, new Observer<MusicService.PlaybackInfo>()
            {
                @Override
                public void onChanged(MusicService.PlaybackInfo playbackInfo)
                {
                    if (isCoverFragmentReady)
                    {
                        updateUI(playbackInfo);
                    } else
                    {
                        pendingPlaybackInfo = playbackInfo;
                    }
                }
            });
            // Start updating progress
            progressHandler.post(progressRunnable);

            if (playbackInfo.getValue().music != null)
            {
                titleTv.setText(playbackInfo.getValue().music.getTitle());
                subTitleTv.setText(playbackInfo.getValue().music.getArtist());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            musicService = null;
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_full);

        playingMusic = getIntent().getParcelableExtra("music", Music.class);

        initView();
        updatePlayMode();
        bindService();
        initData();
        initProgressRunnable();
    }

    private void initView()
    {
        playModeIv = findViewById(R.id.playModeIv);
        collectIv = findViewById(R.id.collectIv);
        progressSb = findViewById(R.id.progressSb);
        playPauseIv = findViewById(R.id.playPauseIv);
        viewPager = findViewById(R.id.viewPager);
        detailView = findViewById(R.id.detailView);
        searchLyricIv = findViewById(R.id.searchLyricIv);
        operateSongIv = findViewById(R.id.operateSongIv);
        progressTv = findViewById(R.id.progressTv);
        durationTv = findViewById(R.id.durationTv);
        bottomOpView = findViewById(R.id.bottomOpView);
        rightTv = findViewById(R.id.rightTv);
        leftTv = findViewById(R.id.leftTv);
        backIv = findViewById(R.id.backIv);
        titleTv = findViewById(R.id.titleTv);
        subTitleTv = findViewById(R.id.subTitleTv);

        setupViewPager(viewPager);

        detailView.setAnimation(moveToViewLocation());

        backIv.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                finish();
            }
        });

        playPauseIv.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (musicService != null)
                {
                    if (musicService.isPlaying())
                    {
                        musicService.pauseMedia();
                    } else
                    {
                        musicService.playMedia();
                    }
                }
            }
        });
    }

    private void setupViewPager(ViewPager2 viewPager)
    {
        fragments.clear();
        coverFragment = new CoverFragment();
        lyricFragment = new LyricFragment();
        fragments.add(coverFragment);
        fragments.add(lyricFragment);

        PlayerPagerAdapter mAdapter = new PlayerPagerAdapter(this, fragments);
        viewPager.setAdapter(mAdapter);
        viewPager.setPageTransformer(new DepthPageTransformer());
        viewPager.setOffscreenPageLimit(2);
        viewPager.setCurrentItem(0);

        int height = bottomOpView.getHeight();
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                if (positionOffset <= 1 && position == 0)
                {
                    if (bottomOpView.getVisibility() == View.GONE) {
                        bottomOpView.startAnimation(moveToViewLocation());
                        bottomOpView.setVisibility(View.VISIBLE);
                    }
                    detailView.setTranslationY(height * positionOffset);
                } else
                {
                    if (bottomOpView.getVisibility() == View.VISIBLE)
                    {
                        bottomOpView.startAnimation(moveToViewBottom());
                        bottomOpView.setVisibility(View.GONE);
                    }
                    detailView.setTranslationY(height * 1f);
                }
            }

            @Override
            public void onPageSelected(int position)
            {
                if (position == 0)
                {
                    searchLyricIv.setVisibility(View.GONE);
                    operateSongIv.setVisibility(View.VISIBLE);
                    lyricFragment.getLyricView().setIndicatorShow(false);
                    rightTv.setChecked(false);
                    leftTv.setChecked(true);
                } else
                {
                    searchLyricIv.setVisibility(View.VISIBLE);
                    operateSongIv.setVisibility(View.GONE);
                    rightTv.setChecked(true);
                    leftTv.setChecked(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
        });
    }

    private void initData()
    {
        if (playingMusic != null)
        {
            coverFragment.setPlayingMusic(playingMusic);
        }
    }


    private void initProgressRunnable()
    {
        progressRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                if (musicService != null && musicService.isPlaying())
                {
                    long currentPosition = musicService.getCurrentPosition();
                    long duration = musicService.getDuration();
                    updateProgress(currentPosition, duration);
                }
                // Schedule the next update after 0.5 second
                progressHandler.postDelayed(this, 500);
            }
        };
    }

    private void bindService()
    {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (isServiceBound)
        {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
        // Stop the progress handler
        progressHandler.removeCallbacks(progressRunnable);
    }

    private void updatePlayMode()
    {
        // UIUtils.updatePlayMode(playModeIv, false);
    }

    private void updateUI(MusicService.PlaybackInfo playbackInfo)
    {
        if (musicService != null)
        {
            boolean isPlaying = playbackInfo.state == MusicService.PlayerState.PLAYING;
            if (playbackInfo.music != null)
            {
                titleTv.setText(playbackInfo.music.getTitle());
                subTitleTv.setText(playbackInfo.music.getArtist());
            }
            updatePlayStatus(isPlaying);
            updateProgress(playbackInfo.currentPosition, playbackInfo.duration);
            coverFragment.setPlayingMusic(playbackInfo.music);
            coverFragment.updateViews();
        }
    }

    private void updatePlayStatus(boolean isPlaying)
    {
        if (isPlaying && ! playPauseIv.isPlaying())
        {
            playPauseIv.play();
            coverFragment.resumeRotateAnimation();
        } else if (! isPlaying && playPauseIv.isPlaying())
        {
            coverFragment.stopRotateAnimation();
            playPauseIv.pause();
        }
    }

    private void updateProgress(long progress, long max)
    {
        progressSb.setProgress((int) progress);
        progressSb.setMax((int) max);
        progressTv.setText(FormatUtil.formatTime(progress));
        durationTv.setText(FormatUtil.formatTime(max));
        lyricFragment.setCurrentTimeMillis(progress);
    }

    public void nextPlay(View view)
    {
        musicService.playNext();
    }

    public void prevPlay(View view)
    {
        musicService.playPrev();
    }

    public void changePlayMode(View view)
    {
        // UIUtils.updatePlayMode((ImageView) view, true);
    }

    public void openPlayQueue(View view)
    {
        // PlayQueueDialog.newInstance().showIt(this);
    }

    public void collectMusic(View view)
    {
        // UIUtils.collectMusic((ImageView) view, playingMusic);
    }

    public void addToPlaylist(View view)
    {
        // PlaylistManagerUtils.addToPlaylist(this, playingMusic);
    }

    public void showSongComment(View view)
    {
        // startActivity(new Intent(this, SongCommentActivity.class).putExtra("SONG", playingMusic));
    }

    public void shareMusic(View view)
    {
        // Tools.qqShare(this, musicService.getPlayingMusic());
    }

    public void downloadMusic(View view)
    {
        // QualitySelectDialog.newInstance(playingMusic).show(this);
    }


    // 底部上移动画
    private TranslateAnimation moveToViewLocation()
    {
        TranslateAnimation hiddenAction = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        hiddenAction.setDuration(300);
        return hiddenAction;
    }

    // 底部下降动画
// 底部下移动画
    private TranslateAnimation moveToViewBottom()
    {
        TranslateAnimation hiddenAction = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f);
        hiddenAction.setDuration(300);
        return hiddenAction;
    }


    public void notifyCoverFragmentReady()
    {
        isCoverFragmentReady = true;
        if (pendingPlaybackInfo != null)
        {
            updateUI(pendingPlaybackInfo);
            pendingPlaybackInfo = null;
        }
    }
}
