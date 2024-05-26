package org.ww.testapp.ui.my.local;

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
import org.ww.testapp.R;
import org.ww.testapp.entity.Music;
import org.ww.testapp.ui.data.MusicViewModel;
import org.ww.testapp.ui.my.local.adapter.AlbumAdapter;
import org.ww.testapp.ui.widget.SidebarView;
import org.ww.testapp.util.MusicLoader;

import java.util.List;

public class FragAlbum extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView recyclerView;
    private AlbumAdapter adapter;
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
        musicViewModel.getMusicList().observe(getViewLifecycleOwner(), new Observer<List<Music>>() {
            @Override
            public void onChanged(List<Music> musicList) {
                // 设置适配器
                adapter = new AlbumAdapter(requireContext(), musicList);
                recyclerView.setAdapter(adapter);
                swipeRefreshLayout.setRefreshing(false); // 停止刷新动画
            }
        });

        // 设置sideBar
        sidebarView.setOnLetterClickedListener(new SidebarView.OnLetterClickedListener()
        {
            @Override
            public void onLetterClicked(String str)
            {
                int position = adapter.getPositionForSection(str.charAt(0));
                recyclerView.smoothScrollToPosition(position);
            }
        });

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
}
