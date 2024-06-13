package org.ww.dpplayer.ui.base;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import org.ww.dpplayer.R;
import org.ww.dpplayer.database.MusicRepository;
import org.ww.dpplayer.entity.MusicList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DialogEditPlaylist extends Dialog
{

    private EditText editTextPlaylistName;
    private ImageView imageViewPlaylistCover;
    private Button buttonSavePlaylist;
    private Bitmap coverBitmap;
    private MusicRepository musicRepository;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private MusicList musicList;

    public DialogEditPlaylist(@NonNull Context context, ActivityResultLauncher<Intent> imagePickerLauncher, MusicList musicList) {
        super(context);
        this.imagePickerLauncher = imagePickerLauncher;
        musicRepository = MusicRepository.getInstance();
        this.musicList = musicList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_new_musiclist);

        editTextPlaylistName = findViewById(R.id.editTextPlaylistName);
        imageViewPlaylistCover = findViewById(R.id.imageViewPlaylistCover);
        buttonSavePlaylist = findViewById(R.id.buttonSavePlaylist);

        buttonSavePlaylist.setText("保存");
        if (musicList != null)
            imageViewPlaylistCover.setImageBitmap(musicList.getCover());
        else
            imageViewPlaylistCover.setImageResource(R.drawable.default_cover);
        editTextPlaylistName.setText(musicList.getName());

        imageViewPlaylistCover.setOnClickListener(v -> chooseImage());
        buttonSavePlaylist.setOnClickListener(v -> updatePlaylist());
        // 将cardview的宽度设置为屏幕宽度
        int width = getContext().getResources().getDisplayMetrics().widthPixels;
        CardView cardViewPlaylist = findViewById(R.id.cardViewPlaylist);
        cardViewPlaylist.getLayoutParams().width = (int) (0.9*width);
        ConstraintLayout consLayoutPlaylist = findViewById(R.id.consLayoutPlaylist);
        consLayoutPlaylist.getLayoutParams().width = (int) (0.9 * width);

    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void updatePlaylist() {
        String name = editTextPlaylistName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "歌单名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }


        musicList.setName(name);

        if (coverBitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            coverBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            musicList.setCoverFromByteArray(stream.toByteArray());
        }

        musicRepository.updateMusicList(musicList);
        Toast.makeText(getContext(), "成功修改歌单信息", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    public void handleImageResult(Intent data) {
        if (data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                coverBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                imageViewPlaylistCover.setImageBitmap(coverBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
