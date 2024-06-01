package org.ww.testapp.player;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import org.ww.testapp.entity.Music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MusicService extends Service {

    public List<Music> getMusicList() {
        return playlist;
    }

    public int getCurrentMusicIndex() {
        return currentSongIndex;
    }


    public enum PlayerState {
        None,
        PLAYING,
        PAUSED
    }

    public enum PlayMode {
        SEQUENTIAL,
        SHUFFLE
    }

    private final IBinder binder = new LocalBinder();
    private ExoPlayer player;
    private List<Music> playlist = new ArrayList<>();
    private int currentSongIndex = 0;
    private final MutableLiveData<PlaybackInfo> playbackState = new MutableLiveData<>();
    private PlayerState currentPlayerState = PlayerState.None;
    private PlayMode currentPlayMode = PlayMode.SEQUENTIAL;
    private final Random random = new Random();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = new ExoPlayer.Builder(this).build();
        updatePlaybackState(PlayerState.None);
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    playNext();
                }
                updatePlaybackStateBasedOnPlayer();
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                updatePlaybackStateBasedOnPlayer();
            }
        });

        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicControlActions.ACTION_PLAY);
        filter.addAction(MusicControlActions.ACTION_PAUSE);
        filter.addAction(MusicControlActions.ACTION_NEXT);
        filter.addAction(MusicControlActions.ACTION_PREVIOUS);
        filter.addAction(MusicControlActions.ACTION_TOGGLE_PLAY_MODE);
        filter.addAction(MusicControlActions.ACTION_PLAY_PAUSE);
        registerReceiver(controlReceiver, filter, RECEIVER_EXPORTED);
    }

    private void updatePlaybackStateBasedOnPlayer() {
        PlayerState newState = player.isPlaying() ? PlayerState.PLAYING : PlayerState.PAUSED;
        if (currentPlayerState != newState) {
            currentPlayerState = newState;
            updatePlaybackState(newState);
        }
    }

    public void setCurrentMusic(int position) {
        if (playlist.size() == 0)
            return;
        int tempIndex = (position + playlist.size()) % playlist.size();
        Long tempId = playlist.get(tempIndex).getId();
        if (!Objects.equals(tempId, playlist.get(currentSongIndex).getId())) {
            currentSongIndex = tempIndex;
            player.setMediaItem(MediaItem.fromUri(playlist.get(currentSongIndex).getPath()));
        }
    }

    public void setPlaylist(List<Music> playlist) {
        if (playlist.size() == 0)
            return;
        this.playlist = playlist;
        currentSongIndex = 0;
        updatePlaybackState(PlayerState.PLAYING);
        player.setMediaItem(MediaItem.fromUri(playlist.get(currentSongIndex).getPath()));
    }

    public void setPlaylist(List<Music> playlist, int position) {
        if (playlist.size() == 0)
            return;
        this.playlist = playlist;
        currentSongIndex = (position + playlist.size()) % playlist.size();
        updatePlaybackState(PlayerState.PLAYING);
        player.setMediaItem(MediaItem.fromUri(playlist.get(currentSongIndex).getPath()));
    }

    public void playMedia() {
        if (playlist.isEmpty()) {
            return;
        }
        currentSongIndex = Math.max(currentSongIndex, 0);
        Music currentMusic = playlist.get(currentSongIndex);
        MediaItem mediaItem = MediaItem.fromUri(currentMusic.getPath());

        if (currentPlayerState == PlayerState.PAUSED) {
            player.play();
        } else {
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        }
    }

    public void playNext() {
        if (playlist.isEmpty()) {
            return;
        }
        if (currentPlayMode == PlayMode.SHUFFLE) {
            currentSongIndex = random.nextInt(playlist.size());
        } else {
            currentSongIndex = (currentSongIndex + 1) % playlist.size();
        }
        playMedia();
    }

    public void playPrev() {
        if (playlist.isEmpty()) {
            return;
        }
        if (currentPlayMode == PlayMode.SHUFFLE) {
            currentSongIndex = random.nextInt(playlist.size());
        } else {
            currentSongIndex = (currentSongIndex - 1 + playlist.size()) % playlist.size();
        }
        playMedia();
    }

    public void pauseMedia() {
        if (player.isPlaying()) {
            player.pause();
        }
    }

    public void stopMedia() {
        player.stop();
    }

    public Music getPrevMusic() {
        if (playlist.isEmpty()) {
            return null;
        }
        if (currentPlayMode == PlayMode.SHUFFLE) {
            return playlist.get(random.nextInt(playlist.size()));
        } else {
            return playlist.get((currentSongIndex - 1 + playlist.size()) % playlist.size());
        }
    }

    public Music getNextMusic() {
        if (playlist.isEmpty()) {
            return null;
        }
        if (currentPlayMode == PlayMode.SHUFFLE) {
            return playlist.get(random.nextInt(playlist.size()));
        } else {
            return playlist.get((currentSongIndex + 1) % playlist.size());
        }
    }


    public String getLyricInfo()
    {
        return "Test Lyric Test Lyric Test Lyric Test Lyric Test Lyric.";
    }

    public long getCurrentPosition() {
        if (player != null) {
            return player.getCurrentPosition();
        }
        return 0;
    }

    public long getDuration() {
        if (player != null) {
            return player.getDuration();
        }
        return 0;
    }

    public boolean isPlaying() {
        if (player != null) {
            return player.isPlaying();
        }
        return false;
    }
    public LiveData<PlaybackInfo> getPlaybackState() {
        return playbackState;
    }

    private void updatePlaybackState(PlayerState playerState) {
        Music currentMusic = null;
        if (!playlist.isEmpty()) {
            currentMusic = playlist.get(currentSongIndex);
        }
        PlaybackInfo state = new PlaybackInfo(
                player.getCurrentMediaItem() != null ? player.getCurrentMediaItem().mediaId : "",
                player.getCurrentPosition(),
                player.getDuration(),
                playerState,
                currentMusic,
                playlist,
                currentPlayMode
        );
        playbackState.postValue(state);
    }

    public void togglePlayMode() {
        if (currentPlayMode == PlayMode.SEQUENTIAL) {
            currentPlayMode = PlayMode.SHUFFLE;
        } else {
            currentPlayMode = PlayMode.SEQUENTIAL;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
        unregisterReceiver(controlReceiver); // 取消注册广播接收器
    }

    private final BroadcastReceiver controlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case MusicControlActions.ACTION_PLAY:
                        playMedia();
                        break;
                    case MusicControlActions.ACTION_PAUSE:
                        pauseMedia();
                        break;
                    case MusicControlActions.ACTION_NEXT:
                        playNext();
                        break;
                    case MusicControlActions.ACTION_PREVIOUS:
                        playPrev();
                        break;
                    case MusicControlActions.ACTION_TOGGLE_PLAY_MODE:
                        togglePlayMode();
                        break;
                }
            }
        }
    };

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public static class PlaybackInfo {
        public final String mediaId;
        public final long currentPosition;
        public final long duration;
        public final PlayerState state;
        public Music music;
        public List<Music> playList;
        public PlayMode playMode;

        public PlaybackInfo(String mediaId, long currentPosition, long duration, PlayerState state, Music music, List<Music> playList, PlayMode playMode) {
            this.mediaId = mediaId;
            this.currentPosition = currentPosition;
            this.duration = duration;
            this.state = state;
            this.music = music;
            this.playList = playList;
            this.playMode = playMode;
        }
    }

    public static class MusicControlActions {
        public static final String ACTION_PLAY = "org.ww.testapp.ACTION_PLAY";
        public static final String ACTION_PAUSE = "org.ww.testapp.ACTION_PAUSE";
        public static final String ACTION_NEXT = "org.ww.testapp.ACTION_NEXT";
        public static final String ACTION_PREVIOUS = "org.ww.testapp.ACTION_PREVIOUS";
        public static final String ACTION_TOGGLE_PLAY_MODE = "org.ww.testapp.ACTION_TOGGLE_PLAY_MODE";

        public static final String ACTION_PLAY_PAUSE = "org.ww.testapp.ACTION_PLAY_PAUSE";
    }
}
