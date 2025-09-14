package tv.hdonlinetv.besttvchannels.movies.watchfree.xtream;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentAddProfileBinding;

public class AddProfileFragment extends Fragment {

    private FragmentAddProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Обработка кнопки "Cancel"
        binding.cancelButton.setOnClickListener(v -> {
            requireActivity().onBackPressed();  // Вернуться назад к предыдущему экрану
        });

        // Обработка кнопки "Save"
        binding.saveButton.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String profileName = binding.profileNameInput.getText().toString();
        String username = binding.usernameInput.getText().toString();
        String password = binding.passwordInput.getText().toString();
        String serverUrl = binding.serverUrlInput.getText().toString();

        // Логика сохранения профиля (например, через API или в локальной базе данных)
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}