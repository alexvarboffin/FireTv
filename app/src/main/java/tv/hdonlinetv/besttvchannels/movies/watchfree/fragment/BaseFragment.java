package tv.hdonlinetv.besttvchannels.movies.watchfree.fragment;

import android.content.Context;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig;


/**
 * Created by combo on 20.04.2017.
 */

public abstract class BaseFragment extends Fragment {

    protected IOnFragmentInteractionListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IOnFragmentInteractionListener) {
            mListener = (IOnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement IOnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            Toast.makeText(getContext(), this.getClass().getSimpleName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    protected void setBadgeText(String thisclazzname, String s) {
        if (mListener != null) {
            mListener.setBadgeText(thisclazzname, s);
        }
    }

//    @Override public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        ButterKnife.bind(this, view);
//        initInstances();
//    }
//    protected abstract void initInstances();
}
