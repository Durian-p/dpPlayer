package org.ww.dpplayer.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.ww.dpplayer.R;
import org.ww.dpplayer.database.MusicRepository;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.player.MusicService;
import org.ww.dpplayer.player.MusicServiceController;
import org.ww.dpplayer.ui.adapter.MainPagerAdaptor;
import org.ww.dpplayer.ui.base.BaseMusicActivity;
import org.ww.dpplayer.ui.player.PlayerActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends BaseMusicActivity {

    private static final int REQUEST_CODE = 1024;
    private static final int REQUEST_CODE_NOTIFICATION = 1001;
    private ViewPager2 mainVp;
    private BottomNavigationView bottomNavigation;
    MainPagerAdaptor mainPagerAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideBottomStatusBar();
        requestStoragePermission();
        requestNotificationPermission();
        MusicRepository.initInstance(this);
        initView();
        startMusicService(); // 启动MusicService
    }

    private void startMusicService() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
        startService(intent);
    }

    private void initView() {
        mainVp = findViewById(R.id.mainVp);
        bottomNavigation = findViewById(R.id.mainNav);
        mainPagerAdaptor = new MainPagerAdaptor(this);
        mainVp.setAdapter(mainPagerAdaptor);
        mainVp.registerOnPageChangeCallback(pageChangeCallBack);
        bottomNavigation.setOnItemSelectedListener(naviListener);

        Music defaultMusic = new Music("[无歌曲播放中]", "", "");
        updateUIMusicList(new ArrayList<Music>() {{
            add(defaultMusic);
        }});

        BottomNavigationMenuView menuView = (BottomNavigationMenuView)bottomNavigation.getChildAt(0);
        BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(1);
        itemView.setIconSize(70); // 设置图标大小
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_main;
    }

    private final ViewPager2.OnPageChangeCallback pageChangeCallBack = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            switch (position) {
                case 0:
                    bottomNavigation.setSelectedItemId(R.id.action_discover);
                    break;
                case 1:
                    bottomNavigation.setSelectedItemId(R.id.action_my);
                    break;
                case 2:
                    bottomNavigation.setSelectedItemId(R.id.action_rover);
                    break;
            }
        }
    };

    private final BottomNavigationView.OnItemSelectedListener naviListener = new BottomNavigationView.OnItemSelectedListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_discover:
                    mainVp.setCurrentItem(0);
                    break;
                case R.id.action_rover:
                    playPauseView.setLoading(true);
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());
                    executor.execute(() -> {
                        List<Music> tmp = MusicRepository.getInstance().getAllLocalMusic();
                        handler.post(() -> {
                            updateServiceMusicList(tmp);
                            setPlayMode(MusicService.PlayMode.SHUFFLE);
                            playPauseView.setLoading(false);
                            musicService.playNext();
                            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                            intent.putExtra("music", musicService.getCurrentMusic());
                            startActivity(intent);
                        });
                    });
                    break;
                case R.id.action_my:
                    mainVp.setCurrentItem(1);
                    break;
            }
            return true;
        }
    };

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Permission denied
            }
        } else if (requestCode == REQUEST_CODE_NOTIFICATION) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Permission denied
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                // Permission denied
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int position = mainVp.getCurrentItem();
        switch (position) {
            case 0:
                bottomNavigation.setSelectedItemId(R.id.action_discover);
                break;
            case 1:
                bottomNavigation.setSelectedItemId(R.id.action_my);
                break;
        }
    }

    protected void hideBottomStatusBar() {
        Window window = getWindow();
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (windowInsetsController == null) {
            return;
        }
        window.setNavigationBarColor(Color.TRANSPARENT);
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());
    }
}
