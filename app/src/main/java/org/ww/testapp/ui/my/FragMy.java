package org.ww.testapp.ui.my;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import org.ww.testapp.R;
import org.ww.testapp.entity.Music;
import org.ww.testapp.player.MusicControlSender;
import org.ww.testapp.player.MusicService;
import org.ww.testapp.ui.data.MusicViewModel;
import org.ww.testapp.ui.my.local.LocalActivity;
import org.ww.testapp.ui.widget.MyItemView;

import java.util.List;

public class FragMy extends Fragment {

    private View rootView;
    private MyItemView localView;
    private List<Music> localMusics;
    private MusicViewModel musicViewModel;
    private MusicService musicService;
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
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews();
        initData();
        initFunc();
        initPlayList();

        // 启动并绑定服务
        Intent serviceIntent = new Intent(getActivity(), MusicService.class);
        getActivity().startService(serviceIntent);
        getActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void bindViews() {
        localView = rootView.findViewById(R.id.localView);
    }

    private void initData() {
        // 获取ViewModel
        musicViewModel = new ViewModelProvider(this).get(MusicViewModel.class);

        // 观察音乐列表数据
        musicViewModel.getMusicList().observe(getViewLifecycleOwner(), new Observer<List<Music>>() {
            @Override
            public void onChanged(List<Music> musicList) {
                localMusics = musicList;
                localView.setSongsNum(localMusics.size(), 0);
            }
        });
    }

    private void initFunc() {
        initLocalMusics();
    }

    private void initPlayList() {

    }

    private void initLocalMusics() {
        localView.setOnItemClickListener(new MyItemView.OnItemClickListener() {
            @Override
            public void click(View view, int position) {
                if (view.getId() == R.id.iv_play) {
                    if (isBound && localMusics != null && !localMusics.isEmpty()) {
                        musicService.setPlaylist(localMusics);
                        MusicControlSender.sendPlayBroadcast(requireActivity());
                    }
                } else {
                    Intent intent = new Intent(getActivity(), LocalActivity.class);
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
