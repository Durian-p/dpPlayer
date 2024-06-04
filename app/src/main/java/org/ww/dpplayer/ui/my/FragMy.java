package org.ww.dpplayer.ui.my;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import org.ww.dpplayer.R;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.player.MusicServiceController;
import org.ww.dpplayer.player.MusicService;

import org.ww.dpplayer.ui.data.MusicViewModel;
import org.ww.dpplayer.ui.my.heart.HeartActivity;
import org.ww.dpplayer.ui.my.local.LocalActivity;
import org.ww.dpplayer.ui.widget.MyItemView;

import java.util.List;

public class FragMy extends Fragment {

    private View rootView;
    private MyItemView localView;
    private MyItemView heartView;
    private List<Music> localMusics;
    private List<Music> heartMusics;
    private MusicViewModel musicViewModel;
    private MusicService musicService;
    private RecyclerView mlistRcv;
    private ImageView playlistAddIv;
    private boolean isBound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public static Fragment newInstance() {
        return new FragMy();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        rootView = inflater.inflate(R.layout.frag_my, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews();
        initData();
        initListener();
        initPlayList();

        // 启动并绑定服务
        Intent serviceIntent = new Intent(getActivity(), MusicService.class);
        getActivity().startService(serviceIntent);
        getActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                CreateMlistDialog dialog = new CreateMlistDialog(requireContext(), imagePickerLauncher);
                dialog.handleImageResult(result.getData());
            }
        });
    }

    private void bindViews() {
        localView = rootView.findViewById(R.id.localView);
        heartView = rootView.findViewById(R.id.favoriteView);
        mlistRcv = rootView.findViewById(R.id.mlistRcv);
        playlistAddIv = rootView.findViewById(R.id.playlistAddIv);
    }

    private void initData() {
        // 获取ViewModel
        musicViewModel = new ViewModelProvider(this).get(MusicViewModel.class);

        // 观察音乐列表数据
        musicViewModel.getLocalMusicList().observe(getViewLifecycleOwner(), new Observer<List<Music>>() {
            @Override
            public void onChanged(List<Music> musicList) {
                localMusics = musicList;
                localView.setSongsNum(localMusics.size(), 0);
            }
        });

        // 观察收藏音乐列表数据
        musicViewModel.getHeartMusicList().observe(getViewLifecycleOwner(), new Observer<List<Music>>() {
            @Override
            public void onChanged(List<Music> heartMusicList) {
                heartMusics = heartMusicList;
                heartView.setSongsNum(heartMusicList.size(), 0);
            }
        });
    }

    private void initListener() {
        initLocalMusics();
        initHeartMusics();

        playlistAddIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateMlistDialog dialog = new CreateMlistDialog(requireContext(), imagePickerLauncher);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
    }

    private void initPlayList() {
        // 初始化播放列表相关逻辑
    }

    private void initLocalMusics() {
        localView.setOnItemClickListener(new MyItemView.OnItemClickListener() {
            @Override
            public void click(View view, int position) {
                if (view.getId() == R.id.iv_play) {
                    if (isBound && localMusics != null && !localMusics.isEmpty()) {
                        musicService.setOriginalPlaylist(localMusics);
                        MusicServiceController.sendPlayBroadcast(requireActivity());
                    }
                } else {
                    Intent intent = new Intent(getActivity(), LocalActivity.class);
                    intent.putExtra("updateService", false);
                    startActivity(intent);
                }
            }
        });
    }

    private void initHeartMusics() {
        heartView.setOnItemClickListener(new MyItemView.OnItemClickListener() {
            @Override
            public void click(View view, int position) {
                if (view.getId() == R.id.iv_play) {
                    musicService.setOriginalPlaylist(heartMusics);
                    MusicServiceController.sendPlayBroadcast(requireActivity());
                } else {
                    Intent intent = new Intent(getActivity(), HeartActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isBound) {
            getActivity().unbindService(serviceConnection);
            isBound = false;
        }
    }
}
