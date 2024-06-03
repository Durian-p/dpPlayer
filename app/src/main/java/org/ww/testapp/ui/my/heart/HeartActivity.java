package org.ww.testapp.ui.my.heart;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.ww.testapp.R;
import org.ww.testapp.database.MusicRepository;
import org.ww.testapp.entity.Music;
import org.ww.testapp.player.MusicServiceController;
import org.ww.testapp.ui.base.BaseMusicActivity;
import org.ww.testapp.ui.my.local.adapter.MusicAdapter;

import java.util.List;

public class HeartActivity extends BaseMusicActivity implements MusicAdapter.OnItemClickListener
{
    private ImageView iv_info_img;
    private TextView tv_title;
    private TextView tv_extra;
    private RecyclerView rv_list;
    private ImageButton heartBackBtn;
    MusicRepository musicRepository;
    List<Music> heartList;
    MusicAdapter musicAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initData();
        initView();
    }

    private void initData()
    {
        musicRepository = new MusicRepository(this);
        heartList = musicRepository.getAllHeartMusic();

        musicAdapter = new MusicAdapter(this, heartList);
        musicAdapter.setOnItemClickListener(this);
    }


    private void initView()
    {
        iv_info_img = findViewById(R.id.iv_info_img);
        tv_title = findViewById(R.id.tv_musiclist_name);
        tv_extra = findViewById(R.id.tv_musiclist_extra);
        rv_list = findViewById(R.id.rv_musics);
        heartBackBtn = findViewById(R.id.heartBackBtn);

        iv_info_img.setImageResource(R.drawable.heart_img);
        tv_title.setText("我的收藏");
        tv_extra.setText("共" + heartList.size() + "首");

        rv_list.setLayoutManager(new LinearLayoutManager(this));
        rv_list.setAdapter(musicAdapter);

        heartBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
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
