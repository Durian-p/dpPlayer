package org.ww.dpplayer.ui.data;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import org.ww.dpplayer.database.MusicRepository;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.util.MusicLoader;

import java.util.List;

public class MusicViewModel extends AndroidViewModel {
    private MutableLiveData<List<Music>> musicList;
    private MutableLiveData<List<Music>> heartMusicList;
    private final MusicRepository musicRepository;

    public MusicViewModel(Application application) {
        super(application);
        musicRepository = MusicRepository.getInstance();
        musicRepository.setMusicRepositoryListener(new MusicRepository.MusicRepositoryListener() {
            @Override
            public void onHeartMusicChanged() {
                loadHeartMusics();
            }
        });
    }

    public LiveData<List<Music>> getMusicList() {
        if (musicList == null) {
            musicList = new MutableLiveData<>();
            loadMusics();
        }
        return musicList;
    }

    public LiveData<List<Music>> getHeartMusicList() {
        if (heartMusicList == null) {
            heartMusicList = new MutableLiveData<>();
            loadHeartMusics();
        }
        return heartMusicList;
    }

    private void loadMusics() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Music> musics = MusicLoader.findLocalMusic(getApplication().getApplicationContext());
                musicList.postValue(musics);
            }
        }).start();
    }

    private void loadHeartMusics() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Music> musics = musicRepository.getAllHeartMusic();
                heartMusicList.postValue(musics);
            }
        }).start();
    }
}
