package org.ww.testapp.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.ww.testapp.R;
import org.ww.testapp.entity.Music;
import org.ww.testapp.player.MusicControlSender;
import org.ww.testapp.player.MusicService;
import org.ww.testapp.ui.widget.PlayPauseView;

public class MainActivity extends AppCompatActivity
{

    private static final int REQUEST_CODE = 1024;
    private ViewPager2 mainVp;
    private BottomNavigationView bottomNavigation;
    SectionsPagerAdaptor sectionsPagerAdaptor;
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestPermission();
        setContentView(R.layout.activity_main);
        initView();
        initService();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView()
    {
        mainVp = findViewById(R.id.mainVp);
        bottomNavigation = findViewById(R.id.mainNav);
        albumCover = findViewById(R.id.albumImageView);
        titleTextView = findViewById(R.id.titleTextView);
        artistTextView = findViewById(R.id.artistTextView);
        playPauseView = findViewById(R.id.playPauseView);
        sectionsPagerAdaptor = new SectionsPagerAdaptor(this);

        Music defaultMusic = new Music("<未播放>", "", "");
        new MusicUpdater(this, titleTextView, artistTextView, albumCover).updateMusicInfo(defaultMusic);

        mainVp.setAdapter(sectionsPagerAdaptor);
        mainVp.registerOnPageChangeCallback(pageChangeCallBack);
        //这里是bottomNavigationView的点击事件
        bottomNavigation.setOnItemSelectedListener(naviListener);
        // 绑定播放暂停键的点击事件
        playPauseView.setOnClickListener(playPauseListener);

    }

    private void initService()
    {
        // 绑定服务
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            isBound = true;

            // 观察播放状态
            musicService.getPlaybackState().observe(MainActivity.this, new Observer<MusicService.PlaybackInfo>() {
                @Override
                public void onChanged(MusicService.PlaybackInfo playbackInfo) {
                    updateUI(playbackInfo);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }


    private final ViewPager2.OnPageChangeCallback pageChangeCallBack = new ViewPager2.OnPageChangeCallback()
    {
        @Override
        public void onPageSelected(int position)
        {
            super.onPageSelected(position);
            // 根据 ViewPager2 的当前页面位置来设置 BottomNavigationView 的选中项
            switch (position)
            {
                case 0:
                    bottomNavigation.setSelectedItemId(R.id.action_discover);
                    break;
                case 1:
                    bottomNavigation.setSelectedItemId(R.id.action_rover);
                    break;
                case 2:
                    bottomNavigation.setSelectedItemId(R.id.action_my);
                    break;
            }
        }
    };

    private final BottomNavigationView.OnItemSelectedListener naviListener = new BottomNavigationView.OnItemSelectedListener()
    {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.action_discover:
                    mainVp.setCurrentItem(0);
                    break;
                case R.id.action_rover:
                    mainVp.setCurrentItem(1);
                    break;
                case R.id.action_my:
                    mainVp.setCurrentItem(2);
                    break;
            }
            return true;
        }
    };

    private final View.OnClickListener playPauseListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            // TODO: 完成播放暂停功能
            if (playPauseView.isPlaying())
            {
                // 发送暂停广播
                MusicControlSender.sendPauseBroadcast(MainActivity.this);
            }
            else
            {
                MusicControlSender.sendPlayBroadcast(MainActivity.this);
            }
        }
    };


    private void requestPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            // 先判断有没有权限
            if (Environment.isExternalStorageManager())
            {
//                writeFile();
            } else
            {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // 先判断有没有权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
//                writeFile();
            } else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        } else
        {
//            writeFile();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE)
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
//                writeFile();
            } else
            {
//                ToastUtils.show("存储权限获取失败");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            if (Environment.isExternalStorageManager())
            {
//                writeFile();
            } else
            {
//                ToastUtils.show("存储权限获取失败");
            }
        }
    }

}
