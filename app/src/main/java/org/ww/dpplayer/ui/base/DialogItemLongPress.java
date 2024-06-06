package org.ww.dpplayer.ui.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.ww.dpplayer.R;
import org.ww.dpplayer.database.MusicRepository;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.ui.main.MainActivity;
import org.ww.dpplayer.ui.my.local.AlbumActivity;
import org.ww.dpplayer.ui.my.local.ArtistActivity;

import java.util.ArrayList;

public class DialogItemLongPress extends BottomSheetDialogFragment {
    private Music music;


    public DialogItemLongPress(Music music) {
        this.music = music;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_longpress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvSongTitle = view.findViewById(R.id.tv_song_title);
        TextView tvSongArtist = view.findViewById(R.id.tv_song_artist);

        LinearLayout llPlayNext = view.findViewById(R.id.ll_play_next);
        LinearLayout llViewAlbum = view.findViewById(R.id.ll_view_album);
        LinearLayout llViewArtist = view.findViewById(R.id.ll_view_artist);
        LinearLayout llAddToMlist = view.findViewById(R.id.ll_add2mlist);
        LinearLayout llDelete = view.findViewById(R.id.ll_delete);
        LinearLayout llShare = view.findViewById(R.id.ll_share);

        tvSongTitle.setText(music.getTitle());
        tvSongArtist.setText(music.getArtist());
        llPlayNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO:
                Activity activity = getActivity();
                if (activity instanceof BaseMusicActivity)
                {
                    ((BaseMusicActivity)activity).setToNextPlay(music);
                }

                dismiss();
            }
        });

        llViewAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO:
                Activity activity = getActivity();
                Intent intent = new Intent(activity, AlbumActivity.class);
                ArrayList<Music> albumMusicList = MusicRepository.getInstance().getMusicsByAlbum(music.getAlbum());
                intent.putParcelableArrayListExtra("albumMusicList", albumMusicList);
                intent.putExtra("albumTitle",albumMusicList.get(0).getAlbum());
                if (activity != null)
                    activity.startActivity(intent);
                dismiss();
            }
        });

        llViewArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO:
                Activity activity = getActivity();
                Intent intent = new Intent(activity, ArtistActivity.class);;
                ArrayList<Music> artistMusicList = MusicRepository.getInstance().getMusicsByArtist(music.getArtist());
                intent.putParcelableArrayListExtra("artistMusicList", artistMusicList);
                intent.putExtra("artistName",artistMusicList.get(0).getArtist());
                if (activity != null)
                    activity.startActivity(intent);
                dismiss();
            }
        });

        llAddToMlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO:
                DialogAdd2Mlist dialog = new DialogAdd2Mlist(getActivity(), music);
                dialog.show(getParentFragmentManager(), "add2mlist");
                dismiss();
            }
        });

        llDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO:
                dismiss();
            }
        });

        llShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO:
                dismiss();
            }
        });
    }

}

