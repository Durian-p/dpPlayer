package org.ww.testapp.ui.my.local.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.ww.testapp.ui.my.local.FragAlbum;
import org.ww.testapp.ui.my.local.FragArtist;
import org.ww.testapp.ui.my.local.FragLocalMusic;

public class LocalPagerAdapter extends FragmentStateAdapter {

    public LocalPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // 根据位置返回对应的 Fragment
        switch (position) {
            case 0:
                return new FragLocalMusic();
            case 1:
                return new FragAlbum();
            case 2:
                return new FragArtist();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        // 返回页面数量
        return 3;
    }
}

