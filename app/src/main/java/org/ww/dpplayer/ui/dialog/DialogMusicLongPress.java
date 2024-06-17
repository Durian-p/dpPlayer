package org.ww.dpplayer.ui.dialog;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.ww.dpplayer.R;
import org.ww.dpplayer.database.MusicRepository;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.ui.base.BaseMusicActivity;
import org.ww.dpplayer.ui.my.local.AlbumActivity;
import org.ww.dpplayer.ui.my.local.ArtistActivity;
import org.ww.dpplayer.util.ShareUtil;

import java.io.File;
import java.util.ArrayList;

public class DialogMusicLongPress extends BottomSheetDialogFragment {
    private Music music;
    private OnItemDeleteListener deleteListener;
    public interface OnItemDeleteListener {
        void onItemDelete(Music music);
    }


    public DialogMusicLongPress(Music music) {
        this.music = music;
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.deleteListener = listener;
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
                if (deleteListener != null)
                    deleteListener.onItemDelete(music);
                dismiss();
            }
        });

        llShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    File file = new File(music.getPath());
                    Uri path;
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    {
                        path = FileProvider.getUriForFile(requireContext(), ShareUtil.getAuthority(), file);
                    } else
                    {
                        path = Uri.fromFile(file);
                    }
                    //注意intent用addFlags 如果intent在这行代码下使用setFlags会导致其他应用没有权限打开你的文件
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    String type = ShareUtil.getMimeType(file);
                    intent.setDataAndType(path, type);
                    //如果想让用户每次打开文件都自己选择（方便切换应用打开）加上下面这句代码
                    intent = Intent.createChooser(intent, "请选择打开此文件的应用");
                    startActivity(intent);
                    dismiss();

                }
                catch (ActivityNotFoundException e)
                {
                    Toast.makeText(requireContext(), "此设备没有可以打开此文件的软件", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

}

