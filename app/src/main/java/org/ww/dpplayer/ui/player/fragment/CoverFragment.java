package org.ww.dpplayer.ui.player.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;
import org.ww.dpplayer.R;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.ui.player.PlayerActivity;
import org.ww.dpplayer.util.MusicLoader;
import android.util.Log;

public class CoverFragment extends Fragment {

    private CircleImageView coverView;
    private CircleImageView cover2View;

    private String coverPath;
    private Bitmap currentCover;
    private Music playingMusic;
    private Boolean isInitAnimator = false;

    private ObjectAnimator coverAnimator;
    private ObjectAnimator objectAnimator1;
    private ObjectAnimator objectAnimator2;
    private ObjectAnimator objectAnimator3;
    private AnimatorSet animatorSet;

    public CoverFragment(){}

    public CoverFragment(String coverPath) {
        this.coverPath = coverPath;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.frag_player_coverview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        coverView = view.findViewById(R.id.coverView);
        cover2View = view.findViewById(R.id.cover2View);

        updateViews();
        initAnimators();
        ((PlayerActivity) getActivity()).notifyCoverFragmentReady();
    }

    public void setCover(String coverPath)
    {
        this.coverPath = coverPath;
        this.currentCover = MusicLoader.getAlbumArt(coverPath);
    }

    public void setPlayingMusic(Music playingMusic)
    {
        this.playingMusic = playingMusic;
        setCover(playingMusic.getPath());
    }

    public void updateViews() {
        if (currentCover != null)
            coverView.setImageBitmap(currentCover);
        else
            coverView.setImageResource(R.drawable.default_cover);
        coverView.setVisibility(View.VISIBLE);
        if (currentCover == null) {
            cover2View.setVisibility(View.GONE);
            if (currentCover != null)
                cover2View.setImageBitmap(currentCover);
            else
                cover2View.setImageResource(R.drawable.default_cover);
        }
    }

    private void initAnimators() {
        coverAnimator = ObjectAnimator.ofFloat(cover2View, "rotation", 0f, 359f);
        coverAnimator.setDuration(20 * 1000);
        coverAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        coverAnimator.setRepeatMode(ObjectAnimator.RESTART);
        coverAnimator.setInterpolator(new LinearInterpolator());
        coverAnimator.addUpdateListener(animation -> {
            coverView.setRotation((float) animation.getAnimatedValue());
            cover2View.setRotation((float) animation.getAnimatedValue());
        });

        objectAnimator1 = ObjectAnimator.ofFloat(cover2View, "scaleX", 1f, 0.7f);
        objectAnimator1.setDuration(500L);
        objectAnimator1.setInterpolator(new AccelerateInterpolator());
        objectAnimator1.addUpdateListener(animation -> {
            cover2View.setScaleY((float) animation.getAnimatedValue());
            cover2View.setScaleX((float) animation.getAnimatedValue());
        });
        objectAnimator1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d("objectAnimator", "objectAnimator1 动画结束");
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationStart(Animator animation) {
                coverView.setAlpha(0f);
                cover2View.setTranslationY(0f);
                cover2View.setVisibility(View.VISIBLE);
                Log.d("objectAnimator", "objectAnimator1 动画开始");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        objectAnimator3 = ObjectAnimator.ofFloat(coverView, "alpha", 0f, 1f);
        objectAnimator3.setDuration(300L);
        objectAnimator3.addUpdateListener(animation -> coverView.setAlpha((float) animation.getAnimatedValue()));
        objectAnimator3.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                coverView.setAlpha(1f);
                Log.d("objectAnimator", "objectAnimator3 onAnimationEnd");
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                coverView.setAlpha(1f);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                Log.d("objectAnimator", "objectAnimator3 动画开始");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        objectAnimator2 = ObjectAnimator.ofFloat(cover2View, "translationY", 0f, -1000f);
        objectAnimator2.setDuration(300L);
        objectAnimator2.setInterpolator(new AccelerateInterpolator());
        objectAnimator2.addUpdateListener(animation -> cover2View.setTranslationY((float) animation.getAnimatedValue()));
        objectAnimator2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                cover2View.setTranslationY(0f);
                cover2View.setImageBitmap(currentCover);
                cover2View.setVisibility(View.GONE);
                Log.d("objectAnimator", "objectAnimator2 onAnimationEnd");
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                coverView.setAlpha(1f);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                Log.d("objectAnimator", "objectAnimator2 动画开始");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        animatorSet = new AnimatorSet();
        animatorSet.play(objectAnimator2).with(objectAnimator3).after(objectAnimator1);

        startAnimation();
    }

    private void startAnimation() {
        coverAnimator.start();
        animatorSet.start();
    }

    private void stopAnimation() {
        coverAnimator.end();
        animatorSet.end();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAnimation();
    }

    public void startRotateAnimation(boolean isPlaying) {
        if (isPlaying) {
            coverAnimator.pause();
            coverAnimator.start();
        }
        if (isInitAnimator) {
            cover2View.setVisibility(View.VISIBLE);
            animatorSet.pause();
            animatorSet.start();
        } else {
            isInitAnimator = true;
            cover2View.setVisibility(View.GONE);
        }
    }

    public void stopRotateAnimation() {
        coverAnimator.pause();
    }

    public void resumeRotateAnimation() {
        if (coverAnimator.isStarted()) {
            coverAnimator.resume();
        } else {
            coverAnimator.start();
        }
    }
}