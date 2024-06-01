package org.ww.testapp.ui.player;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayerPagerAdapter extends FragmentStateAdapter
{
    private final List<Fragment> fragments;

    public PlayerPagerAdapter(FragmentActivity fm, @NonNull @NotNull List<Fragment> fragments)
    {
        super(fm);
        this.fragments = fragments;
    }
    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position)
    {
        return fragments.get(position);
    }

    @Override
    public int getItemCount()
    {
        return fragments.size();
    }
}
