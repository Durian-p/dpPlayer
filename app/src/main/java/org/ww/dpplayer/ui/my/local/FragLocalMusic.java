// FragLocalMusic.java
package org.ww.dpplayer.ui.my.local;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
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
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import org.ww.dpplayer.R;
import org.ww.dpplayer.database.MusicRepository;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.player.MusicService;
import org.ww.dpplayer.player.MusicServiceController;
import org.ww.dpplayer.ui.adapter.MusicListAdapter;
import org.ww.dpplayer.ui.base.BaseMusicActivity;
import org.ww.dpplayer.ui.base.DialogMusicLongPress;
import org.ww.dpplayer.ui.data.MusicViewModel;
import org.ww.dpplayer.ui.widget.SidebarView;
import org.ww.dpplayer.util.MusicLoader;

import java.util.List;
import java.util.Random;

public class FragLocalMusic extends Fragment implements MusicListAdapter.OnItemClickListener, SidebarView.OnLetterClickedListener {

    private LinearLayout llShufflePlay;
    private  BaseMusicActivity baseMusicActivity;
    private RecyclerView recyclerView;
    private MusicListAdapter adapter;
    private LottieAnimationView lottieAnimationView;

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
        SidebarView sidebarView = view.findViewById(R.id.localMusicSide);
        llShufflePlay = view.findViewById(R.id.llShufflePlay);
        lottieAnimationView = view.findViewById(R.id.lottieAnimationView);

        // data
        controller = new MusicServiceController(requireContext());
        controller.bindService();

        repository = MusicRepository.getInstance();

        // 显示加载动画
        showLoadingAnimationLottie();

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
                        DialogMusicLongPress dialog = new DialogMusicLongPress(musicList.get(position));
                        dialog.show(requireActivity().getSupportFragmentManager(), "dialog");
                    }
                });
                adapter.setOnItemLongClickListener(new MusicListAdapter.OnItemLongClickListener()
                {
                    @Override
                    public void onItemLongClick(int position)
                    {
                        DialogMusicLongPress dialog = new DialogMusicLongPress(musicList.get(position));
                        dialog.setOnItemDeleteListener(new DialogMusicLongPress.OnItemDeleteListener(){
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
                applyFadeInAnimation();
                hideLoadingAnimationLottie();
            }
        });

        // 设置sideBar
        sidebarView.setOnLetterClickedListener(this);


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

    private void showLoadingAnimationLottie() {
        lottieAnimationView.setVisibility(View.VISIBLE);
        lottieAnimationView.setAnimation(R.raw.loading_anim_1);
        lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
        lottieAnimationView.playAnimation();
    }

    private void hideLoadingAnimationLottie() {
        // 创建一个 AlphaAnimation 对象，使透明度从 1.0 变为 0.0
        AlphaAnimation fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        fadeOutAnimation.setDuration(400); // 设置动画持续时间为 500 毫秒

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
