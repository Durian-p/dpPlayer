package org.ww.dpplayer.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.ww.dpplayer.R;
import org.ww.dpplayer.entity.MusicList;
import org.ww.dpplayer.util.AnimUtil;

import java.util.List;

public class MusicListsAdapter extends RecyclerView.Adapter<MusicListsAdapter.ViewHolder> {
    private List<MusicList> musicListList;
    private OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(int position, long id);
        void onLongClick(int position, long id);
    }

    public MusicListsAdapter(List<MusicList> musicListList) {
        this.musicListList = musicListList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_musiclist, parent, false);
        return new ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MusicList musicList = musicListList.get(position);
        holder.id = musicList.getId();
        holder.tvTitle.setText(musicList.getName());
        holder.tvExtra.setText(" " + musicList.getMusicIdList().size() + "首");
        Bitmap cover  = musicList.getCover();
        if (cover == null)
            holder.ivCover.setImageResource(R.drawable.default_cover);
        else
            holder.ivCover.setImageBitmap(cover);

        holder.llMlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onItemClick(holder.getBindingAdapterPosition(), holder.id);
            }
        });

        holder.llMlist.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                if (listener != null)
                    listener.onLongClick(holder.getBindingAdapterPosition(), holder.id);
                return true;
            }
        });

        holder.ivMore.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (listener != null)
                    listener.onLongClick(holder.getBindingAdapterPosition(), holder.id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicListList != null ? musicListList.size() : 0;
    }

    public void setMusicLists(List<MusicList> musicListList) {
        this.musicListList = musicListList;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivCover;
        ImageView ivMore;
        TextView tvTitle;
        TextView tvExtra;
        LinearLayout llMlist;
        View container;
        long id;

        public ViewHolder(@NonNull View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvMlistTitle);
            tvExtra = view.findViewById(R.id.tvMlistExtra);
            ivCover = view.findViewById(R.id.ivMlistCover);
            llMlist = view.findViewById(R.id.llMlist);
            ivMore = view.findViewById(R.id.ivMlistMore);
            container = view.findViewById(R.id.mlContainer);

            container.setOnTouchListener(AnimUtil.getTouchAnimListener());
        }
    }
}
