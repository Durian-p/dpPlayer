package org.ww.dpplayer.ui.my.local;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.ww.dpplayer.R;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.player.MusicServiceController;
import org.ww.dpplayer.ui.adapter.MusicListAdapter;
import org.ww.dpplayer.ui.base.BaseMusicActivity;
import org.ww.dpplayer.util.MusicLoader;

import java.util.List;

public class AlbumActivity extends BaseMusicActivity {
    private String albumTitle;
    private MusicListAdapter musicListAdapter;
    private List<Music> albumMusicList;
    private RecyclerView mRecyclerView;

    private ImageView mAlbumCover;
    private TextView mAlbumTitle;
    private TextView mAlbumArtist;
    private ImageButton mAlbumBackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        // 设置View内容
        setTitle(albumTitle);
        mAlbumTitle.setText(albumTitle);
        mAlbumArtist.setText("歌手: " + albumMusicList.get(0).getArtist());
        mAlbumCover.setImageBitmap(albumMusicList.get(0).getAlbumArt());

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
        mRecyclerView.setAdapter(musicListAdapter);

        mAlbumBackBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_album;
    }
}
