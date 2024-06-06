// MusicListAdapter.java
package org.ww.dpplayer.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import org.ww.dpplayer.R;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.ui.base.DialogItemLongPress;
import org.ww.dpplayer.util.ChineseToPinyin;

import java.util.ArrayList;
import java.util.List;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder> implements SectionIndexer{

    private Context context;
    private List<Music> musicList; // 歌曲列表数据
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public MusicListAdapter(Context context, List<Music> musicList) {
        this.context = context;
        this.musicList = musicList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) { this.onItemClickListener = listener; }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) { this.onItemLongClickListener = listener; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_local_music, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position)
    {
        Music music = musicList.get(position);
        if (music.getTitle() != null)
            holder.musicNameTextView.setText(music.getTitle());
        if (music.getArtist() != null)
            holder.albumArtistTextView.setText(music.getArtist() + " - " + music.getAlbum());

        // 设置专辑封面
        Bitmap albumArt = music.getAlbumArt();
        if (albumArt != null) {
            holder.coverImageView.setImageBitmap(albumArt);
        } else {
            // 如果没有专辑封面，则使用默认图标
            holder.coverImageView.setImageResource(R.drawable.default_cover);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getBindingAdapterPosition();
                if (onItemClickListener != null && position != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(position);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getBindingAdapterPosition();
                if (onItemLongClickListener != null && position != RecyclerView.NO_POSITION) {
                    onItemLongClickListener.onItemLongClick(position);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Music> newData) {
        musicList.clear();
        musicList.addAll(newData);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView musicNameTextView;
        TextView albumArtistTextView;
        ImageView coverImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            musicNameTextView = itemView.findViewById(R.id.tvTitle);
            albumArtistTextView = itemView.findViewById(R.id.tvAlbumArtist);
            coverImageView = itemView.findViewById(R.id.ivCover);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    @Override
    public Object[] getSections() {
        List<String> sections = new ArrayList<>();
        for (Music music : musicList) {
            String title = music.getTitle();
            if (title != null && title.length() > 0) {
                char firstChar = title.charAt(0);
                if (Character.isLetter(firstChar)) {
                    sections.add(String.valueOf(Character.toUpperCase(firstChar)));
                } else if (isChinese(firstChar)) {
                    String pinyinFirstChar = ChineseToPinyin.convert(String.valueOf(firstChar));
                    if (pinyinFirstChar != null && !pinyinFirstChar.isEmpty()) {
                        sections.add(String.valueOf(Character.toUpperCase(pinyinFirstChar.charAt(0))));
                    }
                } else {
                    sections.add("#");
                }
            }
        }
        return sections.toArray(new String[0]);
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (sectionIndex == '#') sectionIndex = 26;
        else sectionIndex -= 'A';
        String section = (String) getSections()[sectionIndex];
        for (int i = 0; i < getItemCount(); i++) {
            String title = musicList.get(i).getTitle();
            if (title != null && title.length() > 0) {
                char firstChar = title.charAt(0);
                if (Character.isLetter(firstChar)) {
                    if (Character.toUpperCase(firstChar) == section.charAt(0)) {
                        return i;
                    }
                } else if (isChinese(firstChar)) {
                    String pinyinFirstChar = ChineseToPinyin.convert(String.valueOf(firstChar));
                    if (pinyinFirstChar != null && !pinyinFirstChar.isEmpty()) {
                        if (Character.toUpperCase(pinyinFirstChar.charAt(0)) == section.charAt(0)) {
                            return i;
                        }
                    }
                } else {
                    return 0;
                }
            }
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        String title = musicList.get(position).getTitle();
        if (title != null && title.length() > 0) {
            char firstChar = title.charAt(0);
            if (Character.isLetter(firstChar)) {
                return Character.toUpperCase(firstChar) - 'A';
            } else if (isChinese(firstChar)) {
                String pinyinFirstChar = ChineseToPinyin.convert(String.valueOf(firstChar));
                if (pinyinFirstChar != null && !pinyinFirstChar.isEmpty()) {
                    return Character.toUpperCase(pinyinFirstChar.charAt(0)) - 'A';
                }
            }
        }
        return 26; // '#' 的索引
    }

    private boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }
}
