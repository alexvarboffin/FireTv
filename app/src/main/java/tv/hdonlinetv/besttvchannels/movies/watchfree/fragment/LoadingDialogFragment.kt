package tv.hdonlinetv.besttvchannels.movies.watchfree.fragment

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentLoadingDialogBinding


class LoadingDialogFragment : DialogFragment() {
    private var binding: FragmentLoadingDialogBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoadingDialogBinding.inflate(inflater, container, false)
        val view: View = binding!!.getRoot()
        binding!!.loadingAnimation.setAnimation("loading_anim.json")
        binding!!.loadingAnimation.playAnimation()
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawableResource(R.color.transparent)
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
