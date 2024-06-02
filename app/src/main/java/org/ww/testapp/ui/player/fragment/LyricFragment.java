package org.ww.testapp.ui.player.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import org.ww.testapp.R;
import org.ww.testapp.player.MusicServiceController;
import org.ww.testapp.ui.widget.LyricView;
import org.ww.testapp.util.SPUtils;

public class LyricFragment extends Fragment
{
    public LyricView lyricView;

    public LyricView getLyricView()
    {
        return lyricView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.frag_player_lrcview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        lyricView = view.findViewById(R.id.lyricShow);
        showLyric("testestsetsetesse", true);
    }

    public void showLyric(String lyric, boolean init)
    {
        if (init)
        {
            lyricView.setTextSize(SPUtils.getFontSize(requireContext()));
            lyricView.setHighLightTextColor(SPUtils.getFontColor(requireContext()));
            lyricView.setTouchable(true);
            lyricView.setOnPlayerClickListener((progress, content) -> {
                // 如果正在播放，暂停
                MusicServiceController.sendPlayPauseBroadcast(requireContext());
            });
        }
        lyricView.setLyricContent(lyric);
    }

    public void setCurrentTimeMilles(Long current)
    {
        lyricView.setCurrentTimeMillis(current);
    }


    public void setCurrentTimeMillis(long progress)
    {
        // TODO: 歌词
    }
}
