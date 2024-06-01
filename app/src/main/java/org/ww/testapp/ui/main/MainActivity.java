package org.ww.testapp.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.ww.testapp.R;
import org.ww.testapp.entity.Music;
import org.ww.testapp.player.MusicService;
import org.ww.testapp.ui.base.BaseMusicActivity;

import java.util.ArrayList;

public class MainActivity extends BaseMusicActivity {

    private static final int REQUEST_CODE = 1024;
    private ViewPager2 mainVp;
    private BottomNavigationView bottomNavigation;
    SectionsPagerAdaptor sectionsPagerAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission();
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
        sectionsPagerAdaptor = new SectionsPagerAdaptor(this);
        mainVp.setAdapter(sectionsPagerAdaptor);
        mainVp.registerOnPageChangeCallback(pageChangeCallBack);
        // 这里是bottomNavigationView的点击事件
        bottomNavigation.setOnItemSelectedListener(naviListener);

        Music defaultMusic = new Music("<未播放>", "", "");
        updateUIMusicList(new ArrayList<Music>() {{
            add(defaultMusic);
        }});
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
                    bottomNavigation.setSelectedItemId(R.id.action_rover);
                    break;
                case 2:
                    bottomNavigation.setSelectedItemId(R.id.action_my);
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
                    mainVp.setCurrentItem(1);
                    break;
                case R.id.action_my:
                    mainVp.setCurrentItem(2);
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
}
