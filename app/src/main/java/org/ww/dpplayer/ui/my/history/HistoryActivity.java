package org.ww.dpplayer.ui.my.history;

import android.os.Bundle;
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
import org.ww.dpplayer.ui.base.BaseMusicActivity;
import org.ww.dpplayer.ui.base.DialogItemLongPress;
import org.ww.dpplayer.util.MusicLoader;

import java.util.List;
import java.util.Random;

public class HistoryActivity extends BaseMusicActivity implements MusicListAdapter.OnItemClickListener
{
    private LinearLayout llShufflePlay;
    private ImageView iv_info_img;
    private TextView tv_title;
    private TextView tv_extra;
    private RecyclerView rv_list;
    private ImageButton heartBackBtn;
    MusicRepository musicRepository;
    List<Music> historyList;
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
        historyList = musicRepository.getPlayHistory();
        musicListAdapter = new MusicListAdapter(this, historyList);
        musicListAdapter.setOnItemLongClickListener(new MusicListAdapter.OnItemLongClickListener()
        {
            @Override
            public void onItemLongClick(int position)
            {
                DialogItemLongPress dialog = new DialogItemLongPress(historyList.get(position));
                dialog.setOnItemDeleteListener(new DialogItemLongPress.OnItemDeleteListener(){
                    @Override
                    public void onItemDelete(Music music)
                    {
                        if (musicRepository.deletePlayHistory(music.getId()))
                        {
                            historyList.remove(music);
                            musicListAdapter.notifyItemRemoved(position);
                            Toast.makeText(HistoryActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(HistoryActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show(HistoryActivity.this.getSupportFragmentManager(), "dialog");
            }
        });
        musicListAdapter.setOnItemClickListener(this);
    }


    private void initView()
    {
        iv_info_img = findViewById(R.id.ivMlImg);
        tv_title = findViewById(R.id.historyTitle);
        tv_extra = findViewById(R.id.mlExtra);
        rv_list = findViewById(R.id.rv_musics);
        heartBackBtn = findViewById(R.id.historyBackBtn);
        llShufflePlay = findViewById(R.id.llShufflePlay);


        iv_info_img.setImageResource(R.drawable.history_img);
        tv_title.setText("播放历史");
        tv_extra.setText("共" + historyList.size() + "首");

        rv_list.setLayoutManager(new LinearLayoutManager(this));
        rv_list.setAdapter(musicListAdapter);

        heartBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
        });

        // 播放栏header随机播放
        llShufflePlay.setOnClickListener(v -> {
            updateServiceMusicList(historyList, new Random().nextInt(historyList.size()));
            musicService.setPlayMode(MusicService.PlayMode.SHUFFLE);
            MusicServiceController.sendPlayBroadcast(this);
        });
    }

    @Override
    public void onItemClick(int position) {
        updateServiceMusicList(historyList, position);
        MusicServiceController.sendPlayBroadcast(this);
    }
    @Override
    protected int getContentId()
    {
        return R.layout.activity_history;
    }
}
