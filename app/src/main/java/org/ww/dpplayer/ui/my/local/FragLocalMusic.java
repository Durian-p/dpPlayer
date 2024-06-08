// FragLocalMusic.java
package org.ww.dpplayer.ui.my.local;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.ww.dpplayer.R;
import org.ww.dpplayer.database.MusicRepository;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.player.MusicService;
import org.ww.dpplayer.player.MusicServiceController;
import org.ww.dpplayer.ui.adapter.MusicListAdapter;
import org.ww.dpplayer.ui.base.BaseMusicActivity;
import org.ww.dpplayer.ui.base.DialogItemLongPress;
import org.ww.dpplayer.ui.data.MusicViewModel;
import org.ww.dpplayer.ui.widget.SidebarView;
import org.ww.dpplayer.util.MusicLoader;

import java.util.List;
import java.util.Random;

public class FragLocalMusic extends Fragment implements SwipeRefreshLayout.OnRefreshListener, MusicListAdapter.OnItemClickListener, SidebarView.OnLetterClickedListener {

    private LinearLayout llShufflePlay;
    private  BaseMusicActivity baseMusicActivity;
    private RecyclerView recyclerView;
    private MusicListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private MusicViewModel musicViewModel;
    private MusicRepository repository;

    private List<Music> localList;
    private MusicServiceController controller;

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if (context instanceof BaseMusicActivity)
        {
            baseMusicActivity = (BaseMusicActivity) context;
        }
        else
        {
            throw new RuntimeException(context.toString() + " must be an instance of BaseMusicActivity");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_local_music, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.localMusicReView);
        swipeRefreshLayout = view.findViewById(R.id.localMusicSwipe);
        SidebarView sidebarView = view.findViewById(R.id.localMusicSide);
        llShufflePlay = view.findViewById(R.id.llShufflePlay);

        // data
        controller = new MusicServiceController(requireContext());
        controller.bindService();

        repository = MusicRepository.getInstance();

        // 显示加载动画
        swipeRefreshLayout.setRefreshing(true);

        // 设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);


        // 获取ViewModel
        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        // 观察音乐列表数据
        musicViewModel.getLocalMusicList().observe(getViewLifecycleOwner(), new Observer<List<Music>>() {
            @Override
            public void onChanged(List<Music> musicList) {
                localList = musicList;
                // 设置适配器
                adapter = new MusicListAdapter(requireContext(), musicList);
                adapter.setOnItemClickListener(FragLocalMusic.this);
                adapter.setOnItemLongClickListener(new MusicListAdapter.OnItemLongClickListener()
                {
                    @Override
                    public void onItemLongClick(int position)
                    {
                        DialogItemLongPress dialog = new DialogItemLongPress(musicList.get(position));
                        dialog.show(requireActivity().getSupportFragmentManager(), "dialog");
                    }
                });
                adapter.setOnItemLongClickListener(new MusicListAdapter.OnItemLongClickListener()
                {
                    @Override
                    public void onItemLongClick(int position)
                    {
                        DialogItemLongPress dialog = new DialogItemLongPress(musicList.get(position));
                        dialog.setOnItemDeleteListener(new DialogItemLongPress.OnItemDeleteListener(){
                            @Override
                            public void onItemDelete(Music music)
                            {
                                if (MusicLoader.deleteMusic(requireContext(), music))
                                {
                                    musicList.remove(music);
                                    adapter.notifyItemRemoved(position);
                                    Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show();
                                }
                                else
                                    Toast.makeText(requireContext(), "删除失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog.show(requireActivity().getSupportFragmentManager(), "dialog");
                    }
                });
                recyclerView.setAdapter(adapter);
                swipeRefreshLayout.setRefreshing(false); // 停止刷新动画
            }
        });

        // 设置sideBar
        sidebarView.setOnLetterClickedListener(this);

        // 设置SwipeRefreshLayout的Listener
        swipeRefreshLayout.setOnRefreshListener(this);

        // 播放栏header随机播放
        llShufflePlay.setOnClickListener(v -> {
            controller.updateServiceMusicList(localList, new Random().nextInt(localList.size()));
            controller.setPlayMode(MusicService.PlayMode.SHUFFLE);
            MusicServiceController.sendPlayBroadcast(requireContext());
        });
    }

    @Override
    public void onItemClick(int position) {
        if (baseMusicActivity != null) {
            baseMusicActivity.updateServiceMusicList(musicViewModel.getLocalMusicList().getValue(), position);
            MusicServiceController.sendPlayBroadcast(baseMusicActivity);
        }
    }

    @Override
    public void onRefresh() {
        // 显示刷新动画
        swipeRefreshLayout.setRefreshing(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 执行耗时操作
                final List<Music> musicList = MusicLoader.findLocalMusic(requireContext());

                // 将结果发送到主线程更新UI
                new Handler(requireContext().getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        // 更新适配器数据
                        adapter.updateData(musicList);
                        // 停止刷新动画
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onLetterClicked(String str) {
        int position = adapter.getPositionForSection(str.charAt(0));
        if (position != RecyclerView.NO_POSITION) {
            // 使用自定义的 LinearSmoothScroller 将目标项平滑滚动到顶部
            RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(requireContext());
            smoothScroller.setTargetPosition(position);
            recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
        }
    }

    public class TopSnappedSmoothScroller extends LinearSmoothScroller
    {

        public TopSnappedSmoothScroller(Context context) {
            super(context);
        }

        @Override
        protected int getVerticalSnapPreference() {
            return SNAP_TO_START; // 将目标项平滑滚动到顶部
        }

    }

    @Override
    public void onDestroy()
    {
        controller.unbindService();
        super.onDestroy();
    }
}
