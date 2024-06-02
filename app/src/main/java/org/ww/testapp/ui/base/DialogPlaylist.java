package org.ww.testapp.ui.base;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.ww.testapp.R;
import org.ww.testapp.entity.Music;
import org.ww.testapp.player.MusicService;
import org.ww.testapp.player.MusicServiceController;

import java.util.List;

public class DialogPlaylist extends BottomSheetDialogFragment
{

    private TextView tv_song_sum;
    private TextView tv_play_mode;
    private ImageView iv_play_mode;
    private List<Music> musicList;
    private long currentId;
    private int position;
    private MusicServiceController musicServiceController;
    private MusicService musicService;

    public DialogPlaylist(List<Music> musicList, long currentId)
    {
        this.musicList = musicList;
        this.currentId = currentId;
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.dialog_playlist, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rcv_songs);
        tv_play_mode = view.findViewById(R.id.tv_play_mode);
        tv_song_sum = view.findViewById(R.id.tv_song_sum);
        iv_play_mode = view.findViewById(R.id.iv_play_mode);

        PlayListAdapter adapter = new PlayListAdapter(musicList, currentId);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        tv_song_sum.setText(" (" + musicList.size() + ")");

        iv_play_mode.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MusicServiceController.sendTogglePlayModeBroadcast(requireContext());
            }
        });

        musicServiceController = new MusicServiceController(getContext());
        musicServiceController.bindService(new MusicServiceController.MusicServiceCallback()
        {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service)
            {
                MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
                musicService = binder.getService();
                musicService.getPlaybackState().observe(DialogPlaylist.this, playbackInfo ->
                {
                    if (playbackInfo != null)
                    {
                        // 更新播放列表
                        if (! playbackInfo.originalPlayList.equals(musicList))
                        {
                            musicList = playbackInfo.originalPlayList;
                            tv_song_sum.setText(" (" + musicList.size() + ")");
                            adapter.updatePlayingList(musicList);
                            adapter.updatePlayingId(playbackInfo.music.getId());
                            adapter.notifyDataSetChanged();
                        }
                        // 更新播放列表中正在播放歌曲
                        if(playbackInfo.music.getId() != currentId)
                        {
                            currentId = playbackInfo.music.getId();
                            adapter.updatePlayingId(currentId);
                            adapter.notifyDataSetChanged();
                            position = playbackInfo.currentSongIndex;
                        }
                        MusicService.PlayMode playMode = playbackInfo.playMode;
                        if (playMode != null)
                        {
                            if (playMode == MusicService.PlayMode.SEQUENTIAL)
                            {
                                tv_play_mode.setText("顺序播放");
                                iv_play_mode.setImageResource(R.drawable.ic_repeat);
                            }
                            else if (playMode == MusicService.PlayMode.REPEAT_ONE)
                            {
                                tv_play_mode.setText("单曲循环");
                                iv_play_mode.setImageResource(R.drawable.ic_repeat_one);
                            }
                            else if (playMode == MusicService.PlayMode.SHUFFLE)
                            {
                                tv_play_mode.setText("随机播放");
                                iv_play_mode.setImageResource(R.drawable.ic_shuffle);
                            }
                        }
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        });
        return view;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        musicServiceController.unbindService();
    }
}
