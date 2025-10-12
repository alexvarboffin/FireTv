package tv.hdonlinetv.besttvchannels.movies.watchfree.fragment

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig

/**
 * Created by combo on 20.04.2017.
 */
abstract class BaseFragment : Fragment() {
    protected var mListener: IOnFragmentInteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IOnFragmentInteractionListener) {
            mListener = context as IOnFragmentInteractionListener
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement IOnFragmentInteractionListener"
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (BuildConfig.DEBUG) {
            Toast.makeText(context, this.javaClass.simpleName, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDetach() {
        super.onDetach()
        //mListener = null;
    }

    protected fun setBadgeText(thisclazzname: String, s: String) {
        if (mListener != null) {
            mListener!!.setBadgeText(thisclazzname, s)
        }
    }
    //    @Override public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    //        super.onViewCreated(view, savedInstanceState);
    //        ButterKnife.bind(this, view);
    //        initInstances();
    //    }
    //    protected abstract void initInstances();
}
