package org.ww.testapp.entity;

import android.graphics.Bitmap;

public class Music {
    private Long id;
    private String title;
    private String album;
    private String artist;
    private int duration;
    private String path;

    private Bitmap albumArt;

    public Music(){}


    public Music(String title, String album, String artist) {
        this.title = title;
        this.album = album;
        this.artist = artist;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setAlbum(String album)
    {
        this.album = album;
    }

    public void setArtist(String artist)
    {
        this.artist = artist;
    }

    public void setAlbumArt(Bitmap albumArt)
    {
        this.albumArt = albumArt;
    }

    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public int getDuration()
    {
        return duration;
    }

    public Bitmap getAlbumArt()
    {
        return albumArt;
    }

    public Long getId()
    {
        return id;
    }

    public String getPath()
    {
        return path;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }
}
