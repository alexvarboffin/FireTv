package tv.hdonlinetv.besttvchannels.movies.watchfree.xtream

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentAddProfileBinding

class AddProfileFragment : Fragment() {
    private var binding: FragmentAddProfileBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddProfileBinding.inflate(inflater, container, false)
        return binding!!.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Обработка кнопки "Cancel"
        binding!!.cancelButton.setOnClickListener { v: View? ->
            requireActivity().onBackPressed() // Вернуться назад к предыдущему экрану
        }

        // Обработка кнопки "Save"
        binding!!.saveButton.setOnClickListener { v: View? -> saveProfile() }
    }

    private fun saveProfile() {
        val profileName = binding!!.profileNameInput.getText().toString()
        val username = binding!!.usernameInput.getText().toString()
        val password = binding!!.passwordInput.getText().toString()
        val serverUrl = binding!!.serverUrlInput.getText().toString()

        // Логика сохранения профиля (например, через API или в локальной базе данных)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}