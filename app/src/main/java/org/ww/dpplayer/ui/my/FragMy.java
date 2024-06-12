package org.ww.dpplayer.ui.my;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.ww.dpplayer.R;
import org.ww.dpplayer.database.MusicRepository;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.entity.MusicList;
import org.ww.dpplayer.player.MusicServiceController;
import org.ww.dpplayer.player.MusicService;

import org.ww.dpplayer.ui.adapter.MusicListsAdapter;
import org.ww.dpplayer.ui.data.MusicViewModel;
import org.ww.dpplayer.ui.my.heart.HeartActivity;
import org.ww.dpplayer.ui.my.history.HistoryActivity;
import org.ww.dpplayer.ui.my.local.LocalActivity;
import org.ww.dpplayer.ui.my.mlist.MusicListActivity;
import org.ww.dpplayer.ui.widget.MyItemView;

import java.util.List;

public class FragMy extends Fragment {

    private List<Music> localMusics;
    private List<Music> heartMusics;
    private List<Music> historyMusics;
    private List<MusicList> musicLists;
    private boolean isBound = false;
    private MusicListsAdapter musicListsAdapter;


    private View rootView;
    private MyItemView localView;
    private MyItemView heartView;
    private MyItemView historyView;
    private MusicViewModel musicViewModel;
    private MusicService musicService;
    private RecyclerView mlistRcv;
    private ImageView playlistAddIv;
    private ImageView multiSelectIv;

    private DialogNewMlist dialog;

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
        requireActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary, null));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//设置透明状态栏
        }

        bindViews();
        initData();
        initListener();

        // 启动并绑定服务
        Intent serviceIntent = new Intent(getActivity(), MusicService.class);
        getActivity().startService(serviceIntent);
        getActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
//                DialogNewMlist dialog = new DialogNewMlist(requireContext(), imagePickerLauncher);
                dialog.handleImageResult(result.getData());
            }
        });
    }

    private void bindViews() {
        localView = rootView.findViewById(R.id.localView);
        heartView = rootView.findViewById(R.id.favoriteView);
        historyView = rootView.findViewById(R.id.historyView);
        mlistRcv = rootView.findViewById(R.id.mlistRcv);
        playlistAddIv = rootView.findViewById(R.id.playlistAddIv);
        multiSelectIv = rootView.findViewById(R.id.multiSelectIv);
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

        musicViewModel.getHistoryMusicList().observe(getViewLifecycleOwner(), new Observer<List<Music>>()
        {
            @Override
            public void onChanged(List<Music> historyMusics)
            {
                FragMy.this.historyMusics = historyMusics;
                historyView.setSongsNum(historyMusics.size(), 0);
            }
        });

        // 设置rv
        musicLists = musicViewModel.getMusicLists().getValue();
        musicListsAdapter = new MusicListsAdapter(musicLists);
        musicListsAdapter.setListener(new MusicListsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, long id) {
                Intent intent = new Intent(getActivity(), MusicListActivity.class);
                intent.putExtra("mlistId", musicLists.get(position).getId());
                startActivity(intent);
            }
        });
        mlistRcv.setLayoutManager(new LinearLayoutManager(requireContext()));
        mlistRcv.setAdapter(musicListsAdapter);
        musicViewModel.getMusicLists().observe(getViewLifecycleOwner(), new Observer<List<MusicList>>()
        {
            @Override
            public void onChanged(List<MusicList> musicLists)
            {
                if (musicLists != null && !musicLists.isEmpty()) {
                    FragMy.this.musicLists = musicLists;
                    musicListsAdapter.setMusicLists(musicLists);
                    musicListsAdapter.notifyDataSetChanged();
                }
            }
        });

        multiSelectIv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });
    }

    private void initListener() {
        initLocalMusics();
        initHeartMusics();
        initHistoryMusics();

        playlistAddIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new DialogNewMlist(requireContext(), imagePickerLauncher);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
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

    private void initHistoryMusics()
    {
        historyView.setOnItemClickListener(new MyItemView.OnItemClickListener() {
            @Override
            public void click(View view, int position)
            {
                if (view.getId() == R.id.iv_play)
                {
                    musicService.setOriginalPlaylist(historyMusics);
                    MusicServiceController.sendPlayBroadcast(requireActivity());
                }
                else
                {
                    Intent intent = new Intent(getActivity(), HistoryActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (isBound) {
            musicViewModel.bindListenerToRepository();
            musicViewModel.loadHeartMusics();
            musicViewModel.loadMusics();
            musicViewModel.loadHistoryMusics();
            musicViewModel.loadMusicList();

        }
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
