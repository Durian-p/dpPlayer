package org.ww.dpplayer.ui.my.mlist;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.ww.dpplayer.R;
import org.ww.dpplayer.database.MusicRepository;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.entity.MusicList;
import org.ww.dpplayer.ui.adapter.MusicListAdapter;
import org.ww.dpplayer.ui.base.BaseMusicActivity;

import java.util.List;

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
    TextView mlExtra;

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
    }

     private void initView()
     {
         mlBackBtn = findViewById(R.id.mlBackBtn);
         mlTitle = findViewById(R.id.mlTitle);
         mlRv = findViewById(R.id.rv_musics);
         ivMlImg = findViewById(R.id.ivMlImg);
         mlName = findViewById(R.id.mlName);
         mlExtra = findViewById(R.id.mlExtra);

         mlRv.setAdapter(musicListAdapter);
         mlRv.setLayoutManager(new LinearLayoutManager(this));

         mlBackBtn.setOnClickListener(v -> finish());
         mlTitle.setText("歌单");
         mlName.setText(musicList.getName());
         mlExtra.setText("(" + musicList.getMusicIdList().size() + "首)");
         ivMlImg.setImageBitmap(musicList.getCover());
     }

    @Override
    protected int getContentId()
    {
        return R.layout.activity_musiclist;
    }
}
