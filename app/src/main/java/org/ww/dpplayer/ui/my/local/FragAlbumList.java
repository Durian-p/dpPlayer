package org.ww.dpplayer.ui.my.local;

import android.content.Context;
import android.content.Intent;
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
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.ww.dpplayer.R;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.ui.data.MusicViewModel;
import org.ww.dpplayer.ui.adapter.AlbumListAdapter;
import org.ww.dpplayer.ui.widget.SidebarView;
import org.ww.dpplayer.util.MusicLoader;

import java.util.ArrayList;
import java.util.List;

public class FragAlbumList extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AlbumListAdapter.OnItemClickListener, SidebarView.OnLetterClickedListener
{

    private RecyclerView recyclerView;
    private AlbumListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MusicViewModel musicViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_local_album, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.localAlbumReView);
        swipeRefreshLayout = view.findViewById(R.id.localAlbumSwipe);
        SidebarView sidebarView = view.findViewById(R.id.localAlbumSide);

        // 显示加载动画
        swipeRefreshLayout.setRefreshing(true);

        // 设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // 获取ViewModel
        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        // 观察音乐列表数据
        musicViewModel.getLocalMusicList().observe(getViewLifecycleOwner(), new Observer<List<Music>>() {
            @Override
            public void onChanged(List<Music> musicList) {
                // 设置适配器
                adapter = new AlbumListAdapter(requireContext(), musicList);
                adapter.setOnItemClickListener(FragAlbumList.this);
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
                        // 添加延时
                        new Handler(requireContext().getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 停止刷新动画
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }, 200); // 延时 200 毫秒
                    }
                });
            }
        }).start();
    }

    @Override
    public void onItemClick(int position)
    {
        // 启动AlbumActivity展示详情
        Intent intent = new Intent(requireContext(), AlbumActivity.class);
        ArrayList<Music> artistMusicList = (ArrayList<Music>) adapter.getAlbumList().get(position);
        intent.putParcelableArrayListExtra("albumMusicList", artistMusicList);
        intent.putExtra("albumTitle",artistMusicList.get(0).getAlbum());
        startActivity(intent);
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
}
