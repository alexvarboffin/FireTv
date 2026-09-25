package com.walhalla.terms

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import com.walhalla.utils.AssetUtils
import tv.hdonlinetv.besttvchannels.movies.watchfree.R
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentTermsBinding

class TermsFragment : Fragment(), CompoundButton.OnCheckedChangeListener {
    private var binding: FragmentTermsBinding? = null
    private var callback: ITerms? = null

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        buttonView.error = null
        if (callback != null) {
            callback!!.isTermsAccepted(isChecked)
        }
    }

    fun showError() {
        binding!!.checkboxAccept.error = getString(R.string.error_msg_terms)
        binding!!.checkboxAccept.requestFocus()
    }

    interface ITerms {
        fun isTermsAccepted(b: Boolean)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Используем ViewBinding для инфлейтирования макета
        binding = FragmentTermsBinding.inflate(inflater, container, false)
        binding!!.checkboxAccept.setOnCheckedChangeListener(this)

        // Загрузка и установка текста с заменой плейсхолдеров
        val termsText =
            replacePlaceholders(AssetUtils.loadFromAsset(requireActivity(), "terms_of_service.txt"))
        binding!!.terms.text = termsText
        return binding!!.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun replacePlaceholders(termsText: String): String {
        var termsText = termsText
        val appName = getString(R.string.app_name)
        val publisherName = getString(R.string.play_google_pub)

        // Заменяем плейсхолдеры на реальные значения
        termsText = termsText.replace("%app%", appName)
        termsText = termsText.replace("%dev%", publisherName)

        return termsText
    }

    val isTermsAccepted: Boolean
        get() = binding!!.checkboxAccept.isChecked

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ITerms) {
            callback = context as ITerms
        } else {
            throw RuntimeException("$context must implement ITerms")
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }
}

