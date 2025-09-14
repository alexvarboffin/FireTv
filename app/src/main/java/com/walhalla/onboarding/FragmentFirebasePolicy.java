package com.walhalla.onboarding;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentInfo2Binding;


public class FragmentFirebasePolicy extends Fragment {

    public interface IPrivacyPolicy {
        void launchPrivacyPolicy();
        void isFirebaseAccepted(boolean accepted);
    }

    private FragmentInfo2Binding binding;
    private Animation smallToBigAnimation;
    private IPrivacyPolicy callback;

    public static FragmentFirebasePolicy newInstance() {
        return new FragmentFirebasePolicy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        smallToBigAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.small_to_big);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInfo2Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.imageView.startAnimation(smallToBigAnimation);
        binding.tvPrivacyPolicy.setOnClickListener(v -> {
            callback.launchPrivacyPolicy();
        });

        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioDenyConsent) {
                callback.isFirebaseAccepted(false);
            } else if (checkedId == R.id.radioGiveConsent) {
                callback.isFirebaseAccepted(true);
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IPrivacyPolicy) {
            callback = (IPrivacyPolicy) context;
        } else {
            throw new RuntimeException(context + " must implement IPrivacyPolicy");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}