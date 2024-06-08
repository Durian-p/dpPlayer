package org.ww.dpplayer.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.makeramen.roundedimageview.RoundedImageView;
import org.ww.dpplayer.R;
import org.ww.dpplayer.entity.Music;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeAlbumAdapter extends RecyclerView.Adapter<HomeAlbumAdapter.AlbumViewHolder> {

    private final List<Music> albums;

    public HomeAlbumAdapter(List<Music> albums) {
        this.albums = getUniqueAlbums(albums);
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Music album = albums.get(position);
        holder.albumTitle.setText(album.getTitle());
        if (album.getAlbumArt() != null) {
            holder.albumArt.setImageBitmap(album.getAlbumArt());
        } else {
            holder.albumArt.setImageResource(R.drawable.default_cover);
        }
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        TextView albumTitle;
        RoundedImageView albumArt;

        AlbumViewHolder(View itemView) {
            super(itemView);
            albumTitle = itemView.findViewById(R.id.album_title);
            albumArt = itemView.findViewById(R.id.album_art);
        }
    }

    private List<Music> getUniqueAlbums(List<Music> albums) {
        Set<String> seenAlbums = new HashSet<>();
        List<Music> uniqueAlbums = new ArrayList<>();
        for (Music music : albums) {
            if (seenAlbums.add(music.getArtist())) {
                uniqueAlbums.add(music);
            }
        }
        return uniqueAlbums;
    }
}

