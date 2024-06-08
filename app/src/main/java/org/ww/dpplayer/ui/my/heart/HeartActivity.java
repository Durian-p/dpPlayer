package org.ww.dpplayer.ui.my.heart;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.ww.dpplayer.R;
import org.ww.dpplayer.database.MusicRepository;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.player.MusicService;
import org.ww.dpplayer.player.MusicServiceController;
import org.ww.dpplayer.ui.adapter.MusicListAdapter;
import org.ww.dpplayer.ui.base.BaseMusicActivity;

import java.util.List;
import java.util.Random;

public class HeartActivity extends BaseMusicActivity implements MusicListAdapter.OnItemClickListener
{
    private LinearLayout llShufflePlay;
    private ImageView iv_info_img;
    private TextView tv_title;
    private TextView tv_extra;
    private RecyclerView rv_list;
    private ImageButton heartBackBtn;
    MusicRepository musicRepository;
    List<Music> heartList;
    MusicListAdapter musicListAdapter;

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
        heartList = musicRepository.getAllHeartMusic();

        musicListAdapter = new MusicListAdapter(this, heartList);
        musicListAdapter.setOnItemClickListener(this);
    }


    private void initView()
    {
        iv_info_img = findViewById(R.id.ivMlImg);
        tv_title = findViewById(R.id.mlName);
        tv_extra = findViewById(R.id.mlExtra);
        rv_list = findViewById(R.id.rv_musics);
        heartBackBtn = findViewById(R.id.historyBackBtn);
        llShufflePlay = findViewById(R.id.llShufflePlay);

        iv_info_img.setImageResource(R.drawable.heart_img);
        tv_title.setText("我的收藏");
        tv_extra.setText("共" + heartList.size() + "首");

        rv_list.setLayoutManager(new LinearLayoutManager(this));
        rv_list.setAdapter(musicListAdapter);

        heartBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
        });

        // 播放栏header随机播放
        llShufflePlay.setOnClickListener(v -> {
            updateServiceMusicList(heartList, new Random().nextInt(heartList.size()));
            musicService.setPlayMode(MusicService.PlayMode.SHUFFLE);
            MusicServiceController.sendPlayBroadcast(this);
        });

    }

    @Override
    protected int getContentId()
    {
        return R.layout.activity_heartlist;
    }

    @Override
    public void onItemClick(int position) {
        updateServiceMusicList(heartList, position);
        MusicServiceController.sendPlayBroadcast(this);
    }
}
