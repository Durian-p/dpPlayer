package org.ww.dpplayer.entity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MusicList {
    private long id;
    private String name;
    private Bitmap cover;
    private List<String> musicIdList = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getCover() {
        return cover;
    }

    public void setCover(Bitmap cover) {
        this.cover = cover;
    }

    public List<String> getMusicIdList() {
        return musicIdList;
    }

    public void setMusicIdList(List<String> musicIdList) {
        this.musicIdList = musicIdList;
    }

    public byte[] getCoverAsByteArray() {
        if (cover != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            cover.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        }
        return null;
    }

    public void setCoverFromByteArray(byte[] byteArray) {
        if (byteArray != null && byteArray.length > 0) {
            this.cover = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        } else {
            this.cover = null;
        }
    }
}
