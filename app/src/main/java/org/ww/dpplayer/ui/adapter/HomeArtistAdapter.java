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
import org.ww.dpplayer.ui.my.local.ArtistActivity;
import org.ww.dpplayer.util.AnimUtil;

import java.util.*;

public class HomeArtistAdapter extends RecyclerView.Adapter<HomeArtistAdapter.ArtistViewHolder> {

    private final List<Music> artists;

    public HomeArtistAdapter(List<Music> artists) {
        this.artists = getUniqueArtists(artists);
        Collections.shuffle(this.artists);
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
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ArtistActivity.class);
                intent.putExtra("artistName", artist.getArtist());
                intent.putExtra("artistMusicList", MusicRepository.getInstance().getMusicsByArtist(artist.getArtist()));
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    static class ArtistViewHolder extends RecyclerView.ViewHolder {
        TextView artistName;
        RoundedImageView artistImage;
        LinearLayout container;

        ArtistViewHolder(View itemView) {
            super(itemView);
            artistName = itemView.findViewById(R.id.artist_name);
            artistImage = itemView.findViewById(R.id.artist_image);
            container = itemView.findViewById(R.id.home_artist_container);

            container.setOnTouchListener(AnimUtil.getTouchAnimListener(1.12f));
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
