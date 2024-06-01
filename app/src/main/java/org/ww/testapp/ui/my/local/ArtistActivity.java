package org.ww.testapp.ui.my.local;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.AppBarLayout;
import org.ww.testapp.R;
import org.ww.testapp.entity.Music;
import org.ww.testapp.player.MusicControlSender;
import org.ww.testapp.ui.base.BaseMusicActivity;
import org.ww.testapp.ui.my.local.adapter.MusicAdapter;
import org.ww.testapp.util.MusicLoader;

import java.io.IOException;
import java.util.List;

public class ArtistActivity extends BaseMusicActivity {
    private String artistName;
    private MusicAdapter musicAdapter;
    private List<Music> artistMusicList;
    private ImageView mArtistImage;
    private TextView mArtistName;
    private TextView mArtistInfo;
    private RecyclerView mMusicList;
    private ImageButton albartiBackBtn;
    private AppBarLayout appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获取传入的艺术家信息与歌曲列表
        Intent intent = getIntent();
        artistName = intent.getStringExtra("artistName");
        artistMusicList = intent.getParcelableArrayListExtra("artistMusicList", Music.class);
        for (Music music : artistMusicList) {
            music.setAlbumArt(MusicLoader.getAlbumArt(this, music.getPath()));
        }

        initView();

        // 设置 AppBarLayout 的滚动监听器
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                // 根据偏移量改变返回按钮的颜色
                int totalScrollRange = appBarLayout.getTotalScrollRange();
                float percentage = (float) Math.abs(verticalOffset) / totalScrollRange;

                if (percentage >= 0.7f) {
                    // 当滚动超过 70% 时，将按钮颜色改为黑色
                    albartiBackBtn.setColorFilter(Color.BLACK);
                } else {
                    // 否则设置为白色
                    albartiBackBtn.setColorFilter(Color.WHITE);
                }
            }
        });
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    private void initView() {
        // 绑定View
        mArtistImage = findViewById(R.id.iv_artist_image);
        mArtistName = findViewById(R.id.tv_artist_name);
        mArtistInfo = findViewById(R.id.tv_artist_info);
        albartiBackBtn = findViewById(R.id.albartiBackBtn);
        appBarLayout = findViewById(R.id.app_bar_layout);

        // 设置View内容
        setTitle(artistName);
        mArtistName.setText(artistName);
        mArtistInfo.setText("补充信息: " + artistMusicList.get(0).getArtist());
        // 寻找第一个非null的封面
        Bitmap albumArt = null;
        for (Music music : artistMusicList)
            if (music.getAlbumArt() != null) {
                albumArt = music.getAlbumArt();
                break;
            }
        if (albumArt != null)
            mArtistImage.setImageBitmap(albumArt);
        else
            mArtistImage.setImageDrawable(getDrawable(R.drawable.default_artist_avatar));

        // 初始化 RecyclerView
        mMusicList = findViewById(R.id.rv_musics);
        mMusicList.setLayoutManager(new LinearLayoutManager(this));
        musicAdapter = new MusicAdapter(ArtistActivity.this, artistMusicList);
        musicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // 播放歌曲
                musicService.setPlaylist(artistMusicList, position);
                MusicControlSender.sendPlayBroadcast(ArtistActivity.this);
            }
        });
        mMusicList.setAdapter(musicAdapter);
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_artist;
    }
}
