package org.ww.dpplayer.ui.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.makeramen.roundedimageview.RoundedImageView;
import org.ww.dpplayer.R;
import org.ww.dpplayer.database.MusicRepository;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.ui.my.local.AlbumActivity;
import org.ww.dpplayer.util.AnimUtil;

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
        holder.container.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (album.getAlbum() != null) {
                    Intent intent = new Intent(v.getContext(), AlbumActivity.class);
                    intent.putExtra("albumTitle", album.getAlbum());
                    intent.putExtra("albumMusicList", MusicRepository.getInstance().getMusicsByAlbum(album.getAlbum()));
                    v.getContext().startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        TextView albumTitle;
        RoundedImageView albumArt;
        LinearLayout container;

        AlbumViewHolder(View itemView) {
            super(itemView);
            albumTitle = itemView.findViewById(R.id.album_title);
            albumArt = itemView.findViewById(R.id.album_art);
            container = itemView.findViewById(R.id.home_album_container);

            container.setOnTouchListener(AnimUtil.getTouchAnimListener(1.07f));
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

