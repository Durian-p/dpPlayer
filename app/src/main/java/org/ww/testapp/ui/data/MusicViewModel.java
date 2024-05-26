package org.ww.testapp.ui.data;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import org.ww.testapp.entity.Music;
import org.ww.testapp.util.MusicLoader;

import java.util.List;

public class MusicViewModel extends AndroidViewModel {
    private MutableLiveData<List<Music>> musicList;

    public MusicViewModel(Application application) {
        super(application);
    }

    public LiveData<List<Music>> getMusicList() {
        if (musicList == null) {
            musicList = new MutableLiveData<>();
            loadMusics();
        }
        return musicList;
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
}


