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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> implements SectionIndexer {

    private Context context;
    private List<List<Music>> artistList;
    private String[] sections;
    private int[] sectionPositions;

    public ArtistAdapter(Context context, List<Music> musicList) {
        this.context = context;
        this.artistList = extractArtists(musicList);

        // Sort the artist list
        artistList.sort((artist1, artist2) -> SortUtil.compare(artist1.get(0).getArtist(), artist2.get(0).getArtist()));

        // Initialize section indexer
        initSectionIndexer();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_artist, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        List<Music> artist = artistList.get(position);
        Music music = artist.get(0);
        holder.artistNameTextView.setText(music.getArtist());
        holder.cntTextView.setText(artist.size() + "首");

        // 设置艺术家封面
        Bitmap artistArt = music.getAlbumArt();
        if (artistArt != null) {
            holder.coverImageView.setImageBitmap(artistArt);
        } else {
            // 如果没有艺术家封面，则使用默认图标
            holder.coverImageView.setImageResource(R.drawable.default_cover);
        }
    }

    @Override
    public int getItemCount() {
        return artistList.size();
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
        artistList.clear();
        artistList.addAll(extractArtists(newData));

        // Sort the updated artist list
        Collections.sort(artistList, (artist1, artist2) -> SortUtil.compare(artist1.get(0).getArtist(), artist2.get(0).getArtist()));

        // Reinitialize section indexer
        initSectionIndexer();

        notifyDataSetChanged();
    }

    private void initSectionIndexer() {
        List<String> sectionList = new ArrayList<>();
        List<Integer> positionList = new ArrayList<>();

        String lastSection = null;
        for (int i = 0; i < artistList.size(); i++) {
            String artistName = artistList.get(i).get(0).getArtist();
            if (artistName != null && artistName.length() > 0) {
                String currentSection = getFirstLetter(artistName);
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

    private List<List<Music>> extractArtists(List<Music> musicList) {
        Map<String, List<Music>> artistMap = new HashMap<>();
        for (Music music : musicList) {
            String artist = music.getArtist();
            if (!artistMap.containsKey(artist)) {
                artistMap.put(artist, new ArrayList<>());
            }
            artistMap.get(artist).add(music);
        }
        return new ArrayList<>(artistMap.values());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView artistNameTextView;
        ImageView coverImageView;

        TextView cntTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            artistNameTextView = itemView.findViewById(R.id.tvArtist);
            coverImageView = itemView.findViewById(R.id.ivArtistImg);
            cntTextView = itemView.findViewById(R.id.tvCnt);
        }
    }
}
