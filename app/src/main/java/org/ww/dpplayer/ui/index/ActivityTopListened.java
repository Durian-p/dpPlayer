package org.ww.dpplayer.ui.index;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.ww.dpplayer.R;
import org.ww.dpplayer.database.MusicRepository;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.player.MusicService;
import org.ww.dpplayer.player.MusicServiceController;
import org.ww.dpplayer.ui.adapter.MusicListAdapter;
import org.ww.dpplayer.ui.adapter.MusicListWithCntAdapter;
import org.ww.dpplayer.ui.base.BaseMusicActivity;
import org.ww.dpplayer.ui.base.DialogItemLongPress;

import java.util.List;
import java.util.Random;

public class ActivityTopListened extends BaseMusicActivity implements MusicListAdapter.OnItemClickListener
{
    private LinearLayout llShufflePlay;
    private ImageView iv_info_img;
    private TextView tv_title;
    private TextView tv_extra;
    private RecyclerView rv_list;
    private ImageButton heartBackBtn;
    MusicRepository musicRepository;
    List<Music> topList;
    List<Integer> topCnt;
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
        Pair<List<Music>, List<Integer>> ret = MusicRepository.getInstance().getTop100SongsByPlayCount();;
        topList = ret.first;
        topCnt = ret.second;

        musicListAdapter = new MusicListWithCntAdapter(this, topList, topCnt);
        musicListAdapter.setOnItemClickListener(this);
        musicListAdapter.setOnItemLongClickListener(new MusicListAdapter.OnItemLongClickListener()
        {
            @Override
            public void onItemLongClick(int position)
            {
                DialogItemLongPress dialog = new DialogItemLongPress(topList.get(position));
                dialog.setOnItemDeleteListener(new DialogItemLongPress.OnItemDeleteListener(){
                    @Override
                    public void onItemDelete(Music music)
                    {
                        if (musicRepository.deleteRecord(music.getId()))
                        {
                            topList.remove(music);
                            musicListAdapter.notifyItemRemoved(position);
                            Toast.makeText(ActivityTopListened.this, "删除成功", Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(ActivityTopListened.this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show(ActivityTopListened.this.getSupportFragmentManager(), "dialog");
            }
        });
    }


    private void initView()
    {
        iv_info_img = findViewById(R.id.ivMlImg);
        tv_title = findViewById(R.id.mlName);
        tv_extra = findViewById(R.id.mlExtra);
        rv_list = findViewById(R.id.rv_musics);
        heartBackBtn = findViewById(R.id.historyBackBtn);
        llShufflePlay = findViewById(R.id.llShufflePlay);

        iv_info_img.setImageResource(R.drawable.trending_img);

        tv_title.setText("最多收听");
        TextView mlTitle = findViewById(R.id.historyTitle);
        mlTitle.setText("最多收听");
        tv_extra.setText("共" + topList.size() + "首");

        rv_list.setLayoutManager(new LinearLayoutManager(this));
        rv_list.setAdapter(musicListAdapter);

        heartBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
        });

        // 播放栏header随机播放
        llShufflePlay.setOnClickListener(v -> {
            updateServiceMusicList(topList, new Random().nextInt(topList.size()));
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
        updateServiceMusicList(topList, position);
        MusicServiceController.sendPlayBroadcast(this);
    }
}

