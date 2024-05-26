package org.ww.testapp.ui.my.local.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.ww.testapp.R;
import org.ww.testapp.entity.Music;
import org.ww.testapp.util.ChineseToPinyin;
import org.ww.testapp.util.SortUtil;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> implements SectionIndexer {

    private Context context;
    private List<List<Music>> albumList;
    private String[] sections;
    private int[] sectionPositions;

    public AlbumAdapter(Context context, List<Music> musicList) {
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
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (sectionIndex >= sections.length) {
            return getItemCount() - 1;
        }
        return sectionPositions[sectionIndex];
    }

    @Override
    public int getSectionForPosition(int position) {
        for (int i = sections.length - 1; i >= 0; i--) {
            if (position >= sectionPositions[i]) {
                return i;
            }
        }
        return 0;
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            albumNameTextView = itemView.findViewById(R.id.tvAlbumName);
            coverImageView = itemView.findViewById(R.id.ivAlbumCover);
            artistNameTextView = itemView.findViewById(R.id.tvArtist);
        }
    }
}
