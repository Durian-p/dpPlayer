package org.ww.dpplayer.ui.my.local;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import org.ww.dpplayer.R;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.ui.data.MusicViewModel;
import org.ww.dpplayer.ui.adapter.AlbumListAdapter;
import org.ww.dpplayer.ui.widget.SidebarView;

import java.util.ArrayList;
import java.util.List;

public class FragAlbumList extends Fragment implements AlbumListAdapter.OnItemClickListener, SidebarView.OnLetterClickedListener
{

    private RecyclerView recyclerView;
    private AlbumListAdapter adapter;
    private LottieAnimationView lottieAnimationView;
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
        lottieAnimationView = view.findViewById(R.id.lottieAnimationView);
        SidebarView sidebarView = view.findViewById(R.id.localAlbumSide);

        // 显示加载动画
        showLoadingAnimationLottie();

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
                applyFadeInAnimation();
                hideLoadingAnimationLottie();
            }
        });

        // 设置sideBar
        sidebarView.setOnLetterClickedListener(this);


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

    private void showLoadingAnimationLottie() {
        lottieAnimationView.setVisibility(View.VISIBLE);
        lottieAnimationView.setAnimation(R.raw.loading_anim_1);
        lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
        lottieAnimationView.playAnimation();
    }

    private void hideLoadingAnimationLottie() {
        // 创建一个 AlphaAnimation 对象，使透明度从 1.0 变为 0.0
        AlphaAnimation fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        fadeOutAnimation.setDuration(300); // 设置动画持续时间为 300 毫秒

        // 添加动画结束监听器，当动画结束时隐藏加载动画并停止动画播放
        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // 动画开始时不需要做任何操作
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                lottieAnimationView.setVisibility(View.GONE);
                lottieAnimationView.cancelAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // 动画重复时不需要做任何操作
            }
        });
        // 启动动画
        lottieAnimationView.startAnimation(fadeOutAnimation);
        // 延迟300ms再设置一次GONE以免动画没有被取消
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                lottieAnimationView.setVisibility(View.GONE);
            }
        }, 300);
    }

    private void applyFadeInAnimation() {
        AlphaAnimation fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
        fadeInAnimation.setDuration(1000); // Set duration as per your requirement
        recyclerView.startAnimation(fadeInAnimation);
    }
}
