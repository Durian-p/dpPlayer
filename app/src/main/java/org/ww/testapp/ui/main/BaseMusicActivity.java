package org.ww.testapp.ui.main;

import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.ww.testapp.player.MusicService;
import org.ww.testapp.ui.widget.PlayPauseView;

public class BaseMusicActivity  extends AppCompatActivity
{
    private ImageView albumCover;
    private TextView titleTextView;
    private TextView artistTextView;
    private PlayPauseView playPauseView;
    private MusicService musicService;
    private boolean isBound = false;
}
