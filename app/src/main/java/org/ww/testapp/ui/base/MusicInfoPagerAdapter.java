package org.ww.testapp.ui.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.ww.testapp.R;
import org.ww.testapp.entity.Music;

import java.util.List;

public class MusicInfoPagerAdapter extends RecyclerView.Adapter<MusicInfoPagerAdapter.MusicInfoViewHolder>
{

    private List<Music> musicList;

    public MusicInfoPagerAdapter(List<Music> musicList)
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
    }

    @Override
    public int getItemCount()
    {
        return musicList.size();
    }

    static class MusicInfoViewHolder extends RecyclerView.ViewHolder
    {

        private ImageView albumImageView;
        private TextView titleTextView;
        private TextView artistTextView;

        public MusicInfoViewHolder(@NonNull View itemView)
        {
            super(itemView);
            albumImageView = itemView.findViewById(R.id.albumImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            artistTextView = itemView.findViewById(R.id.artistTextView);
        }

        public void bind(Music music)
        {
            titleTextView.setText(music.getTitle());
            artistTextView.setText(music.getArtist());
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


