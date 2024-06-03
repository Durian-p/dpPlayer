package org.ww.dpplayer.ui.rover;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import org.ww.dpplayer.R;
import org.ww.dpplayer.databinding.FragSecondBinding;

public class FragRover extends Fragment
{

    private FragSecondBinding binding;

    public static Fragment newInstance()
    {
        return new FragRover();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    )
    {

        binding = FragSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSecond.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                NavHostFragment.findNavController(FragRover.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }

}