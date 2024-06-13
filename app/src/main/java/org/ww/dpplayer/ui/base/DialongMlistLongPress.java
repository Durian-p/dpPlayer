package org.ww.dpplayer.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.ww.dpplayer.R;
import org.ww.dpplayer.entity.MusicList;

public class DialongMlistLongPress extends BottomSheetDialogFragment
{
    private MusicList musicList;
    private OnItemClickListener listener;
    public interface OnItemClickListener
    {
        void onItemDelete(MusicList musicList);
        void onItemEdit(MusicList musicList);
    }


    public DialongMlistLongPress(MusicList musicList) {
        this.musicList = musicList;
    }

    public void setOnItemDeleteListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_longpressing_mlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tv_song_title);
        ImageView ivCover = view.findViewById(R.id.iv_mlist_cover);

        LinearLayout llEdit= view.findViewById(R.id.ll_edit);
        LinearLayout llDelete = view.findViewById(R.id.ll_delete);


        tvTitle.setText(musicList.getName());
        if (musicList.getCover() != null)
        {
            ivCover.setImageBitmap(musicList.getCover());
        }
        llEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onItemEdit(musicList);
                dismiss();
            }
        });


        llDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO:
                if (listener != null)
                    listener.onItemDelete(musicList);
                dismiss();
            }
        });

    }

}

