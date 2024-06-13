package org.ww.dpplayer.ui.my.local;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.ww.dpplayer.R;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.player.MusicService;
import org.ww.dpplayer.player.MusicServiceController;
import org.ww.dpplayer.ui.adapter.MusicListAdapter;
import org.ww.dpplayer.ui.base.BaseMusicActivity;
import org.ww.dpplayer.ui.base.DialogMusicLongPress;
import org.ww.dpplayer.util.MusicLoader;

import java.util.List;
import java.util.Random;

public class AlbumActivity extends BaseMusicActivity {
    private String albumTitle;
    private MusicListAdapter musicListAdapter;
    private List<Music> albumMusicList;
    private RecyclerView mRecyclerView;

    private ImageView mAlbumCover;
    private TextView mAlbumTitle;
    private TextView mAlbumArtist;
    private ImageButton mAlbumBackBtn;
    private LinearLayout llShufflePlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(getColor(R.color.colorPrimary));
        // 获取传入的专辑信息与歌曲列表
        Intent intent = getIntent();
        albumTitle = intent.getStringExtra("albumTitle");
        albumMusicList = intent.getParcelableArrayListExtra("albumMusicList", Music.class);
        for (Music music: albumMusicList)
        {
            music.setAlbumArt(MusicLoader.getAlbumArt(music.getPath()));
        }



        initView();
    }

    @SuppressLint("SetTextI18n")
    private void initView()
    {
        // 绑定View
        mAlbumCover = findViewById(R.id.ivMlImg);
        mAlbumTitle = findViewById(R.id.mlName);
        mAlbumArtist = findViewById(R.id.mlExtra);
        mAlbumBackBtn = findViewById(R.id.albumBackBtn);
        llShufflePlay = findViewById(R.id.llShufflePlay);

        // 设置View内容
        setTitle(albumTitle);
        mAlbumTitle.setText(albumTitle);
        mAlbumArtist.setText("歌手: " + albumMusicList.get(0).getArtist());
        if (albumMusicList.get(0).getAlbumArt() != null)
            mAlbumCover.setImageBitmap(albumMusicList.get(0).getAlbumArt());
        else
            mAlbumCover.setImageResource(R.drawable.default_cover);

        // 初始化 RecyclerView
        mRecyclerView = findViewById(R.id.rv_musics);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        musicListAdapter = new MusicListAdapter(AlbumActivity.this, albumMusicList);
        musicListAdapter.setOnItemClickListener(new MusicListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // 播放歌曲
                musicService.setPlaylist(albumMusicList, position);
                MusicServiceController.sendPlayBroadcast(AlbumActivity.this);
            }
        });
        musicListAdapter.setOnItemLongClickListener(new MusicListAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                // 长按弹出菜单
                DialogMusicLongPress dialog = new DialogMusicLongPress(albumMusicList.get(position));
                dialog.setOnItemDeleteListener(new DialogMusicLongPress.OnItemDeleteListener(){
                    @Override
                    public void onItemDelete(Music music)
                    {
                        if (MusicLoader.deleteMusic(AlbumActivity.this, music))
                        {
                            musicList.remove(music);
                            musicListAdapter.notifyItemRemoved(position);
                        }
                        else
                            Toast.makeText(AlbumActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        });
        mRecyclerView.setAdapter(musicListAdapter);

        mAlbumBackBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        // 播放栏header随机播放
        llShufflePlay.setOnClickListener(v -> {
            updateServiceMusicList(albumMusicList, new Random().nextInt(albumMusicList.size()));
            musicService.setPlayMode(MusicService.PlayMode.SHUFFLE);
            MusicServiceController.sendPlayBroadcast(this);
        });
    }



    @Override
    protected int getContentId() {
        return R.layout.activity_album;
    }
}
