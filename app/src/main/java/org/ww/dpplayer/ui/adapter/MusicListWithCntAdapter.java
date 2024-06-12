package org.ww.dpplayer.ui.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import org.jetbrains.annotations.NotNull;
import org.ww.dpplayer.entity.Music;

import java.util.List;

public class MusicListWithCntAdapter extends MusicListAdapter
{

    private List<Integer> cntList;
    public MusicListWithCntAdapter(Context context, List<Music> musicList, List<Integer> cntList)
    {
        super(context, musicList);
        this.cntList = cntList;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MusicListAdapter.ViewHolder holder, int position)
    {
        super.onBindViewHolder(holder, position);
        holder.cntTv.setText(cntList.get(position).toString() + "次");
        holder.cntTv.setVisibility(View.VISIBLE);
    }


}
