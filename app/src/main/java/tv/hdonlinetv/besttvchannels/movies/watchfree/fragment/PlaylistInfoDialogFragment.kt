package tv.hdonlinetv.besttvchannels.movies.watchfree.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.walhalla.data.model.PlaylistImpl
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentPlaylistInfoDialogBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.DateFormatUtils

class PlaylistInfoDialogFragment(private val playlist: PlaylistImpl) : DialogFragment() {
    private var binding: FragmentPlaylistInfoDialogBinding? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val w = dialog.getWindow()
        if (w != null) {
            w.requestFeature(Window.FEATURE_NO_TITLE)
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = getDialog()
        if (dialog != null) {
            val w = dialog.getWindow()
            if (w != null) {
                w.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                //w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlaylistInfoDialogBinding.inflate(inflater, container, false)
        return binding!!.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Заполнение данными
        binding!!.etPlaylistName.setText(playlist.title)
        binding!!.etFileName.setText(playlist.fileName)
        binding!!.tvImportDate.setText(DateFormatUtils.formatUpdateTime(playlist.importDate))
        binding!!.tvChannelCount.text = playlist.count.toString()
        binding!!.cbAutoUpdate.setChecked(playlist.autoUpdate)

        // Слушатель для кнопки Экспорт
        binding!!.btnExport.setOnClickListener(View.OnClickListener { v: View? -> })

        // Слушатель для кнопки Сохранить
        binding!!.btnSave.setOnClickListener(View.OnClickListener { v: View? ->
            // Логика сохранения изменений
            playlist.title=(binding!!.etPlaylistName.text.toString())
            // Сохранить данные через ViewModel или напрямую в базу данных
            dismiss()
        })

        // Слушатель для кнопки Закрыть
        binding!!.btnClose.setOnClickListener(View.OnClickListener { v: View? -> dismiss() })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

