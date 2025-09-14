package tv.hdonlinetv.besttvchannels.movies.watchfree.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentLoadingDialogBinding;

public class LoadingDialogFragment extends DialogFragment {

    private FragmentLoadingDialogBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoadingDialogBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        binding.loadingAnimation.setAnimation("loading_anim.json");
        binding.loadingAnimation.playAnimation();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
