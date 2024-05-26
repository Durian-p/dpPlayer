package org.ww.testapp.ui.main;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import org.jetbrains.annotations.NotNull;
import org.ww.testapp.ui.discover.FragDiscover;
import org.ww.testapp.ui.my.FragMy;
import org.ww.testapp.ui.rover.FragRover;

public class SectionsPagerAdaptor extends FragmentStateAdapter{
    public SectionsPagerAdaptor(FragmentActivity fa) {super(fa);};


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
