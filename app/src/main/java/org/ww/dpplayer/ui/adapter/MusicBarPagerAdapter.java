package org.ww.dpplayer.ui.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import org.ww.dpplayer.R;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.ui.player.PlayerActivity;

import java.util.List;
import java.util.Objects;

public class MusicBarPagerAdapter extends RecyclerView.Adapter<MusicBarPagerAdapter.MusicInfoViewHolder>
{

    private List<Music> musicList;

    public MusicBarPagerAdapter(List<Music> musicList)
    {
        this.musicList = musicList;
    }

    @NonNull
    @Override
    public MusicInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bottom_music, parent, false);
        return new MusicInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicInfoViewHolder holder, int position)
    {
        Music music = musicList.get(position);
        holder.bind(music);
        holder.musicInfoContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicList == null || musicList.size() == 0 || Objects.equals(musicList.get(0).getAlbum(), "[无歌曲播放中]"))
                    return;
                Intent playerIntent = new Intent(v.getContext(), PlayerActivity.class);
                playerIntent.putExtra("music", music);
                v.getContext().startActivity(playerIntent);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return musicList.size();
    }


    public void setToNext(Music music, int currentIndex)
    {
        musicList.add((currentIndex + 1 ) % musicList.size(), music);
    }

    static class MusicInfoViewHolder extends RecyclerView.ViewHolder
    {
        private ConstraintLayout musicInfoContainer;
        private ImageView albumImageView;
        private TextView titleTextView;
        private TextView artistTextView;

        public MusicInfoViewHolder(@NonNull View itemView)
        {
            super(itemView);
            albumImageView = itemView.findViewById(R.id.albumImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            artistTextView = itemView.findViewById(R.id.artistTextView);
            musicInfoContainer = itemView.findViewById(R.id.musicInfoContainer);
        }

        public void bind(Music music)
        {
            titleTextView.setText(music.getTitle());
            if (music.getArtist() != null && !music.getArtist().equals(""))
                artistTextView.setText(" - " + music.getArtist());
            else
                artistTextView.setText("");
            // 设置专辑封面
            if (music.getAlbumArt() != null)
            {
                albumImageView.setImageBitmap(music.getAlbumArt());
            } else
            {
                albumImageView.setImageResource(R.drawable.default_cover);
            }
        }
    }
}


