// FragLocalMusic.java
package org.ww.dpplayer.ui.my.local;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.ww.dpplayer.R;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.player.MusicServiceController;
import org.ww.dpplayer.ui.base.BaseMusicActivity;
import org.ww.dpplayer.ui.data.MusicViewModel;
import org.ww.dpplayer.ui.my.local.adapter.MusicAdapter;
import org.ww.dpplayer.ui.widget.SidebarView;
import org.ww.dpplayer.util.MusicLoader;

import java.util.List;

public class FragLocalMusic extends Fragment implements SwipeRefreshLayout.OnRefreshListener, MusicAdapter.OnItemClickListener, SidebarView.OnLetterClickedListener {

    private  BaseMusicActivity baseMusicActivity;
    private RecyclerView recyclerView;
    private MusicAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MusicViewModel musicViewModel;

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

        // 显示加载动画
        swipeRefreshLayout.setRefreshing(true);

        // 设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);


        // 获取ViewModel
        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        // 观察音乐列表数据
        musicViewModel.getMusicList().observe(getViewLifecycleOwner(), new Observer<List<Music>>() {
            @Override
            public void onChanged(List<Music> musicList) {
                // 设置适配器
                adapter = new MusicAdapter(requireContext(), musicList);
                adapter.setOnItemClickListener(FragLocalMusic.this);
                recyclerView.setAdapter(adapter);
                swipeRefreshLayout.setRefreshing(false); // 停止刷新动画
            }
        });

        // 设置sideBar
        sidebarView.setOnLetterClickedListener(this);

        // 设置SwipeRefreshLayout的Listener
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onItemClick(int position) {
        if (baseMusicActivity != null) {
            baseMusicActivity.updateServiceMusicList(musicViewModel.getMusicList().getValue(), position);
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
    public void onLetterClicked(String str)
    {
        int position = adapter.getPositionForSection(str.charAt(0));
        recyclerView.smoothScrollToPosition(position);
    }
}
