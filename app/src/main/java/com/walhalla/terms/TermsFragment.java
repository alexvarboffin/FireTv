package com.walhalla.terms;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.walhalla.utils.AssetUtils;

import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentTermsBinding;


public class TermsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    private FragmentTermsBinding binding;
    private ITerms callback;

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        buttonView.setError(null);
        if (callback != null) {
            callback.isTermsAccepted(isChecked);
        }
    }

    public void showError() {
        binding.checkboxAccept.setError(getString(R.string.error_msg_terms));
        binding.checkboxAccept.requestFocus();

    }

    public interface ITerms {
        void isTermsAccepted(boolean b);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Используем ViewBinding для инфлейтирования макета
        binding = FragmentTermsBinding.inflate(inflater, container, false);
        binding.checkboxAccept.setOnCheckedChangeListener(this);

        // Загрузка и установка текста с заменой плейсхолдеров
        String termsText = replacePlaceholders(AssetUtils.loadFromAsset(requireActivity(), "terms_of_service.txt"));
        binding.terms.setText(termsText);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private String replacePlaceholders(String termsText) {
        String appName = getString(R.string.app_name);
        String publisherName = getString(R.string.play_google_pub);

        // Заменяем плейсхолдеры на реальные значения
        termsText = termsText.replace("%app%", appName);
        termsText = termsText.replace("%dev%", publisherName);

        return termsText;
    }

    public boolean isTermsAccepted() {
        return binding.checkboxAccept.isChecked();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ITerms) {
            callback = (ITerms) context;
        } else {
            throw new RuntimeException(context + " must implement ITerms");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
}

