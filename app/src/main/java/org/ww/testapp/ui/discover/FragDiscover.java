package org.ww.testapp.ui.discover;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import org.ww.testapp.databinding.FragFirstBinding;
import org.ww.testapp.entity.Music;
import org.ww.testapp.util.MusicLoader;

import java.util.List;

public class FragDiscover extends Fragment
{

    private FragFirstBinding binding;


    public static Fragment newInstance()
    {
        return new FragDiscover();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    )
    {

        binding = FragFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 在 UI 线程更新文本
                List<Music> list = MusicLoader.findLocalMusic(requireContext());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.textviewFirst.setText(list.size());
                    }
                });
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