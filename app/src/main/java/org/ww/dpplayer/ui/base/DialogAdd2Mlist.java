package org.ww.dpplayer.ui.base;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.ww.dpplayer.R;
import org.ww.dpplayer.database.MusicRepository;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.entity.MusicList;
import org.ww.dpplayer.ui.adapter.MusicListsAdapter;
import org.ww.dpplayer.ui.my.DialogNewMlist;

import java.util.List;

public class DialogAdd2Mlist extends BottomSheetDialogFragment {

    private Context context;
    private Music music;
    private List<MusicList> musicLists;
    private MusicListsAdapter adapter;
    private DialogNewMlist dialog;
    private ActivityResultLauncher<Intent> imagePickerLauncher;


    public DialogAdd2Mlist(Context context, Music music) {
        this.music = music;
        this.context = context;;
        this.musicLists = MusicRepository.getInstance().getAllMusicLists();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add2mlist, container, false);

        ImageView ivNewMList = view.findViewById(R.id.iv_new_mlist);
        RecyclerView recyclerView = view.findViewById(R.id.rcv_songs);
        TextView tvTitle = view.findViewById(R.id.tv_title);

        tvTitle.setText(getResources().getString(R.string.add2mlist));


        // rv设置adapter
        adapter = new MusicListsAdapter(musicLists);
        adapter.setListener(new MusicListsAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(int position, long id)
            {
                if (MusicRepository.getInstance().addMusicToPlaylist(id, music.getId()))
                {
                    Toast.makeText(context, getResources().getString(R.string.add2mlist_success), Toast.LENGTH_SHORT).show();
                    dismiss();
                }
                else
                {
                    Toast.makeText(context, getResources().getString(R.string.add2mlist_fail), Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        // 新建歌单
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                dialog.handleImageResult(result.getData());
            }
        });
        ivNewMList.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog = new DialogNewMlist(requireContext(), imagePickerLauncher);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        adapter.setMusicLists(MusicRepository.getInstance().getAllMusicLists());
                        adapter.notifyDataSetChanged();
                    }
                });
                dialog.show();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }
}
