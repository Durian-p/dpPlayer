package org.ww.dpplayer.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import androidx.lifecycle.LiveData;

import org.ww.dpplayer.entity.Music;
import java.util.List;

public class MusicServiceController
{

    private MusicService musicService;
    private boolean bound = false;
    private final Context context;

    public void updateServiceMusicList(List<Music> localList, int i)
    {
        musicService.setPlaylist(localList, i);
    }

    public interface MusicServiceCallback {
        void onServiceConnected(ComponentName name, IBinder service);
        void onServiceDisconnected(ComponentName name);
    }
    private MusicServiceCallback callback;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            bound = true;
            if (callback != null)
                callback.onServiceConnected(name, service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
            if (callback != null)
                callback.onServiceDisconnected(name);
        }
    };

//    public static MusicServiceController getInstance() {
//        return new MusicServiceController(context);
//    }

    public MusicServiceController(Context context) {
        this.context = context;
    }

    public void bindService() {
        Intent intent = new Intent(context, MusicService.class);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public void bindService(MusicServiceCallback callback) {
        this.callback = callback;
        Intent intent = new Intent(context, MusicService.class);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public void unbindService() {
        if (bound) {
            context.unbindService(connection);
            bound = false;
        }
    }

    public void play() {
        if (bound && musicService != null) {
            musicService.playMedia();
        }
    }

    public void pause() {
        if (bound && musicService != null) {
            musicService.pauseMedia();
        }
    }

    public void next() {
        if (bound && musicService != null) {
            musicService.playNext();
        }
    }

    public void previous() {
        if (bound && musicService != null) {
            musicService.playPrev();
        }
    }

    public void togglePlayMode() {
        if (bound && musicService != null) {
            musicService.togglePlayMode();
        }
    }

    public void setPlayMode(MusicService.PlayMode playMode)
    {
        if (bound && musicService != null)
        {
            musicService.setPlayMode(playMode);
        }
    }

    public LiveData<MusicService.PlaybackInfo> getPlaybackState() {
        if (bound && musicService != null) {
            return musicService.getPlaybackState();
        }
        return null;
    }

    public void setCurrentMusic(int index)
    {
        musicService.setCurrentMusic(index);
    }

    public void setCurrentMusicId(long id)
    {
        musicService.setCurrentMusicId(id);
    }


    public List<Music> getMusicList() {
        if (bound && musicService != null) {
            return musicService.getPlayingMusicList();
        }
        return null;
    }

    public int getCurrentMusicIndex() {
        if (bound && musicService != null) {
            return musicService.getCurrentMusicIndex();
        }
        return -1;
    }

    public static void sendPlayBroadcast(Context context) {Intent intent = new Intent(MusicService.MusicControlActions.ACTION_PLAY);
        context.sendBroadcast(intent);
    }

    public static void sendPauseBroadcast(Context context) {
        Intent intent = new Intent(MusicService.MusicControlActions.ACTION_PAUSE);
        context.sendBroadcast(intent);
    }

    public static void sendNextBroadcast(Context context) {
        Intent intent = new Intent(MusicService.MusicControlActions.ACTION_NEXT);
        context.sendBroadcast(intent);
    }

    public static void sendPreviousBroadcast(Context context) {
        Intent intent = new Intent(MusicService.MusicControlActions.ACTION_PREVIOUS);
        context.sendBroadcast(intent);
    }

    public static void sendPlayPauseBroadcast(Context context){
        Intent intent = new Intent(MusicService.MusicControlActions.ACTION_PLAY_PAUSE);
        context.sendBroadcast(intent);
    }

    public static void sendTogglePlayModeBroadcast(Context context){
        Intent intent = new Intent(MusicService.MusicControlActions.ACTION_TOGGLE_PLAY_MODE);
        context.sendBroadcast(intent);
    }
}

