package org.ww.dpplayer.ui.index;

import android.animation.ArgbEvaluator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.*;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;
import android.util.Pair;
import android.view.*;

import android.view.translation.ViewTranslationCallback;
import android.widget.Toast;
import android.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import org.ww.dpplayer.R;
import org.ww.dpplayer.database.MusicRepository;
import org.ww.dpplayer.databinding.FragIndexBinding;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.player.MusicService;
import org.ww.dpplayer.player.MusicServiceController;
import org.ww.dpplayer.ui.adapter.HomeSectionAdapter;
import org.ww.dpplayer.ui.base.BaseMusicActivity;
import org.ww.dpplayer.ui.my.heart.HeartActivity;
import org.ww.dpplayer.ui.my.history.HistoryActivity;
import org.ww.dpplayer.ui.widget.AccentIcon;
import org.ww.dpplayer.ui.widget.TopAppBarLayout;
import org.ww.dpplayer.ui.widget.insets.InsetsRecyclerView;
import org.ww.dpplayer.util.AnimUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

public class FragIndex extends Fragment {

    private FragIndexBinding binding;
    private List<Music> localMusics;
    private List<Music> musicsSuList;
    private List<Music> musicsWithAlbum;
    private NestedScrollView nestedScrollView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private int flag = 0;

    public static Fragment newInstance() {
        return new FragIndex();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragIndexBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        localMusics = MusicRepository.getInstance().getAllLocalMusic();
        Collections.shuffle(localMusics);

        nestedScrollView = view.findViewById(R.id.container);
        ShapeableImageView userImage = view.findViewById(R.id.userImage);
        MaterialTextView textView = view.findViewById(R.id.titleWelcome);
        AppCompatImageView bannerImage = view.findViewById(R.id.bannerImage);
        AccentIcon refreshBtn = view.findViewById(R.id.refresh_button);
        MaterialButton historyBtn = view.findViewById(R.id.history);
        MaterialButton topBtn = view.findViewById(R.id.topPlayed);
        MaterialButton lastAddedBtn = view.findViewById(R.id.lastAdded);
        MaterialButton shuffleBtn = view.findViewById(R.id.actionShuffle);
        InsetsRecyclerView recycler = view.findViewById(R.id.recyclerView);
        MaterialCardView cardMsg = view.findViewById(R.id.cardMsg);
        MaterialCardView[] suMusicCVs = new MaterialCardView[8];
        AppCompatImageView[] suMusicImages = new AppCompatImageView[8];
        for (int i = 1; i <= 8; i++) {
            suMusicCVs[i - 1] = view.findViewById(getResources().getIdentifier("card" + i, "id", getContext().getPackageName()));
            suMusicImages[i - 1] = view.findViewById(getResources().getIdentifier("image" + i, "id", getContext().getPackageName()));
        }

        userImage.setImageResource(R.drawable.my_avatar);
        // 根据时间显示欢迎词
        LocalDateTime now = LocalDateTime.now();
        if (now.getHour() < 9 || now.getHour() >= 18) {
            textView.setText("晚上好，durianp");
        } else if (now.getHour() < 12 && now.getHour() >= 9) {
            textView.setText("上午好，durianp");
        } else {
            textView.setText("下午好，durianp");
        }
        bannerImage.setImageResource(R.drawable.banner);

        AppBarLayout appBarLayout = view.findViewById(R.id.app_bar_layout);
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        collapsingToolbarLayout = view.findViewById(R.id.collapsing_toolbar_layout);
//
//        // 设置View内容
//        toolbar.setTitle(appNameSpannable);
//        collapsingToolbarLayout.setTitle(appNameSpannable);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) requireActivity()).getWindow().setStatusBarColor(getResources().getColor(R.color.transparent, null));
        collapsingToolbarLayout.setExpandedTitleTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorOnPrimary, null)));
        collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary, null));
        collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.white, null));
//        collapsingToolbarLayout.setExpandedTitleMarginStart(54);

        toolbar.setTitleTextColor(getResources().getColor(R.color.colorOnPrimary, null));
        toolbar.setNavigationIconTint(getResources().getColor(R.color.transparent, null));

        int maxScrollRange = appBarLayout.getTotalScrollRange();
//        int colorPrimary = getResources().getColor(R.color.colorPrimary, null);
//        int colorBlack = getResources().getColor(R.color.black, null);
//        int colorWhite = getResources().getColor(R.color.white, null);
//        int backgroundColor = Color.parseColor("#e9f3f2");
//        ArgbEvaluator argbEvaluator = new ArgbEvaluator();
//
//        String padding = "    ";
//        collapsingToolbarLayout.setTitle(Html.fromHtml(
//                "<font face='sans' color='" + colorPrimary + "'>dp</font>" +
//                        "<font face='sans' color='" + colorBlack + "'>" + "Player" +"</font>",
//                Html.FROM_HTML_MODE_LEGACY
//        ));
//
//        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
//            @Override
//            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//                int absVerticalOffset = Math.abs(verticalOffset);
//
//                if (Math.abs(verticalOffset)-appBarLayout.getTotalScrollRange() < -130) {
//                    // Expanded
//                    String playerStr = ((Spanned) collapsingToolbarLayout.getTitle()).toString();
//                    playerStr = playerStr.replace("dp","");
//                    if (playerStr.endsWith(" "))
//                        playerStr = playerStr.replace(" ","");
//                    else
//                        playerStr += ' ';
//                    collapsingToolbarLayout.setTitle(Html.fromHtml(
//                            "<font face='sans' color='" + colorPrimary + "'>dp</font>" +
//                                    "<font face='sans' color='" + colorBlack + "'>" +playerStr+"</font>",
//                            Html.FROM_HTML_MODE_LEGACY
//                    ));
//                    toolbar.setNavigationIconTint(colorPrimary);
//                    requireActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
//                    toolbar.setNavigationIconTint(Color.TRANSPARENT);
//
//
//                } else if (Math.abs(verticalOffset)-appBarLayout.getTotalScrollRange() == 0) {
//                    // Collapsed
//                    String playerStr = ((Spanned) collapsingToolbarLayout.getTitle()).toString();
//                    playerStr = playerStr.replace("dp","");
//                    if (playerStr.endsWith(" "))
//                        playerStr = playerStr.replace(" ","");
//                    else
//                        playerStr += ' ';
//                    collapsingToolbarLayout.setTitle(Html.fromHtml(
//                            "<font face='sans' color='" + colorWhite + "'>dp</font>" +
//                                    "<font face='sans' color='" + colorWhite + "'>"+ playerStr +"</font>",
//                            Html.FROM_HTML_MODE_LEGACY
//                    ));
//                    toolbar.setNavigationIconTint(colorWhite);
//                    collapsingToolbarLayout.setContentScrimColor(colorPrimary);
//                    requireActivity().getWindow().setStatusBarColor(colorPrimary);
//                } else {
//                    // In between
//
//                    float fraction = (float) absVerticalOffset / 132.0f;
//                    float adjustedFraction = (float) Math.pow(fraction, 2);
//                    int dpColor = (int) argbEvaluator.evaluate(fraction, colorPrimary, colorWhite);
//                    int playerColor = (int) argbEvaluator.evaluate(fraction, colorBlack, colorWhite);
//                    int statusBarColor = (int) argbEvaluator.evaluate(adjustedFraction, backgroundColor, colorPrimary);
//                    String playerStr = ((Spanned) collapsingToolbarLayout.getTitle()).toString();
//                    playerStr = playerStr.replace("dp","");
//                    if (playerStr.endsWith(" "))
//                        playerStr = playerStr.replace(" ","");
//                    else
//                        playerStr += ' ';
//                    collapsingToolbarLayout.setTitle(Html.fromHtml(
//                            "<font face='sans' color='" + String.format("#%06X", (0xFFFFFF & dpColor)) + "'>dp</font>" +
//                                    "<font face='sans' color='" + String.format("#%06X", (0xFFFFFF & playerColor)) + "'>"+ playerStr + "</font>",
//                            Html.FROM_HTML_MODE_LEGACY
//                    ));
//                    requireActivity().getWindow().setStatusBarColor(statusBarColor);
//                    collapsingToolbarLayout.setContentScrimColor(statusBarColor);
//                    toolbar.setNavigationIconTint(Color.TRANSPARENT);
//                }
//            }
//        });

        int colorPrimary = getResources().getColor(R.color.colorPrimary, null);
        int colorBlack = getResources().getColor(R.color.black, null);
        int colorWhite = getResources().getColor(R.color.white, null);
        int backgroundColor = Color.parseColor("#e9f3f2");
        ArgbEvaluator argbEvaluator = new ArgbEvaluator();

        Typeface sansTypeface = ResourcesCompat.getFont(requireContext(), R.font.sans);

        collapsingToolbarLayout.setTitle(buildStyledTitle("dp", "Player", colorPrimary, colorBlack, sansTypeface));

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int absVerticalOffset = Math.abs(verticalOffset);

                if (absVerticalOffset - appBarLayout.getTotalScrollRange() < -130) {
                    // Expanded
                    String playerStr = ((Spanned) collapsingToolbarLayout.getTitle()).toString();
                    playerStr = playerStr.replace("dp","");
                    if (playerStr.endsWith(" "))
                        playerStr = playerStr.replace(" ","");
                    else
                        playerStr += ' ';
                    collapsingToolbarLayout.setTitle(buildStyledTitle("dp", playerStr, colorPrimary, colorBlack, sansTypeface));
                    toolbar.setNavigationIconTint(colorPrimary);
                    requireActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
                    toolbar.setNavigationIconTint(Color.TRANSPARENT);

                } else if (absVerticalOffset - appBarLayout.getTotalScrollRange() == 0) {
                    // Collapsed
                    String playerStr = ((Spanned) collapsingToolbarLayout.getTitle()).toString();
                    playerStr = playerStr.replace("dp","");
                    if (playerStr.endsWith(" "))
                        playerStr = playerStr.replace(" ","");
                    else
                        playerStr += ' ';
                    collapsingToolbarLayout.setTitle(buildStyledTitle("dp", playerStr, colorWhite, colorWhite, sansTypeface));
                    toolbar.setNavigationIconTint(colorWhite);
                    collapsingToolbarLayout.setContentScrimColor(colorPrimary);
                    requireActivity().getWindow().setStatusBarColor(colorPrimary);

                } else {
                    // In between
                    float fraction = (float) absVerticalOffset / 132.0f;
                    float adjustedFraction = (float) Math.pow(fraction, 2);
                    int dpColor = (int) argbEvaluator.evaluate(fraction, colorPrimary, colorWhite);
                    int playerColor = (int) argbEvaluator.evaluate(fraction, colorBlack, colorWhite);
                    int statusBarColor = (int) argbEvaluator.evaluate(adjustedFraction, backgroundColor, colorPrimary);

                    String playerStr = ((Spanned) collapsingToolbarLayout.getTitle()).toString();
                    playerStr = playerStr.replace("dp","");
                    if (playerStr.endsWith(" "))
                        playerStr = playerStr.replace(" ","");
                    else
                        playerStr += ' ';

                    collapsingToolbarLayout.setTitle(buildStyledTitle("dp", playerStr, dpColor, playerColor, sansTypeface));
                    requireActivity().getWindow().setStatusBarColor(statusBarColor);
                    collapsingToolbarLayout.setContentScrimColor(statusBarColor);
                    toolbar.setNavigationIconTint(Color.TRANSPARENT);
                }
            }
        });


        // 初始化推荐曲目
        musicsSuList = new ArrayList<>();
        musicsWithAlbum = new ArrayList<>();
        for (Music music : localMusics) {
            if (music.getAlbumArt() != null) {
                musicsWithAlbum.add(music);
                if (musicsSuList.size() < 8) {
                    musicsSuList.add(music);
                }
            }
        }

        if (musicsSuList.size() < 8) {
            for (Music music : localMusics) {
                if (music.getAlbumArt() == null) {
                    musicsSuList.add(music);
                    if (musicsSuList.size() == 8) {
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < 8; i++) {
            if (i < musicsSuList.size()) {
                setSuCard(suMusicImages[i], suMusicCVs[i], i);
            } else {
                setSuCard(suMusicImages[i], suMusicCVs[i], i % musicsSuList.size());
            }
        }

        // 刷新推荐曲目
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshRecommendations(suMusicImages, suMusicCVs);
            }
        });

        // 播放推荐曲目
        cardMsg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((BaseMusicActivity) requireActivity()).updateServiceMusicList(musicsSuList, 0);
                MusicServiceController.sendPlayBroadcast(requireContext());
            }
        });

        // 快捷功能入口
        // 历史记录
        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), HistoryActivity.class);
                startActivity(intent);
            }
        });
        historyBtn.setOnTouchListener(AnimUtil.getTouchAnimListener());

        // 最近添加
        lastAddedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ActivityLastAdded.class);
                startActivity(intent);
            }
        });
        lastAddedBtn.setOnTouchListener(AnimUtil.getTouchAnimListener());

        // 最多播放
        topBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ActivityTopListened.class);
                startActivity(intent);
            }
        });
        topBtn.setOnTouchListener(AnimUtil.getTouchAnimListener());

        // 随机播放
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseMusicActivity baseActivity = (BaseMusicActivity) requireActivity();
                baseActivity.updateServiceMusicList(localMusics, 0);
                baseActivity.setPlayMode(MusicService.PlayMode.SHUFFLE);
                MusicServiceController.sendPlayBroadcast(requireContext());
            }
        });
        shuffleBtn.setOnTouchListener(AnimUtil.getTouchAnimListener());

        setupRecyclerView(recycler);
    }

    private SpannableString buildStyledTitle(String firstPart, String secondPart, int firstPartColor, int secondPartColor, Typeface typeface) {
        String title = firstPart + secondPart;
        SpannableString spannableString = new SpannableString(title);

        spannableString.setSpan(new ForegroundColorSpan(firstPartColor), 0, firstPart.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(secondPartColor), firstPart.length(), title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new TypefaceSpan(typeface), 0, firstPart.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new TypefaceSpan(typeface), firstPart.length(), title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }


    private void updateTitleColors(int dpColor, int playerColor) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString dpSpan = new SpannableString("dp");
        dpSpan.setSpan(new ForegroundColorSpan(dpColor), 0, dpSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        StringBuilder player = new StringBuilder("Player");
        for (int i = 0;i < flag;i++)
            player.append(' ');
        SpannableString playerSpan = new SpannableString(player.toString());
        playerSpan.setSpan(new ForegroundColorSpan(playerColor), 0, playerSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(dpSpan).append(playerSpan);

        collapsingToolbarLayout.setTitle(builder);
    }

    private void refreshRecommendations(AppCompatImageView[] suMusicImages, MaterialCardView[] suMusicCVs) {
        // 保存当前滚动位置
        final int scrollX = nestedScrollView.getScrollX();
        final int scrollY = nestedScrollView.getScrollY();

        musicsSuList.clear();
        List<Music> remainingMusicsWithAlbum = new ArrayList<>(musicsWithAlbum);

        Collections.shuffle(remainingMusicsWithAlbum);
        for (Music music : remainingMusicsWithAlbum) {
            if (!musicsSuList.contains(music)) {
                musicsSuList.add(music);
                if (musicsSuList.size() == 8) {
                    break;
                }
            }
        }

        if (musicsSuList.size() < 8) {
            List<Music> remainingLocalMusics = new ArrayList<>(localMusics);
            remainingLocalMusics.removeAll(musicsSuList);
            Collections.shuffle(remainingLocalMusics);
            for (Music music : remainingLocalMusics) {
                musicsSuList.add(music);
                if (musicsSuList.size() == 8) {
                    break;
                }
            }
        }

        for (int i = 0; i < 8; i++) {
            if (i < musicsSuList.size()) {
                setSuCard(suMusicImages[i], suMusicCVs[i], i);
            } else {
                setSuCard(suMusicImages[i], suMusicCVs[i], i % musicsSuList.size());
            }
        }

        // 恢复滚动位置
        nestedScrollView.post(new Runnable() {
            @Override
            public void run() {
                nestedScrollView.scrollTo(scrollX, scrollY);
            }
        });

    }

    private void setSuCard(AppCompatImageView suMusicImage, MaterialCardView suMusicCV, int index) {
        Music music = musicsSuList.get(index);
        if (music.getAlbumArt() != null) {
            suMusicImage.setImageBitmap(music.getAlbumArt());
        } else {
            suMusicImage.setImageResource(R.drawable.default_cover);
        }
        suMusicCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseMusicActivity) requireActivity()).updateServiceMusicList(musicsSuList, index);
                MusicServiceController.sendPlayBroadcast(requireContext());
            }
        });
        suMusicCV.setOnTouchListener(AnimUtil.getTouchAnimListener());
    }

    private void setupRecyclerView(InsetsRecyclerView recyclerView) {
        Map<String, List<Music>> sectionData = new HashMap<>();

        List<Music> albumList = new ArrayList<>();
        List<Music> artistList = new ArrayList<>();
        List<Music> recentPlayList = MusicRepository.getInstance().getRecent10Songs();

        // Populate albumList and artistList
        for (Music music : recentPlayList) {
            if (music.getAlbum() != null && !music.getAlbum().isEmpty()) {
                albumList.add(music);
            }
            if (music.getArtist() != null && !music.getArtist().isEmpty()) {
                artistList.add(music);
            }
        }

        sectionData.put("Albums", albumList);
        sectionData.put("Artists", artistList);

        HomeSectionAdapter sectionAdapter = new HomeSectionAdapter(sectionData);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(sectionAdapter);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
