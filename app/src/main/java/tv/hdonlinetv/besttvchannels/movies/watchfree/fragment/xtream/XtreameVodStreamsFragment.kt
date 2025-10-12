package tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.xtream;

import static tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.xtream.XstreamsLiveFragment.ARG_XTREAM_INPUT;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.m3u.data.parser.xtream.XtreamInput;
import com.walhalla.data.model.Channel;
import com.walhalla.data.repository.XtreamPresenter;

import java.util.List;

import tv.hdonlinetv.besttvchannels.movies.watchfree.Const;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentFavoriteBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;


public class XtreameVodStreamsFragment extends CaseChannelListFragment {


    private FragmentFavoriteBinding binding;

    private final String THISCLAZZNAME = getClass().getSimpleName();
    private PrefManager prf;
    private XtreamPresenter presenter;


    public XtreameVodStreamsFragment() {
        // Required empty public constructor
    }


    private XtreamInput xtreamInput;


    public static XtreameVodStreamsFragment newInstance(XtreamInput xtreamInput) {
        XtreameVodStreamsFragment fragment = new XtreameVodStreamsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_XTREAM_INPUT, xtreamInput);
        fragment.setArguments(args);
        return fragment;
    }

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
//        if (BuildConfig.DEBUG) {
//            Toast.makeText(getContext(), "123", Toast.LENGTH_SHORT).show();
//        }
        presenter.getVodStreams(xtreamInput, this);
    }

    @Override
    public void successResult(List<Channel> tmp) {
        binding.noFavorite.setVisibility(tmp.isEmpty() ? View.VISIBLE : View.GONE);
        this.setBadgeText(THISCLAZZNAME, String.valueOf(tmp.size()));
        channelAdapter.swapData(tmp);
    }

    @Override
    public void errorResult(String err) {
        Toast.makeText(getContext(), err, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onItemClick(View view, Channel channel, int position) {
        presenter.onStreamItemClick(channel, XtreamPresenter.Action.get_vod_streams);
    }
}
