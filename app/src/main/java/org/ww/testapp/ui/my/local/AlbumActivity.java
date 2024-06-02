package org.ww.testapp.ui.my.local;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.ww.testapp.R;
import org.ww.testapp.entity.Music;
import org.ww.testapp.player.MusicServiceController;
import org.ww.testapp.ui.base.BaseMusicActivity;
import org.ww.testapp.ui.my.local.adapter.MusicAdapter;
import org.ww.testapp.util.MusicLoader;

import java.util.List;

public class AlbumActivity extends BaseMusicActivity {
    private String albumTitle;
    private MusicAdapter musicAdapter;
    private List<Music> albumMusicList;
    private RecyclerView mRecyclerView;

    private ImageView mAlbumCover;
    private TextView mAlbumTitle;
    private TextView mAlbumArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获取传入的专辑信息与歌曲列表
        Intent intent = getIntent();
        albumTitle = intent.getStringExtra("albumTitle");
        albumMusicList = intent.getParcelableArrayListExtra("albumMusicList", Music.class);
        for (Music music: albumMusicList)
        {
            music.setAlbumArt(MusicLoader.getAlbumArt(this, music.getPath()));
        }



        initView();
    }

    @SuppressLint("SetTextI18n")
    private void initView()
    {
        // 绑定View
        mAlbumCover = findViewById(R.id.iv_album_cover);
        mAlbumTitle = findViewById(R.id.tv_album_name);
        mAlbumArtist = findViewById(R.id.tv_artist_name);

        // 设置View内容
        setTitle(albumTitle);
        mAlbumTitle.setText(albumTitle);
        mAlbumArtist.setText("歌手: " + albumMusicList.get(0).getArtist());
        mAlbumCover.setImageBitmap(albumMusicList.get(0).getAlbumArt());

        // 初始化 RecyclerView
        mRecyclerView = findViewById(R.id.rv_musics);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        musicAdapter = new MusicAdapter(AlbumActivity.this, albumMusicList);
        musicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // 播放歌曲
                musicService.setPlaylist(albumMusicList, position);
                MusicServiceController.sendPlayBroadcast(AlbumActivity.this);
            }
        });
        mRecyclerView.setAdapter(musicAdapter);


    }

    @Override
    protected int getContentId() {
        return R.layout.activity_album;
    }
}
