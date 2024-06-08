package org.ww.dpplayer.ui.data;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import org.ww.dpplayer.database.MusicRepository;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.entity.MusicList;
import org.ww.dpplayer.util.MusicLoader;

import java.util.List;

public class MusicViewModel extends AndroidViewModel {
    private MutableLiveData<List<Music>> localMusicList;
    private MutableLiveData<List<Music>> heartMusicList;
    private MutableLiveData<List<Music>> playHistory;
    private MutableLiveData<List<MusicList>> musicList;
    private final MusicRepository musicRepository;
    private MusicRepository.MusicRepositoryListener musicRepositoryListener = new MusicRepository.MusicRepositoryListener() {
        @Override
        public void onHeartMusicChanged() {
        loadHeartMusics();
    }

        @Override
        public void onMusicListsChanged() {
            loadMusicList();
        }

        @Override
        public void onHistoryMusicChanged() { loadHistoryMusics(); }};

    public MusicViewModel(Application application) {
        super(application);
        musicRepository = MusicRepository.getInstance();
        musicRepository.setMusicRepositoryListener(musicRepositoryListener);
    }

    public void bindListenerToRepository()
    {
        musicRepository.setMusicRepositoryListener(musicRepositoryListener);
    }

    public LiveData<List<Music>> getLocalMusicList() {
        if (localMusicList == null)
        {
            localMusicList = new MutableLiveData<>();
            loadMusics();
        }
        return localMusicList;
    }

    public LiveData<List<Music>> getHeartMusicList() {
        if (heartMusicList == null)
        {
            heartMusicList = new MutableLiveData<>();
            loadHeartMusics();
        }
        return heartMusicList;
    }

    public LiveData<List<Music>> getHistoryMusicList() {
        if (playHistory == null)
        {
            playHistory = new MutableLiveData<>();
            loadHistoryMusics();
        }
        return playHistory;
    }

    public LiveData<List<MusicList>> getMusicLists() {
        if (musicList == null)
        {
            musicList = new MutableLiveData<>();
            loadMusicList();
        }
        return musicList;
    }

    public void loadMusicList()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<MusicList> musicList = musicRepository.getAllMusicLists();
                if (MusicViewModel.this.musicList == null)
                    MusicViewModel.this.musicList = new MutableLiveData<>();
                MusicViewModel.this.musicList.postValue(musicList);
            }
        }).start();
    }

    public void loadMusics() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Music> musics = MusicLoader.findLocalMusic(getApplication().getApplicationContext());
                if (localMusicList == null)
                    localMusicList = new MutableLiveData<>();
                localMusicList.postValue(musics);
            }
        }).start();
    }

    public void loadHeartMusics() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Music> musics = musicRepository.getAllHeartMusic();
                if (heartMusicList == null)
                    heartMusicList = new MutableLiveData<>();
                heartMusicList.postValue(musics);
            }
        }).start();
    }

    public void loadHistoryMusics(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Music> musics = musicRepository.getPlayHistory();
                if (playHistory == null)
                    playHistory = new MutableLiveData<>();
                playHistory.postValue(musics);
            }
        }).start();
    }
}
