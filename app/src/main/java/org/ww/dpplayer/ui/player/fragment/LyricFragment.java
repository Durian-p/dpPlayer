package org.ww.dpplayer.ui.player.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import me.wcy.lrcview.LrcView;
import org.ww.dpplayer.R;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LyricFragment extends Fragment
{
//    public LyricView lyricView;
    public LrcView lyricView;
    private String path;
    public LrcView getLyricView()
    {
        return lyricView;
    }
    public LrcView.OnPlayClickListener listener;

    public void setListener(LrcView.OnPlayClickListener listener)
    {
        this.listener = listener;
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
        lyricView.setCurrentColor(getResources().getColor(R.color.colorPrimary, null));
        showLyrics();
        lyricView.setDraggable(true, new LrcView.OnPlayClickListener()
        {
            @Override
            public boolean onPlayClick(LrcView view, long time)
            {
                return listener.onPlayClick(view, time);
            }
        });
    }

    public void showLyric(String lyric, boolean init)
    {
        if (init)
        {
//            lyricView.setTextSize(SPUtils.getFontSize(requireContext()));
//            lyricView.setHighLightTextColor(SPUtils.getFontColor(requireContext()));
//            lyricView.setTouchable(true);
//            lyricView.setOnPlayerClickListener((progress, content) -> {
//                // 如果正在播放，暂停
//                MusicServiceController.sendPlayPauseBroadcast(requireContext());
//            });
        }
//        lyricView.setLyricContent(lyric);
    }

    public void setCurrentTimeMilles(Long current)
    {
//        lyricView.setCurrentTimeMillis(current);
        lyricView.updateTime(current);
    }



    public void setCurrentTimeMillis(long progress)
    {
        // TODO: 歌词
    }

    public void showLyrics(File file, String charsetName)
    {
//        lyricView.setLyricFile(file, charsetName);
        lyricView.loadLrc(file);
    }

    public void showLyrics(File file, File filech, String charsetName)
    {
//        lyricView.setLyricFile(file, charsetName);
        lyricView.loadLrc(file, filech);
    }

    public void showLyrics()
    {

        try
        {
            if (path == null)
            {
                lyricView.loadLrc("");
                return;
            }
            Pattern MUSIC_FILE_PATTERN = Pattern.compile("(?i)\\.(mp3|wav|flac|m4a|aac)$");
            Matcher matcher = MUSIC_FILE_PATTERN.matcher(path);
            if (matcher.find())
            {
                File lyricFile = new File(matcher.replaceFirst(".lrc"));
                File lyricFile_ch = new File(matcher.replaceFirst(".ch.lrc"));

                    lyricView.loadLrc(lyricFile,  lyricFile_ch);

            }
        }
        catch (Exception e)
        {
            Log.d("PlayerActivity", "Error reading file: " + e.getMessage());
        }
    }



    public void setLyricPath(String path)
    {
        this.path = path;
    }
}
