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
import org.ww.testapp.R;
import org.ww.testapp.entity.Music;

import java.util.*;

public class MusicService extends Service
{


    public void seekTo(int progress)
    {
        player.seekTo(progress);
    }

    public enum PlayerState
    {
        None,
        PLAYING,
        PAUSED;
    }

    public enum PlayMode
    {
        SEQUENTIAL,
        SHUFFLE,
        REPEAT_ONE;
    }

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        player = new ExoPlayer.Builder(this).build();
        updatePlaybackState(PlayerState.None);
        player.addListener(new Player.Listener()
        {
            @Override
            public void onPlaybackStateChanged(int state)
            {
                if (state == Player.STATE_ENDED)
                {
                    if (currentPlayMode == PlayMode.REPEAT_ONE)
                    {
                        player.seekTo(0); // Seek to the beginning of the current track
                        player.play(); // Play the current track again
                    } else
                    {
                        playNext();
                    }
                }
                updatePlaybackStateBasedOnPlayer();
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying)
            {
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

    private void updatePlaybackStateBasedOnPlayer()
    {
        PlayerState newState = player.isPlaying() ? PlayerState.PLAYING : PlayerState.PAUSED;
        if (currentPlayerState != newState)
        {
            currentPlayerState = newState;
            updatePlaybackState(newState);
        }
    }

    public void setCurrentMusic(int position)
    {
        if (originalPlaylist.size() == 0)
            return;
        List<Music> currentList = getPlayingMusicList();
        int tempIndex = (position + currentList.size()) % currentList.size();
        Long tempId = currentList.get(tempIndex).getId();
        if (! Objects.equals(tempId, currentList.get(currentSongIndex).getId()))
        {
            currentSongIndex = tempIndex;
            player.setMediaItem(MediaItem.fromUri(currentList.get(currentSongIndex).getPath()));
        }
    }

    public void setCurrentMusicId(long id)
    {
        for (Music music : getPlayingMusicList())
        {
            if (music.getId() == id)
            {
                currentSongIndex = playingPlaylist.indexOf(music);
                player.setMediaItem(MediaItem.fromUri(music.getPath()));
                break;
            }
        }
    }

    public void setOriginalPlaylist(List<Music> originalPlaylist)
    {
        if (originalPlaylist.size() == 0)
            return;
        this.originalPlaylist = new ArrayList<>(originalPlaylist);
        this.playingPlaylist = new ArrayList<>(originalPlaylist);
        currentSongIndex = 0;
        updatePlaybackState(PlayerState.PLAYING);
        player.setMediaItem(MediaItem.fromUri(originalPlaylist.get(currentSongIndex).getPath()));
    }

    public void setPlaylist(List<Music> playlist, int position)
    {
        if (playlist.size() == 0)
            return;
        this.originalPlaylist = new ArrayList<>(playlist);
        this.playingPlaylist = new ArrayList<>(playlist);
        currentSongIndex = (position + playlist.size()) % playlist.size();
        updatePlaybackState(PlayerState.PLAYING);
        player.setMediaItem(MediaItem.fromUri(playlist.get(currentSongIndex).getPath()));
    }


    public void clearPlaylist()
    {
        originalPlaylist.clear();
        currentSongIndex = 0;
        updatePlaybackState(PlayerState.None);
    }

    public void playMedia()
    {
        if (originalPlaylist.isEmpty())
        {
            return;
        }
        currentSongIndex = Math.max(currentSongIndex, 0);
        Music currentMusic = originalPlaylist.get(currentSongIndex);
        MediaItem mediaItem = MediaItem.fromUri(currentMusic.getPath());

        if (currentPlayerState == PlayerState.PAUSED)
        {
            player.play();
        } else
        {
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        }
    }

    public void playNext()
    {
        if (originalPlaylist.isEmpty())
        {
            return;
        }
        List<Music> currentList = getPlayingMusicList();
        currentSongIndex = (currentSongIndex + 1) % currentList.size();
        playMedia();
    }

    public void playPrev()
    {
        if (originalPlaylist.isEmpty())
        {
            return;
        }
        List<Music> currentList = getPlayingMusicList();
        currentSongIndex = (currentSongIndex - 1 + currentList.size()) % currentList.size();
        playMedia();
    }

    public void pauseMedia()
    {
        if (player.isPlaying())
        {
            player.pause();
        }
    }

    public void stopMedia()
    {
        player.stop();
    }

    public int getCurrentMusicIndex()
    {
        return currentSongIndex;
    }

    public Music getPrevMusic()
    {
        if (originalPlaylist.isEmpty())
        {
            return null;
        }
        List<Music> currentList = getPlayingMusicList();
        if (currentPlayMode == PlayMode.SHUFFLE)
        {
            return currentList.get(random.nextInt(currentList.size()));
        } else
        {
            return currentList.get((currentSongIndex - 1 + currentList.size()) % currentList.size());
        }
    }

    public Music getNextMusic()
    {
        if (originalPlaylist.isEmpty())
        {
            return null;
        }
        List<Music> currentList = getPlayingMusicList();
        if (currentPlayMode == PlayMode.SHUFFLE)
        {
            return currentList.get(random.nextInt(currentList.size()));
        } else
        {
            return currentList.get((currentSongIndex + 1) % currentList.size());
        }
    }


    public String getLyricInfo()
    {
        return "Test Lyric Test Lyric Test Lyric Test Lyric Test Lyric.";
    }

    public long getCurrentProgress()
    {
        if (player != null)
        {
            return player.getCurrentPosition();
        }
        return 0;
    }

    public long getDuration()
    {
        if (player != null)
        {
            return player.getDuration();
        }
        return 0;
    }

    public boolean isPlaying()
    {
        if (player != null)
        {
            return player.isPlaying();
        }
        return false;
    }

    public LiveData<PlaybackInfo> getPlaybackState()
    {
        return playbackState;
    }

    private void updatePlaybackState(PlayerState playerState)
    {
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
    }

    public void togglePlayMode()
    {
        switch (currentPlayMode)
        {
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

    private void shufflePlaylist()
    {
        long id = originalPlaylist.get(currentSongIndex).getId();
        playingPlaylist = new ArrayList<>(originalPlaylist);
        Collections.shuffle(playingPlaylist, random);
        isShuffleMode = true;
        for (Music music : playingPlaylist)
        {
            if (music.getId() == id)
            {
                currentSongIndex = playingPlaylist.indexOf(music);
                break;
            }
        }
    }

    private void recoverPlayList()
    {
        long id = playingPlaylist.get(currentSongIndex).getId();
        playingPlaylist.clear();
        playingPlaylist.addAll(originalPlaylist);
        for (Music music : playingPlaylist)
        {
            if (music.getId() == id)
            {
                currentSongIndex = playingPlaylist.indexOf(music);
                break;
            }
        }
    }

    public List<Music> getPlayingMusicList()
    {
        return playingPlaylist;
    }

    public List<Music> getOriginalMusicList()
    {
        return originalPlaylist;
    }

    public Music getCurrentMusic()
    {
        List<Music> currentList = getPlayingMusicList();
        if (currentList.size() > 0)
            return getPlayingMusicList().get(currentSongIndex);
        else
            return null;
    }

    public static int getPlayModeIconId(PlayMode playMode)
    {
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
    public void onDestroy()
    {
        super.onDestroy();
        player.release();
        unregisterReceiver(controlReceiver); // 取消注册广播接收器
    }

    private final BroadcastReceiver controlReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action != null)
            {
                switch (action)
                {
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

    public class LocalBinder extends Binder
    {
        public MusicService getService()
        {
            return MusicService.this;
        }
    }

    public static class PlaybackInfo
    {
        public final String mediaId;
        public final long currentPosition;
        public final long duration;
        public final PlayerState state;
        public Music music;
        public List<Music> originalPlayList;
        public PlayMode playMode;
        public int currentSongIndex;

        public PlaybackInfo(String mediaId, long currentPosition, long duration, PlayerState state, Music music, List<Music> originalPlayList, PlayMode playMode, int currentSongIndex)
        {
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


    public static class MusicControlActions
    {
        public static final String ACTION_PLAY = "org.ww.testapp.ACTION_PLAY";
        public static final String ACTION_PAUSE = "org.ww.testapp.ACTION_PAUSE";
        public static final String ACTION_NEXT = "org.ww.testapp.ACTION_NEXT";
        public static final String ACTION_PREVIOUS = "org.ww.testapp.ACTION_PREVIOUS";
        public static final String ACTION_TOGGLE_PLAY_MODE = "org.ww.testapp.ACTION_TOGGLE_PLAY_MODE";

        public static final String ACTION_PLAY_PAUSE = "org.ww.testapp.ACTION_PLAY_PAUSE";
    }
}
