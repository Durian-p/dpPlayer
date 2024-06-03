package org.ww.dpplayer.ui.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;
import org.ww.dpplayer.R;
import org.ww.dpplayer.entity.Music;

import java.util.Objects;

public class MusicUpdater
{
    private TextView songTitleTextView;
    private TextView albumArtistTextView;
    private ImageView albumCoverView;

    private Context context;

    public MusicUpdater(Context context, TextView songTitleTextView, TextView albumArtistTextView, ImageView albumCoverView) {
        this.context = context;
        this.songTitleTextView = songTitleTextView;
        this.albumArtistTextView = albumArtistTextView;
        this.albumCoverView = albumCoverView;
    }

    @SuppressLint("SetTextI18n")
    public void updateMusicInfo(final Music music) {
        String songTitle = music.getTitle();
        String songArtist = music.getArtist();
        String songAlbum = music.getAlbum();
        ((Activity) context).runOnUiThread(() ->
        {
            if (songTitleTextView != null) {
                songTitleTextView.setText(songTitle);
            }
            if (albumArtistTextView != null) {
                if (Objects.equals(songArtist, ""))
                    albumArtistTextView.setText("");
                else
                    albumArtistTextView.setText(" - " + songArtist);
            }
            Bitmap albumArt = music.getAlbumArt();
            if (albumArt != null) {
                albumCoverView.setImageBitmap(albumArt);
            } else {
                // 如果没有专辑封面，则使用默认图标
                albumCoverView.setImageResource(R.drawable.default_cover);
            }
        });
    }

    public void updateMusicInfo(final String songTitle, final String songArtist, final String songAlbum) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                if (songTitleTextView != null) {
                    songTitleTextView.setText(songTitle);
                }
                if (albumArtistTextView != null) {
                    albumArtistTextView.setText(" - " + songArtist);
                }
            }
        });
    }
}