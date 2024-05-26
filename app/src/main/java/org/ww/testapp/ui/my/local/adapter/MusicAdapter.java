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
import org.jetbrains.annotations.NotNull;
import org.ww.testapp.R;
import org.ww.testapp.entity.Music;
import org.ww.testapp.util.ChineseToPinyin;

import java.util.ArrayList;
import java.util.List;


public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> implements SectionIndexer
{

    private Context context;
    private List<Music> musicList; // 歌曲列表数据

    public MusicAdapter(Context context, List<Music> musicList)
    {
        this.context = context;
        this.musicList = musicList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_local_music, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position)
    {
        Music music = musicList.get(position);
        // 在这里可以获取歌曲的标题、艺术家和专辑信息，并设置到对应的 TextView 中

        if (music.getTitle() != null)
            holder.musicNameTextView.setText(music.getTitle());
        if (music.getArtist() != null)
            holder.albumArtistTextView.setText(music.getArtist() + " - " + music.getAlbum());

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
    public int getItemCount()
    {
        return musicList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Music> newData) {
        musicList.clear();
        musicList.addAll(newData);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView musicNameTextView;
        // 添加艺术家和专辑信息的 TextView
        TextView albumArtistTextView;
        // 添加专辑封面的 ImageView
        ImageView coverImageView;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            musicNameTextView = itemView.findViewById(R.id.tvTitle);
            // 初始化艺术家和专辑信息的 TextView
            albumArtistTextView = itemView.findViewById(R.id.tvAlbumArtist);
            // 初始化专辑封面的 ImageView
            coverImageView = itemView.findViewById(R.id.ivCover);
        }
    }

    @Override
    public Object[] getSections()
    {
        // 在这里返回所有的分组标签（例如A、B、C...）
        // 这里假设每个歌曲标题的首字母都可以作为分组标签
        List<String> sections = new ArrayList<>();
        for (Music music : musicList)
        {
            String title = music.getTitle();
            if (title != null && title.length() > 0)
            {
                char firstChar = title.charAt(0);
                if (Character.isLetter(firstChar))
                {
                    sections.add(String.valueOf(Character.toUpperCase(firstChar)));
                }
                else if (isChinese(firstChar))
                {
                    String pinyinFirstChar = ChineseToPinyin.convert(String.valueOf(firstChar));
                    if (pinyinFirstChar != null && ! pinyinFirstChar.isEmpty())
                    {
                        sections.add(String.valueOf(Character.toUpperCase(pinyinFirstChar.charAt(0))));
                    }
                }
                else
                {
                    // 其他情况视为特殊字符
                    sections.add("#");
                }
            }
        }
        return sections.toArray(new String[0]);
    }

    @Override
    public int getPositionForSection(int sectionIndex)
    {
        // 根据分组标签返回该分组的第一个位置
        if ( sectionIndex == '#')
            sectionIndex = 26;
        else
            sectionIndex -= 'A';
        String section = (String) getSections()[sectionIndex];
        for (int i = 0; i < getItemCount(); i++)
        {
            String title = musicList.get(i).getTitle();
            if (title != null && title.length() > 0)
            {
                char firstChar = title.charAt(0);
                if (Character.isLetter(firstChar))
                {
                    if (Character.toUpperCase(firstChar) == section.charAt(0))
                    {
                        return i;
                    }
                }
                else if (isChinese(firstChar))
                {
                    String pinyinFirstChar = ChineseToPinyin.convert(String.valueOf(firstChar));
                    if (pinyinFirstChar != null && ! pinyinFirstChar.isEmpty())
                    {
                        if (Character.toUpperCase(pinyinFirstChar.charAt(0)) == section.charAt(0))
                        {
                            return i;
                        }
                    }
                }
                else
                {
                    // 特殊字符以#分组，直接返回第一个位置
                    return 0;
                }
            }
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position)
    {
        // 根据位置返回对应的分组标签的索引
        String title = musicList.get(position).getTitle();
        if (title != null && title.length() > 0)
        {
            char firstChar = title.charAt(0);
            if (Character.isLetter(firstChar))
            {
                // 如果是英文字母，则直接转换为大写
                return Character.toUpperCase(firstChar) - 'A';
            }
            else if (isChinese(firstChar))
            {
                // 如果是中文字符，则调用工具类获取拼音首字母，并转换为大写
                String pinyinFirstChar = ChineseToPinyin.convert(String.valueOf(firstChar));
                if (pinyinFirstChar != null && ! pinyinFirstChar.isEmpty())
                {
                    return Character.toUpperCase(pinyinFirstChar.charAt(0)) - 'A';
                }
            }
        }
        // 其他情况视为特殊字符，返回#对应的索引
        return 26; // '#' 的索引
    }

    // 判断字符是否是中文字符
    private boolean isChinese(char c)
    {
        // 中文字符的Unicode范围为0x4E00~0x9FA5
        return c >= 0x4E00 && c <= 0x9FA5;
    }


}
