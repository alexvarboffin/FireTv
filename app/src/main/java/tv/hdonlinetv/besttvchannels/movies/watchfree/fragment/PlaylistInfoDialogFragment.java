package tv.hdonlinetv.besttvchannels.movies.watchfree.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.walhalla.data.model.PlaylistImpl;

import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentPlaylistInfoDialogBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.DateFormatUtils;

public class PlaylistInfoDialogFragment extends DialogFragment {

    private FragmentPlaylistInfoDialogBinding binding;
    private PlaylistImpl playlist;

    public PlaylistInfoDialogFragment(PlaylistImpl playlist) {
        this.playlist = playlist;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window w = dialog.getWindow();
        if (w != null) {
            w.requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window w = dialog.getWindow();
            if (w != null) {
                w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                //w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPlaylistInfoDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Заполнение данными
        binding.etPlaylistName.setText(playlist.getTitle());
        binding.etFileName.setText(playlist.getFileName());
        binding.tvImportDate.setText(DateFormatUtils.formatUpdateTime(playlist.getImportDate()));
        binding.tvChannelCount.setText(String.valueOf(playlist.getCount()));
        binding.cbAutoUpdate.setChecked(playlist.isAutoUpdate());

        // Слушатель для кнопки Экспорт
        binding.btnExport.setOnClickListener(v -> {
            // Реализация экспорта плейлиста
        });

        // Слушатель для кнопки Сохранить
        binding.btnSave.setOnClickListener(v -> {
            // Логика сохранения изменений
            playlist.setTitle(binding.etPlaylistName.getText().toString());
            // Сохранить данные через ViewModel или напрямую в базу данных
            dismiss();
        });

        // Слушатель для кнопки Закрыть
        binding.btnClose.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

