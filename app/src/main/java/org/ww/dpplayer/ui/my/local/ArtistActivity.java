package org.ww.dpplayer.ui.my.local;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.exoplayer2.C;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import org.ww.dpplayer.R;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.player.MusicService;
import org.ww.dpplayer.player.MusicServiceController;
import org.ww.dpplayer.ui.base.BaseMusicActivity;
import org.ww.dpplayer.ui.adapter.MusicListAdapter;
import org.ww.dpplayer.ui.base.DialogItemLongPress;
import org.ww.dpplayer.util.ColorUtil;
import org.ww.dpplayer.util.MusicLoader;

import java.util.List;
import java.util.Random;

public class ArtistActivity extends BaseMusicActivity {
    private String artistName;
    private MusicListAdapter musicListAdapter;
    private List<Music> artistMusicList;
    private ImageView mArtistImage;
    private RecyclerView mMusicList;
    private AppBarLayout appBarLayout;
    private LinearLayout llShufflePlay;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获取传入的艺术家信息与歌曲列表
        Intent intent = getIntent();
        artistName = intent.getStringExtra("artistName");
        artistMusicList = intent.getParcelableArrayListExtra("artistMusicList", Music.class);
        for (Music music : artistMusicList) {
            music.setAlbumArt(MusicLoader.getAlbumArt(music.getPath()));
        }

        initView();

        // 设置 AppBarLayout 的滚动监听器
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            boolean isShow = false;
            int scrollRange = -1;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    toolbar.setTitle(artistName);
                    toolbar.setVisibility(View.VISIBLE);
                    isShow = true;
                }
            }
        });
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    private void initView() {
        // 绑定View
        mArtistImage = findViewById(R.id.iv_artist_image);

        appBarLayout = findViewById(R.id.app_bar_layout);
        llShufflePlay = findViewById(R.id.llShufflePlay);
        toolbar = findViewById(R.id.toolbar);

        // 设置View内容
        setTitle(artistName);
        toolbar.setTitle(artistName);

        // 寻找第一个非null的封面
        Bitmap albumArt = null;
        for (Music music : artistMusicList)
            if (music.getAlbumArt() != null) {
                albumArt = music.getAlbumArt();
                break;
            }

        setSupportActionBar(toolbar);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_layout);
        collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary, null));
        collapsingToolbarLayout.setTitle(artistName);
        collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.colorOnPrimary, null));
//        collapsingToolbarLayout.setExpandedTitleTypeface(Typeface.create());
        collapsingToolbarLayout.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD);

        if (albumArt != null)
        {
            mArtistImage.setImageBitmap(albumArt);
            if (ColorUtil.isDarkColor(ColorUtil.getAverageColor(albumArt)))
            {
                toolbar.setTitleTextColor(getResources().getColor(R.color.white, null));
                collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.white, null));
            }
            else
            {
                toolbar.setTitleTextColor(getResources().getColor(R.color.colorOnPrimary, null));
                collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.colorOnPrimary, null));
            }

        }
        else
        {
            mArtistImage.setImageDrawable(getDrawable(R.drawable.default_artist_avatar));
            toolbar.setTitleTextColor(getResources().getColor(R.color.colorOnPrimary, null));
            collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.colorOnPrimary, null));
        }

        // 初始化 RecyclerView
        mMusicList = findViewById(R.id.rv_musics);
        mMusicList.setLayoutManager(new LinearLayoutManager(this));
        musicListAdapter = new MusicListAdapter(ArtistActivity.this, artistMusicList);
        musicListAdapter.setOnItemClickListener(new MusicListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // 播放歌曲
                musicService.setPlaylist(artistMusicList, position);
                MusicServiceController.sendPlayBroadcast(ArtistActivity.this);
            }
        });
        musicListAdapter.setOnItemLongClickListener(new MusicListAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                DialogItemLongPress dialog = new DialogItemLongPress(artistMusicList.get(position));
                dialog.setOnItemDeleteListener(new DialogItemLongPress.OnItemDeleteListener() {
                    @Override
                    public void onItemDelete(Music music) {
                        // 删除歌曲
                        if (MusicLoader.deleteMusic(ArtistActivity.this, music))
                        {
                            musicList.remove(music);
                            musicListAdapter.notifyItemRemoved(position);
                        }
                        else
                            Toast.makeText(ArtistActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        });
        mMusicList.setAdapter(musicListAdapter);

        // 播放栏header随机播放
        llShufflePlay.setOnClickListener(v -> {
            updateServiceMusicList(artistMusicList, new Random().nextInt(artistMusicList.size()));
            musicService.setPlayMode(MusicService.PlayMode.SHUFFLE);
            MusicServiceController.sendPlayBroadcast(this);
        });
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_artist;
    }
}
