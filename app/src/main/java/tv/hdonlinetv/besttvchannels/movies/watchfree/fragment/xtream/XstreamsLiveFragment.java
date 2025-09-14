package tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.xtream;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.m3u.data.parser.xtream.XtreamInput;
import com.walhalla.data.model.Channel;
import com.walhalla.data.repository.XtreamPresenter;

import java.util.List;

import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig;
import tv.hdonlinetv.besttvchannels.movies.watchfree.Const;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentFavoriteBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;


public class XstreamsLiveFragment extends CaseChannelListFragment {


    private static final String STREAM_TYPE = "STREAM_TYPE";

    private FragmentFavoriteBinding binding;

    private final String THISCLAZZNAME = getClass().getSimpleName();
    private PrefManager prf;
    private XtreamPresenter presenter;
    public static final String ARG_XTREAM_INPUT = "arg_xtream_input";

    private XtreamInput xtreamInput;

    public static XstreamsLiveFragment newInstance(XtreamInput xtreamInput) {
        XstreamsLiveFragment fragment = new XstreamsLiveFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_XTREAM_INPUT, xtreamInput);
        fragment.setArguments(args);
        return fragment;
    }

    //private String action;


    public XstreamsLiveFragment() {
        // Required empty public constructor
    }

//    public static Fragment newInstance(StreamType streamType) {
//        Fragment fragment = new XStreamsFragment();
//        Bundle args = new Bundle();
//        args.putString(STREAM_TYPE, streamType.getAction());
//        fragment.setArguments(args);
//        return fragment;
//    }


//    public static FavoritesFragment newInstance(long playlistId) {
//        FavoritesFragment fragment = new FavoritesFragment();
//        Bundle args = new Bundle();
//        args.putLong(ARG_PLAYLIST_ID, playlistId);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Handler handler = new Handler(Looper.getMainLooper());

        if (getArguments() != null) {
            xtreamInput = (XtreamInput) getArguments().getSerializable(ARG_XTREAM_INPUT);
            presenter = new XtreamPresenter(handler, getContext(), xtreamInput);
            prf = new PrefManager(getContext());
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false);
        //binding.recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), prf.getInt(Const.KEY_CHANNEL_COLUMNS));
        binding.recyclerView.setLayoutManager(gridLayoutManager);
        channelAdapter.setOnItemClickListener(this);
        binding.recyclerView.setAdapter(channelAdapter);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        //initCheck();
        if (BuildConfig.DEBUG) {
            //Toast.makeText(getContext(), "" + playlistId, Toast.LENGTH_SHORT).show();
        }
        presenter.getLiveStreams(xtreamInput, this);
    }

    @Override
    public void successResult(List<Channel> tmp) {
        if (tmp != null) {
            binding.noFavorite.setVisibility(tmp.isEmpty() ? View.VISIBLE : View.GONE);
            this.setBadgeText(THISCLAZZNAME, String.valueOf(tmp.size()));
            channelAdapter.swapData(tmp);
        }
    }

    @Override
    public void errorResult(String err) {

    }

    @Override
    public void onItemClick(View view, Channel channel, int position) {
        presenter.onStreamItemClick(channel, XtreamPresenter.Action.get_live_streams);
    }


}
