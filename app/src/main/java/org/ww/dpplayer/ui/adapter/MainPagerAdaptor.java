package org.ww.dpplayer.ui.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import org.jetbrains.annotations.NotNull;
import org.ww.dpplayer.ui.discover.FragDiscover;
import org.ww.dpplayer.ui.my.FragMy;
import org.ww.dpplayer.ui.rover.FragRover;

public class MainPagerAdaptor extends FragmentStateAdapter{
    public MainPagerAdaptor(FragmentActivity fa) {super(fa);};


    @NotNull
    @Override
    public Fragment createFragment(int position)
    {
        switch (position){
            case 0:
                return FragDiscover.newInstance();
            case 1:
                return FragRover.newInstance();
            case 2:
                return FragMy.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount(){
        return 3;
    }

}

