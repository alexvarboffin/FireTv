package tv.hdonlinetv.besttvchannels.movies.watchfree.tutorial;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentTutorialBinding;


public class TutorialFragment extends Fragment {

    private FragmentTutorialBinding binding;

    public TutorialFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTutorialBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        binding.onSearchButton.setOnClickListener(v->{
            String url = "https://www.google.com/search?q=Free+Popular+IPTV+Playlist";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}