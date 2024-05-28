package org.ww.testapp.entity;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;


public class Music  implements Parcelable
{
    private Long id;
    private String title;
    private String album;
    private String artist;
    private int duration;
    private String path;

    private Bitmap albumArt;

    public static final Parcelable.Creator<Music> CREATOR = new Parcelable.Creator<Music>() {
        @NonNull
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @NonNull
        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    private Music(Parcel in) {
        id = in.readLong();
        title = in.readString();
        album = in.readString();
        artist = in.readString();
        duration = in.readInt();
        path = in.readString();
    }

    public Music(){}


    public Music(String title, String album, String artist){
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

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i)
    {
        parcel.writeLong(id);
        parcel.writeString(title);
        parcel.writeString(album);
        parcel.writeString(artist);
        parcel.writeInt(duration);
        parcel.writeString(path);
    }
}
