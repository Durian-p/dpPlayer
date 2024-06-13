package org.ww.dpplayer.ui.player;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.viewpager2.widget.ViewPager2;
import jp.wasabeef.blurry.Blurry;
import me.wcy.lrcview.LrcView;
import net.steamcrafted.materialiconlib.MaterialIconView;
import org.ww.dpplayer.R;
import org.ww.dpplayer.database.MusicRepository;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.player.MusicService;
import org.ww.dpplayer.player.MusicServiceController;
import org.ww.dpplayer.ui.base.DialogAdd2Mlist;
import org.ww.dpplayer.ui.base.DialogPlaylist;
import org.ww.dpplayer.ui.player.fragment.CoverFragment;
import org.ww.dpplayer.ui.player.fragment.LyricFragment;
import org.ww.dpplayer.ui.widget.DepthPageTransformer;
import org.ww.dpplayer.ui.widget.PlayPauseView;
import org.ww.dpplayer.util.ColorUtil;
import org.ww.dpplayer.util.FormatUtil;
import org.ww.dpplayer.util.MusicLoader;
import org.ww.dpplayer.util.ShareUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlayerActivity extends AppCompatActivity
{
    private Music playingMusic;
    private CoverFragment coverFragment;
    private LyricFragment lyricFragment;
    private List<Fragment> fragments = new ArrayList<>();

    private ImageView playModeIv;
    private ImageView heartIv;
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
    private MaterialIconView playQueueIv;
    private ImageView playlistAddIv;
    private ImageView playingBgIv;
    private MaterialIconView shareIv;

    private boolean isServiceBound = false;
    private boolean isCoverFragmentReady = false;
    private MusicService.PlaybackInfo pendingPlaybackInfo;

    private Handler progressHandler = new Handler();
    private Runnable progressRunnable;
    MusicRepository musicRepository;

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
                lyricFragment.setLyricPath(playbackInfo.getValue().music.getPath());
                lyricFragment.showLyrics();
                lyricFragment.setListener(new LrcView.OnPlayClickListener()
                {
                    @Override
                    public boolean onPlayClick(LrcView view, long time)
                    {
                        musicService.seekTo(time);
//                        MusicServiceController.sendPlayBroadcast(PlayerActivity.this);
                        musicService.resumePlay();
                        return true;
                    }
                });
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
        hideBottomStatusBar();
        setContentView(R.layout.activity_player_full);

        playingMusic = getIntent().getParcelableExtra("music", Music.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//设置透明状态栏
        }

        initView();
        bindService();
        initData();
        initProgressRunnable();
    }

    private void initView()
    {
        playModeIv = findViewById(R.id.playModeIv);
        heartIv = findViewById(R.id.collectIv);
        progressSb = findViewById(R.id.progressSb);
        playPauseIv = findViewById(R.id.playPauseIv);
        viewPager = findViewById(R.id.viewPager);
        detailView = findViewById(R.id.detailView);
//        searchLyricIv = findViewById(R.id.searchLyricIv);
        operateSongIv = findViewById(R.id.operateSongIv);
        progressTv = findViewById(R.id.progressTv);
        durationTv = findViewById(R.id.durationTv);
        bottomOpView = findViewById(R.id.bottomOpView);
        rightTv = findViewById(R.id.rightTv);
        leftTv = findViewById(R.id.leftTv);
        backIv = findViewById(R.id.backIv);
        titleTv = findViewById(R.id.titleTv);
        subTitleTv = findViewById(R.id.subTitleTv);
        playQueueIv = findViewById(R.id.playQueueIv);
        playlistAddIv = findViewById(R.id.playlistAddIv);
        playingBgIv = findViewById(R.id.playingBgIv);
        shareIv = findViewById(R.id.shareIv);

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

        playModeIv.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (musicService != null)
                {
                    musicService.togglePlayMode();
                }
            }
        });

        playQueueIv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (musicService != null && musicService.getPlayingMusicList() != null && musicService.getPlayingMusicList().size() > 0)
                {
                    DialogPlaylist dialogPlaylist = new DialogPlaylist(musicService.getPlayingMusicList(), musicService.getCurrentMusic().getId());
                    dialogPlaylist.show(getSupportFragmentManager(), "DialogPlaylist");
                }
            }
        });

        progressSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if (fromUser && musicService != null)
                {
                    progressTv.setText(FormatUtil.formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                // 用户开始拖动进度条时可以暂停更新
                progressHandler.removeCallbacks(progressRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                if (musicService != null)
                {
                    musicService.seekTo(seekBar.getProgress());
                    progressHandler.post(progressRunnable); // 用户停止拖动进度条后继续更新
                }
            }
        });

        musicRepository = MusicRepository.getInstance();
        if (musicRepository.getHeartMusicBySongId(playingMusic.getId()) != null)
        {
            heartIv.setImageResource(R.drawable.item_heart_fill);
        } else
        {
            heartIv.setImageResource(R.drawable.item_heart);
        }
        heartIv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                boolean isHeart = musicRepository.toggleHeartSong(playingMusic);
                if (isHeart)
                {
                    heartIv.setImageResource(R.drawable.item_heart_fill);
                } else
                {
                    heartIv.setImageResource(R.drawable.item_heart);
                }
            }
        });

        // 添加歌单
        playlistAddIv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DialogAdd2Mlist dialogAdd2Mlist = new DialogAdd2Mlist(PlayerActivity.this, playingMusic);
                dialogAdd2Mlist.show(getSupportFragmentManager(), "DialogAdd2Mlist");
            }
        });

        // 分享
        shareIv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    File file = new File(playingMusic.getPath());
                    Uri path;
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    {
                        path = FileProvider.getUriForFile(PlayerActivity.this, ShareUtil.getAuthority(), file);
                    } else
                    {
                        path = Uri.fromFile(file);
                    }
                    //注意intent用addFlags 如果intent在这行代码下使用setFlags会导致其他应用没有权限打开你的文件
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    String type = ShareUtil.getMimeType(file);
                    intent.setDataAndType(path, type);
                    //如果想让用户每次打开文件都自己选择（方便切换应用打开）加上下面这句代码
                    intent = Intent.createChooser(intent, "请选择打开此文件的应用");
                    startActivity(intent);


                }
                catch (ActivityNotFoundException e)
                {
                    Toast.makeText(PlayerActivity.this, "此设备没有可以打开此文件的软件", Toast.LENGTH_SHORT).show();

                }
            }
        });

        // 动态颜色设置
        setDynamicColor();

    }

    private void setDynamicColor()
    {
        // 设置背景
        if (playingMusic.getAlbumArt() == null)
            playingMusic.setAlbumArt(MusicLoader.getAlbumArt(playingMusic.getPath())) ;
//        setGradientBackground(playingBgIv, playingMusic.getAlbumArt());
        if (playingMusic.getAlbumArt() != null)
        {
            Blurry.with(this).radius(20).color(Color.argb(60, 0, 0, 0)).from(playingMusic.getAlbumArt()).into(playingBgIv);
            getWindow().setStatusBarColor(ColorUtil.getAverageColorWithBlackOverlay(playingMusic.getAlbumArt(), true));
            // 设置进度条播放按钮
            int themeColor = ColorUtil.getAverageColor(playingMusic.getAlbumArt());
            progressSb.setThumbTintList(ColorStateList.valueOf(themeColor));
            playPauseIv.setBgColor(themeColor);
        }
        else
        {
            Blurry.with(this).radius(20).color(Color.argb(60, 0, 0, 0)).from(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.banner, null)).getBitmap()).into(playingBgIv);
            int themeColor = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null);
            progressSb.setThumbTintList(ColorStateList.valueOf(themeColor));
            playPauseIv.setBgColor(themeColor);
        }

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

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                if (positionOffset < 1 && position == 0)
                {
                    detailView.setTranslationY(bottomOpView.getHeight() * positionOffset);
                }
                else
                {
                    detailView.setTranslationY(bottomOpView.getHeight() * 1f);
                }
            }

            @Override
            public void onPageSelected(int position)
            {
                if (position == 0)
                {
//                    searchLyricIv.setVisibility(View.GONE);
//                    operateSongIv.setVisibility(View.VISIBLE);
//                    lyricFragment.getLyricView().setIndicatorShow(false);
//                    rightTv.setChecked(false);
//                    leftTv.setChecked(true);
                } else
                {
//                    searchLyricIv.setVisibility(View.VISIBLE);
//                    operateSongIv.setVisibility(View.GONE);
//                    rightTv.setChecked(true);
//                    leftTv.setChecked(false);
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
                    long currentPosition = musicService.getCurrentProgress();
                    long duration = musicService.getDuration();
                    updateUIProgress(currentPosition, duration);
                    lyricFragment.setCurrentTimeMilles(currentPosition);
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

    private void updateUIPlayMode()
    {
        MusicService.PlayMode playMode = musicService.getPlaybackState().getValue().playMode;
        if (playMode != null)
        {
            if (playMode == MusicService.PlayMode.SEQUENTIAL)
            {
                playModeIv.setImageResource(R.drawable.ic_repeat);
            } else if (playMode == MusicService.PlayMode.REPEAT_ONE)
            {
                playModeIv.setImageResource(R.drawable.ic_repeat_one);
            } else if (playMode == MusicService.PlayMode.SHUFFLE)
            {
                playModeIv.setImageResource(R.drawable.ic_shuffle);
            }
        }
    }


    private void updateUI(MusicService.PlaybackInfo playbackInfo)
    {
        if (musicService != null)
        {
            boolean isPlaying = playbackInfo.state == MusicService.PlayerState.PLAYING;
            if (playbackInfo.music != null)
            {
                if (! Objects.equals(playbackInfo.music.getId(), playingMusic.getId()))
                {
                    playingMusic = playbackInfo.music;
                    titleTv.setText(playbackInfo.music.getTitle());
                    subTitleTv.setText(playbackInfo.music.getArtist());
                    if (musicRepository.getHeartMusicBySongId(playingMusic.getId()) != null)
                    {
                        heartIv.setImageResource(R.drawable.item_heart_fill);
                    } else
                    {
                        heartIv.setImageResource(R.drawable.item_heart);
                    }
                    lyricFragment.setLyricPath(playbackInfo.music.getPath());
                    lyricFragment.showLyrics();

                }
            }
            updateUIPlayStatus(isPlaying);
            updateUIProgress(playbackInfo.currentPosition, playbackInfo.duration);
            updateUIPlayMode();
            setDynamicColor();
            coverFragment.setPlayingMusic(playbackInfo.music);
            coverFragment.updateViews();
        }
    }

    private void updateUIPlayStatus(boolean isPlaying)
    {
        if (isPlaying)
        {
            if (! playPauseIv.isPlaying())
                playPauseIv.play();
            coverFragment.resumeRotateAnimation();
        } else
        {
            if (playPauseIv.isPlaying())
                playPauseIv.pause();
            coverFragment.stopRotateAnimation();
        }
    }

    private void updateUIProgress(long progress, long max)
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


    public void notifyCoverFragmentReady()
    {
        isCoverFragmentReady = true;
        if (pendingPlaybackInfo != null)
        {
            updateUI(pendingPlaybackInfo);
            pendingPlaybackInfo = null;
        }
    }

    /**
     * 隐藏 NavigationBar和StatusBar
     */
    protected void hideBottomStatusBar() {
        //隐藏虚拟按键，并且全屏
//        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
//            View v = this.getWindow().getDecorView();
//            v.setSystemUiVisibility(View.GONE);
//        } else if (Build.VERSION.SDK_INT >= 19) {
//            //for new api versions.
//            View decorView = getWindow().getDecorView();
//            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY ;
//            decorView.setSystemUiVisibility(uiOptions);
//
//        }
        Window window = getWindow();
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (windowInsetsController == null) {
            return;
        }
// Hide the system bars.
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());

    }

}
