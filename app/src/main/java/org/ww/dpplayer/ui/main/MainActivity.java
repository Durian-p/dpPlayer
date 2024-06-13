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
import androidx.activity.EdgeToEdge;
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
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends BaseMusicActivity {

    private static final int REQUEST_CODE = 1024;
    private ViewPager2 mainVp;
    private BottomNavigationView bottomNavigation;
    MainPagerAdaptor mainPagerAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        hideBottomStatusBar();


        requestPermission();
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
        // 这里是bottomNavigationView的点击事件
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
            // 根据 ViewPager2 的当前页面位置来设置 BottomNavigationView 的选中项
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
//                    mainVp.setCurrentItem(1);
                    playPauseView.setLoading(true);

                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());

                    executor.execute(() -> {
                        // Execute the time-consuming task in a background thread
                        List<Music> tmp = MusicRepository.getInstance().getAllLocalMusic();

                        // Once the task is completed, update the UI in the main thread
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

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (Environment.isExternalStorageManager()) {
                // writeFile();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 先判断有没有权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // writeFile();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        } else {
            // writeFile();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // writeFile();
            } else {
                // ToastUtils.show("存储权限获取失败");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                // writeFile();
            } else {
                // ToastUtils.show("存储权限获取失败");
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // 获取窗口位置调整底部导航栏选中
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

    /**
     * 隐藏 NavigationBar和StatusBar
     */
    protected void hideBottomStatusBar() {
        //隐藏虚拟按键，并且全屏
        Window window = getWindow();
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (windowInsetsController == null) {
            return;
        }
// Hide the system bars.
        window.setNavigationBarColor(Color.TRANSPARENT);
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());




// Show the system bars.
//        windowInsetsController.show(WindowInsetsCompat.Type.systemBars());
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }
}
