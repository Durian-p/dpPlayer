package org.ww.dpplayer.ui.adapter;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.ww.dpplayer.R;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.util.AnimUtil;
import org.ww.dpplayer.util.ChineseToPinyin;
import org.ww.dpplayer.util.SortUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class  AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.ViewHolder> implements SectionIndexer {

    private Context context;
    private List<List<Music>> albumList;
    private String[] sections;
    private int[] sectionPositions;

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public List<List<Music>> getAlbumList() {
        return albumList;
    }

    public AlbumListAdapter(Context context, List<Music> musicList) {
        this.context = context;
        this.albumList = extractAlbums(musicList);

        // Sort the album list
        albumList.sort((album1, album2) -> SortUtil.compare(album1.get(0).getAlbum(), album2.get(0).getAlbum()));

        // Initialize section indexer
        initSectionIndexer();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        List<Music> album = albumList.get(position);
        Music music = album.get(0);
        holder.albumNameTextView.setText(music.getAlbum());
        holder.artistNameTextView.setText(music.getArtist());

        // 设置专辑封面
        Bitmap albumArt = music.getAlbumArt();
        if (albumArt != null) {
            holder.coverImageView.setImageBitmap(albumArt);
        } else {
            // 如果没有专辑封面，则使用默认图标
            holder.coverImageView.setImageResource(R.drawable.default_cover);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                int position = holder.getBindingAdapterPosition();
                if (onItemClickListener != null && position != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public Object[] getSections() {
        List<String> sections = new ArrayList<>();
        for (char i = 'A';i < 'Z';i++)
        {
            sections.add(String.valueOf(i));
        }
        sections.add("#");
        return sections.toArray(new String[0]);
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (sectionIndex == '#') sectionIndex = 26;
        else sectionIndex -= 'A';
        String section = (String) getSections()[sectionIndex];

        // 用于保存上一个有效位置
        int lastValidPosition = 0;

        for (int i = 0; i < getItemCount(); i++) {
            String title = albumList.get(i).get(0).getTitle();
            if (title != null && title.length() > 0) {
                char firstChar = title.charAt(0);
                if (Character.isLetter(firstChar)) {
                    if (Character.toUpperCase(firstChar) == section.charAt(0)) {
                        return i;
                    } else if (Character.toUpperCase(firstChar) < section.charAt(0)) {
                        lastValidPosition = i;
                    }
                } else if (isChinese(firstChar)) {
                    String pinyinFirstChar = ChineseToPinyin.convert(String.valueOf(firstChar));
                    if (pinyinFirstChar != null && !pinyinFirstChar.isEmpty()) {
                        if (Character.toUpperCase(pinyinFirstChar.charAt(0)) == section.charAt(0)) {
                            return i;
                        } else if (Character.toUpperCase(pinyinFirstChar.charAt(0)) < section.charAt(0)) {
                            lastValidPosition = i;
                        }
                    }
                } else {
                    if ('#' == section.charAt(0)) {
                        lastValidPosition = i;
                    }
                }
            }
        }
        // 如果没有找到完全匹配的项，返回最后一个有效位置
        return lastValidPosition;
    }


    @Override
    public int getSectionForPosition(int position) {
        String title = albumList.get(position).get(0).getTitle();
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

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Music> newData) {
        albumList.clear();
        albumList.addAll(extractAlbums(newData));

        // Sort the updated album list
        Collections.sort(albumList, (album1, album2) -> SortUtil.compare(album1.get(0).getAlbum(), album2.get(0).getAlbum()));

        // Reinitialize section indexer
        initSectionIndexer();

        notifyDataSetChanged();
    }

    private void initSectionIndexer() {
        List<String> sectionList = new ArrayList<>();
        List<Integer> positionList = new ArrayList<>();

        String lastSection = null;
        for (int i = 0; i < albumList.size(); i++) {
            String albumName = albumList.get(i).get(0).getAlbum();
            if (albumName != null && albumName.length() > 0) {
                String currentSection = getFirstLetter(albumName);
                if (!currentSection.equals(lastSection)) {
                    lastSection = currentSection;
                    sectionList.add(currentSection);
                    positionList.add(i);
                }
            }
        }

        sections = sectionList.toArray(new String[0]);
        sectionPositions = new int[positionList.size()];
        for (int i = 0; i < positionList.size(); i++) {
            sectionPositions[i] = positionList.get(i);
        }
    }

    private String getFirstLetter(String str) {
        char firstChar = str.charAt(0);
        if (Character.isLetter(firstChar)) {
            return String.valueOf(Character.toUpperCase(firstChar));
        } else if (isChinese(firstChar)) {
            String pinyin = ChineseToPinyin.convert(String.valueOf(firstChar));
            if (pinyin != null && !pinyin.isEmpty()) {
                return String.valueOf(Character.toUpperCase(pinyin.charAt(0)));
            }
        }
        return "#";
    }

    private boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }

    private List<List<Music>> extractAlbums(List<Music> musicList) {
        Map<String, List<Music>> albumMap = new HashMap<>();
        for (Music music : musicList) {
            String album = music.getAlbum();
            if (!albumMap.containsKey(album)) {
                albumMap.put(album, new ArrayList<>());
            }
            albumMap.get(album).add(music);
        }
        return new ArrayList<>(albumMap.values());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView albumNameTextView;

        TextView artistNameTextView;
        ImageView coverImageView;
        View container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            albumNameTextView = itemView.findViewById(R.id.tvAlbumName);
            coverImageView = itemView.findViewById(R.id.ivAlbumCover);
            artistNameTextView = itemView.findViewById(R.id.tvArtist);
            container = itemView.findViewById(R.id.album_container);

            container.setOnTouchListener(AnimUtil.getTouchAnimListener());
        }
    }
}
