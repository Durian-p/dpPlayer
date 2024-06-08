package org.ww.dpplayer.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import org.ww.dpplayer.R;
import org.ww.dpplayer.entity.Music;
import java.util.List;
import java.util.Map;

public class HomeSectionAdapter extends RecyclerView.Adapter<HomeSectionAdapter.SectionViewHolder> {

    private final Map<String, List<Music>> sectionData;

    public HomeSectionAdapter(Map<String, List<Music>> sectionData) {
        this.sectionData = sectionData;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.section_recycler_view, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        String sectionTitle = (String) sectionData.keySet().toArray()[position];
        List<Music> items = sectionData.get(sectionTitle);


        holder.recyclerView.setLayoutManager(new LinearLayoutManager(holder.recyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false));

        if (sectionTitle.equals("Artists")) {
            holder.title.setText("喜爱艺术家");
            holder.recyclerView.setAdapter(new HomeArtistAdapter(items));
        } else if (sectionTitle.equals("Albums")) {
            holder.title.setText("爱听专辑");
            holder.recyclerView.setAdapter(new HomeAlbumAdapter(items));
        }
    }

    @Override
    public int getItemCount() {
        return sectionData.size();
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        RecyclerView recyclerView;

        SectionViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            recyclerView = itemView.findViewById(R.id.recyclerView);
        }
    }
}
