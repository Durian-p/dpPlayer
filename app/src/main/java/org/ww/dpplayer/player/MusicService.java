package org.ww.dpplayer.player;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import org.ww.dpplayer.R;
import org.ww.dpplayer.database.MusicRepository;
import org.ww.dpplayer.entity.Music;


import java.util.*;

public class MusicService extends Service {
    public enum PlayerState {
        None,
        PLAYING,
        PAUSED;
    }

    public enum PlayMode {
        SEQUENTIAL,
        SHUFFLE,
        REPEAT_ONE;
    }

    private static final String CHANNEL_ID = "music_playback_channel";
    private static final int NOTIFICATION_ID = 1;

    private final IBinder binder = new LocalBinder();
    private ExoPlayer player;
    private List<Music> originalPlaylist = new ArrayList<>();
    private List<Music> playingPlaylist = new ArrayList<>();
    private boolean isShuffleMode = false;

    private final Music defaultMusic = new Music("[无歌曲播放中]", "[无歌曲播放中]", "");
    private int currentSongIndex = 0;
    private final MutableLiveData<PlaybackInfo> playbackState = new MutableLiveData<>();
    private PlayerState currentPlayerState = PlayerState.None;
    private PlayMode currentPlayMode = PlayMode.SEQUENTIAL;
    private final Random random = new Random();

    private MusicRepository musicRepository; // Add MusicRepository

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
                    if (currentPlayMode == PlayMode.REPEAT_ONE) {
                        player.seekTo(0); // Seek to the beginning of the current track
                        player.play(); // Play the current track again
                        musicRepository.addPlayHistory(playingPlaylist.get(currentSongIndex).getId());
                    } else {
                        playNext();
                    }
                }
                updatePlaybackStateBasedOnPlayer();
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                updatePlaybackStateBasedOnPlayer();
            }
        });

        // Initialize MusicRepository
        musicRepository = MusicRepository.initInstance(getApplicationContext());

        // Register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicControlActions.ACTION_PLAY);
        filter.addAction(MusicControlActions.ACTION_PAUSE);
        filter.addAction(MusicControlActions.ACTION_NEXT);
        filter.addAction(MusicControlActions.ACTION_PREVIOUS);
        filter.addAction(MusicControlActions.ACTION_TOGGLE_PLAY_MODE);
        filter.addAction(MusicControlActions.ACTION_PLAY_PAUSE);
        filter.addAction(MusicControlActions.ACTION_CLOSE_NOTIFICATION);
        registerReceiver(controlReceiver, filter, RECEIVER_EXPORTED);

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        NotificationChannel channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Music playback controls");
            channel.setSound(null, null);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private PendingIntent createPendingIntent(String action) {
        Intent intent = new Intent(action);
        intent.setAction(action);
        int requestCode = action.hashCode(); // Use a unique request code for each action
        return PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }


    private void showNotification() {
        Music currentMusic = getCurrentMusic();
        Bitmap albumArt = currentMusic != null ? currentMusic.getAlbumArt() : null;

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notice_control_bar);
        contentView.setTextViewText(R.id.musicTitleTextView, currentMusic == null? "[无歌曲播放中]" :currentMusic.getTitle() + " - " + currentMusic.getArtist());
        if (albumArt != null)
            contentView.setImageViewBitmap(R.id.bgmmMusicImageView, albumArt);
        else
            contentView.setImageViewResource(R.id.bgmmMusicImageView, R.drawable.default_cover);
        contentView.setImageViewResource(R.id.audio_close_btn, R.drawable.ic_close);
        contentView.setImageViewResource(R.id.lastImageView, R.drawable.ic_prev);
        contentView.setImageViewResource(R.id.nextImageView, R.drawable.ic_next);
        if (player != null && player.isPlaying()) {
            contentView.setImageViewResource(R.id.stopImageView, R.drawable.ic_pause);
        } else {
            contentView.setImageViewResource(R.id.stopImageView, R.drawable.ic_play);
        }

        // 实现停止/播放
        contentView.setOnClickPendingIntent(R.id.stopImageView, createPendingIntent(MusicControlActions.ACTION_PLAY_PAUSE));
        //下一首事件
        contentView.setOnClickPendingIntent(R.id.nextImageView, createPendingIntent(MusicControlActions.ACTION_NEXT));
        //上一首事件
        contentView.setOnClickPendingIntent(R.id.lastImageView, createPendingIntent(MusicControlActions.ACTION_PREVIOUS));
        // 关闭通知栏
        contentView.setOnClickPendingIntent(R.id.audio_close_btn, createPendingIntent(MusicControlActions.ACTION_CLOSE_NOTIFICATION));


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setCustomContentView(contentView)
                        .setCustomBigContentView(contentView)
                                .setSmallIcon(R.drawable.ic_album);

        startForeground(NOTIFICATION_ID, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
    }

    private void updateNotification() {
        showNotification();
    }

    private void updatePlaybackStateBasedOnPlayer() {
        PlayerState newState = player.isPlaying() ? PlayerState.PLAYING : PlayerState.PAUSED;
        if (currentPlayerState != newState) {
            currentPlayerState = newState;
            updatePlaybackState(newState);
        }
    }

    public void setCurrentMusic(int position) {
        if (originalPlaylist.size() == 0)
            return;
        List<Music> currentList = getPlayingMusicList();
        int tempIndex = (position + currentList.size()) % currentList.size();
        Long tempId = currentList.get(tempIndex).getId();
        if (!Objects.equals(tempId, currentList.get(currentSongIndex).getId())) {
            currentSongIndex = tempIndex;
            setUpMedia(currentList.get(currentSongIndex));
        }
    }

    public void setCurrentMusicId(long id) {
        for (Music music : getPlayingMusicList()) {
            if (music.getId() == id) {
                currentSongIndex = playingPlaylist.indexOf(music);
                setUpMedia(music);
                break;
            }
        }
    }

    public void seekTo(long progress) {
        player.seekTo(progress);
    }

    public void setToNextPlay(Music music) {
        if (originalPlaylist.size() == 0)
            return;

        int index = -1;
        for (Music music1:playingPlaylist)
        {
            index++;
            if (Objects.equals(music1.getId(), music.getId()))
            {
                break;
            }
        }
        if (index != -1) {
            if (currentSongIndex == index)
                return;
            playingPlaylist.remove(index);
            playingPlaylist.add((currentSongIndex + 1) % playingPlaylist.size(), music);
        } else {
            playingPlaylist.add((currentSongIndex + 1) % playingPlaylist.size(), music);
            originalPlaylist.add(music);
        }

    }

    public void setOriginalPlaylist(List<Music> originalPlaylist) {
        if (originalPlaylist.size() == 0)
            return;

        this.originalPlaylist = new ArrayList<>(originalPlaylist);
        this.playingPlaylist = new ArrayList<>(originalPlaylist);
        if (currentPlayMode == PlayMode.SHUFFLE)
            Collections.shuffle(this.playingPlaylist);
        currentSongIndex = 0;
        updatePlaybackState(PlayerState.PLAYING);
        setUpMedia(originalPlaylist.get(currentSongIndex));
    }

    public void setPlaylist(List<Music> playlist, int position) {
        if (playlist.size() == 0)
            return;
        this.originalPlaylist = new ArrayList<>(playlist);
        this.playingPlaylist = new ArrayList<>(playlist);
        currentSongIndex = (position + playlist.size()) % playlist.size();
        updatePlaybackState(PlayerState.PLAYING);
        setUpMedia(playlist.get(currentSongIndex));
    }

    public void clearPlaylist() {
        originalPlaylist.clear();
        currentSongIndex = 0;
        updatePlaybackState(PlayerState.None);
    }

    public void playMedia() {
        if (playingPlaylist.isEmpty()) {
            return;
        }
        currentSongIndex = Math.max(currentSongIndex, 0);
        Music currentMusic = playingPlaylist.get(currentSongIndex);

        if (currentPlayerState == PlayerState.PAUSED) {
            player.play();
        } else {
            setUpMedia(currentMusic);
            player.prepare();
            player.play();
        }
    }

    public void resumePlay() {
        if (!player.isPlaying())
            player.play();
    }

    public void playMediaFromBegin() {
        if (playingPlaylist.isEmpty()) {
            return;
        }
        currentSongIndex = Math.max(currentSongIndex, 0);
        Music currentMusic = playingPlaylist.get(currentSongIndex);
        setUpMedia(currentMusic);
        player.prepare();
        player.play();
    }

    private void setUpMedia(Music music)
    {
        player.setMediaItem(MediaItem.fromUri(music.getPath()));

        // Add play history
        musicRepository.addPlayHistory(music.getId());
    }

    public void playNext() {
        if (originalPlaylist.isEmpty()) {
            return;
        }
        List<Music> currentList = getPlayingMusicList();
        currentSongIndex = (currentSongIndex + 1) % currentList.size();
        playMediaFromBegin();
    }

    public void playPrev() {
        if (originalPlaylist.isEmpty()) {
            return;
        }
        List<Music> currentList = getPlayingMusicList();
        currentSongIndex = (currentSongIndex - 1 + currentList.size()) % currentList.size();
        playMediaFromBegin();
    }

    public void pauseMedia() {
        if (player.isPlaying()) {
            player.pause();
        }
    }

    public void stopMedia() {
        player.stop();
    }

    public int getCurrentMusicIndex() {
        return currentSongIndex;
    }

    public Music getPrevMusic() {
        if (originalPlaylist.isEmpty()) {
            return null;
        }
        List<Music> currentList = getPlayingMusicList();
        if (currentPlayMode == PlayMode.SHUFFLE) {
            return currentList.get(random.nextInt(currentList.size()));
        } else {
            return currentList.get((currentSongIndex - 1 + currentList.size()) % currentList.size());
        }
    }

    public Music getNextMusic() {
        if (originalPlaylist.isEmpty()) {
            return null;
        }
        List<Music> currentList = getPlayingMusicList();
        if (currentPlayMode == PlayMode.SHUFFLE) {
            return currentList.get(random.nextInt(currentList.size()));
        } else {
            return currentList.get((currentSongIndex + 1) % currentList.size());
        }
    }

    public String getLyricInfo() {
        return "Test Lyric Test Lyric Test Lyric Test Lyric Test Lyric.";
    }

    public long getCurrentProgress() {
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
        PlaybackInfo state = new PlaybackInfo(
                player.getCurrentMediaItem() != null ? player.getCurrentMediaItem().mediaId : "",
                player.getCurrentPosition(),
                player.getDuration(),
                playerState,
                getCurrentMusic(),
                originalPlaylist,
                currentPlayMode,
                currentSongIndex
        );
        playbackState.postValue(state);

        updateNotification();
    }

    public void togglePlayMode() {
        switch (currentPlayMode) {
            case SEQUENTIAL:
                currentPlayMode = PlayMode.SHUFFLE;
                shufflePlaylist();
                break;
            case SHUFFLE:
                currentPlayMode = PlayMode.REPEAT_ONE;
                recoverPlayList();
                break;
            case REPEAT_ONE:
                currentPlayMode = PlayMode.SEQUENTIAL;
                break;
        }
        updatePlaybackState(currentPlayerState);
    }

    public void setPlayMode(PlayMode playMode) {
        if (playMode == currentPlayMode) return;
        switch (playMode) {
            case SEQUENTIAL:
                currentPlayMode = PlayMode.SEQUENTIAL;
                recoverPlayList();
                break;
            case SHUFFLE:
                currentPlayMode = PlayMode.SHUFFLE;
                shufflePlaylist();
                break;
            case REPEAT_ONE:
                currentPlayMode = PlayMode.REPEAT_ONE;
                recoverPlayList();
                break;
        }
    }

    private void shufflePlaylist() {
        long id = originalPlaylist.get(currentSongIndex).getId();
        playingPlaylist = new ArrayList<>(originalPlaylist);
        Collections.shuffle(playingPlaylist, random);
        isShuffleMode = true;
        for (Music music : playingPlaylist) {
            if (music.getId() == id) {
                currentSongIndex = playingPlaylist.indexOf(music);
                break;
            }
        }
    }

    private void recoverPlayList() {
        long id = playingPlaylist.get(currentSongIndex).getId();
        playingPlaylist.clear();
        playingPlaylist.addAll(originalPlaylist);
        for (Music music : playingPlaylist) {
            if (music.getId() == id) {
                currentSongIndex = playingPlaylist.indexOf(music);
                break;
            }
        }
    }

    public List<Music> getPlayingMusicList() {
        return playingPlaylist;
    }

    public List<Music> getOriginalMusicList() {
        return originalPlaylist;
    }

    public Music getCurrentMusic() {
        List<Music> currentList = getPlayingMusicList();
        if (currentList.size() > 0)
            return getPlayingMusicList().get(currentSongIndex);
        else
            return null;
    }

    public static int getPlayModeIconId(PlayMode playMode) {
        int ret;
        if (playMode == PlayMode.SEQUENTIAL)
            ret = R.drawable.ic_shuffle;
        else if (playMode == PlayMode.SHUFFLE)
            ret = R.drawable.ic_repeat;
        else if (playMode == PlayMode.REPEAT_ONE)
            ret = R.drawable.ic_repeat_one;
        else
            ret = R.drawable.ic_repeat;
        return ret;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
        unregisterReceiver(controlReceiver); // Unregister broadcast receiver
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
                    case MusicControlActions.ACTION_PLAY_PAUSE:
                        if (player.isPlaying()) {
                            pauseMedia();
                        } else {
                            playMedia();
                        }
                        break;
                    case MusicControlActions.ACTION_CLOSE_NOTIFICATION:
                        stopForeground(false); // Remove the notification but keep the service running
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(NOTIFICATION_ID); // Cancel the notification
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
        public List<Music> originalPlayList;
        public PlayMode playMode;
        public int currentSongIndex;

        public PlaybackInfo(String mediaId, long currentPosition, long duration, PlayerState state, Music music, List<Music> originalPlayList, PlayMode playMode, int currentSongIndex) {
            this.mediaId = mediaId;
            this.currentPosition = currentPosition;
            this.duration = duration;
            this.state = state;
            this.music = music;
            this.originalPlayList = originalPlayList;
            this.playMode = playMode;
            this.currentSongIndex = currentSongIndex;
        }
    }

    public static class MusicControlActions {
        public static final String ACTION_PLAY = "org.ww.dpplayer.ACTION_PLAY";
        public static final String ACTION_PAUSE = "org.ww.dpplayer.ACTION_PAUSE";
        public static final String ACTION_NEXT = "org.ww.dpplayer.ACTION_NEXT";
        public static final String ACTION_PREVIOUS = "org.ww.dpplayer.ACTION_PREVIOUS";
        public static final String ACTION_TOGGLE_PLAY_MODE = "org.ww.dpplayer.ACTION_TOGGLE_PLAY_MODE";
        public static final String ACTION_PLAY_PAUSE = "org.ww.dpplayer.ACTION_PLAY_PAUSE";
        public static final String ACTION_CLOSE_NOTIFICATION = "org.ww.dpplayer.ACTION_CLOSE_NOTIFICATION"; // New action for closing notification
    }

}
