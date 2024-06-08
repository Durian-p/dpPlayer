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

public class HomeArtistAdapter extends RecyclerView.Adapter<HomeArtistAdapter.ArtistViewHolder> {

    private final List<Music> artists;

    public HomeArtistAdapter(List<Music> artists) {
        this.artists = getUniqueArtists(artists);
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_artist, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        Music artist = artists.get(position);
        holder.artistName.setText(artist.getArtist());
        if (artist.getAlbumArt() != null) {
            holder.artistImage.setImageBitmap(artist.getAlbumArt());
        } else {
            holder.artistImage.setImageResource(R.drawable.default_artist_avatar);
        }
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    static class ArtistViewHolder extends RecyclerView.ViewHolder {
        TextView artistName;
        RoundedImageView artistImage;

        ArtistViewHolder(View itemView) {
            super(itemView);
            artistName = itemView.findViewById(R.id.artist_name);
            artistImage = itemView.findViewById(R.id.artist_image);
        }
    }

    private List<Music> getUniqueArtists(List<Music> artists) {
        Set<String> seenArtists = new HashSet<>();
        List<Music> uniqueArtists = new ArrayList<>();
        for (Music music : artists) {
            if (seenArtists.add(music.getArtist())) {
                uniqueArtists.add(music);
            }
        }
        return uniqueArtists;
    }
}
