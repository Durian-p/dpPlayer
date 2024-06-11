package org.ww.dpplayer.ui.my.mlist;

import android.os.Bundle;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.ww.dpplayer.R;
import org.ww.dpplayer.database.MusicRepository;
import org.ww.dpplayer.databinding.DialogLongpressBinding;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.entity.MusicList;
import org.ww.dpplayer.player.MusicService;
import org.ww.dpplayer.player.MusicServiceController;
import org.ww.dpplayer.ui.adapter.MusicListAdapter;
import org.ww.dpplayer.ui.base.BaseMusicActivity;
import org.ww.dpplayer.ui.base.DialogItemLongPress;

import java.util.List;
import java.util.Random;

public class MusicListActivity extends BaseMusicActivity
{
    MusicList musicList;
    List<Music> musics;
    MusicRepository musicRepository;
    MusicListAdapter musicListAdapter;

    ImageButton mlBackBtn;
    TextView mlTitle;
    RecyclerView mlRv;
    ImageView ivMlImg;
    TextView mlName;
    TextView tvTitle;
    TextView mlExtra;
    LinearLayout llShufflePlay;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initData();
        initView();

    }

    private void initData()
    {
        musicRepository = MusicRepository.getInstance();
        this.musicList = musicRepository.getMusicListById(getIntent().getLongExtra("mlistId", 0));
        this.musics = musicRepository.getMusicInMlistByMlistId(musicList.getId());

        musicListAdapter = new MusicListAdapter(this, musics);
        musicListAdapter.setOnItemClickListener(new MusicListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position)
            {
                updateServiceMusicList(musics, position);
            }
        });
        musicListAdapter.setOnItemLongClickListener(new MusicListAdapter.OnItemLongClickListener()
        {
            @Override
            public void onItemLongClick(int position)
            {
                DialogItemLongPress dialog = new DialogItemLongPress(musics.get(position));
                dialog.setOnItemDeleteListener(new DialogItemLongPress.OnItemDeleteListener(){
                    @Override
                    public void onItemDelete(Music music)
                    {
                        if (musicRepository.removeMusicFromPlaylist(musicList.getId(), music.getId()))
                        {
                            musics.remove(music);
                            Toast.makeText(MusicListActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            musicListAdapter.notifyItemRemoved(position);
                            Toast.makeText(MusicListActivity.this, "删除失败", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        });
    }

    private void initView()
    {
        mlBackBtn = findViewById(R.id.mlBackBtn);
        mlTitle = findViewById(R.id.mlTitle);

        mlRv = findViewById(R.id.rv_musics);
        ivMlImg = findViewById(R.id.ivMlImg);
        mlName = findViewById(R.id.mlName);
        mlExtra = findViewById(R.id.mlExtra);
        llShufflePlay = findViewById(R.id.llShufflePlay);

        mlRv.setAdapter(musicListAdapter);
        mlRv.setLayoutManager(new LinearLayoutManager(this));

        mlBackBtn.setOnClickListener(v -> finish());
        mlTitle.setText("歌单");
        mlName.setText(musicList.getName());
        mlExtra.setText("(" + musicList.getMusicIdList().size() + "首)");
        if (musicList.getCover()  != null)
            ivMlImg.setImageBitmap(musicList.getCover());
        else
            ivMlImg.setImageResource(R.drawable.default_cover);

        llShufflePlay.setOnClickListener(v -> {
            updateServiceMusicList(musics, new Random().nextInt(musics.size()));
            musicService.setPlayMode(MusicService.PlayMode.SHUFFLE);
            MusicServiceController.sendPlayBroadcast(this);
        });
    }

    @Override
    protected int getContentId()
    {
        return R.layout.activity_musiclist;
    }
}