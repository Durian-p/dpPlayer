package org.ww.testapp.ui.base;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.os.IBinder;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.ww.testapp.R;
import org.ww.testapp.entity.Music;
import org.ww.testapp.player.MusicServiceController;

import java.util.List;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.MusicViewHolder>
{

    private List<Music> musicList;
    private long currentId;
    public int index;
    boolean bound = false;
    private MusicServiceController musicServiceController;

    public PlayListAdapter(List<Music> musicList, long currentId)
    {
        this.musicList = musicList;
        this.currentId = currentId;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist_music, parent, false);
        musicServiceController = new MusicServiceController(parent.getContext());
        musicServiceController.bindService(new MusicServiceController.MusicServiceCallback()
        {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service)
            {
                PlayListAdapter.this.bound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name)
            {

            }
        });
        return new MusicViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, @SuppressLint("RecyclerView") int position)
    {
        // 显示信息
        Music music = musicList.get(position);
        String title = music.getTitle();
        String artist = " - " + music.getArtist();
        SpannableString spannableString = new SpannableString(title + artist);
        spannableString.setSpan(new TextAppearanceSpan(holder.tvMusicInfo.getContext(), R.style.SongTitleTextViewStyle), 0, title.length(), 0);
        spannableString.setSpan(new TextAppearanceSpan(holder.tvMusicInfo.getContext(), R.style.ArtistTextViewStyle), title.length(), spannableString.length(), 0);
        if (music.getId() == this.currentId)
        {
            spannableString.setSpan(new TextAppearanceSpan(holder.tvMusicInfo.getContext(), R.style.SelectedTextViewColorStyle), 0, spannableString.length(), 0);
            index = position;
        }
        holder.tvMusicInfo.setText(spannableString);

        // 绑定点击事件
        holder.llPlayListItem.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int index = holder.getBindingAdapterPosition();
                // 切换歌曲
                if (bound)
                    musicServiceController.setCurrentMusicId(musicList.get(index).getId());
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return musicList.size();
    }

    public void updatePlayingId(long id)
    {
        this.currentId = id;
    }

    public void updatePlayingList(List<Music> musicList)
    {
        this.musicList = musicList;
    }

    static class MusicViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvMusicInfo;
        LinearLayout llPlayListItem;


        MusicViewHolder(View itemView)
        {
            super(itemView);
            tvMusicInfo = itemView.findViewById(R.id.tvMusicInfo);
            llPlayListItem = itemView.findViewById(R.id.llPlayListItem);
        }
    }
}

