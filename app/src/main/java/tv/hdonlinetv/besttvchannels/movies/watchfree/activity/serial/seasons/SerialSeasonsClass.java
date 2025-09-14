package tv.hdonlinetv.besttvchannels.movies.watchfree.activity.serial.seasons;

import static tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant.INTERSTITIAL_POST_LIST;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;

import com.m3u.data.database.model.ResponseData;
import com.walhalla.data.model.Channel;
import com.walhalla.data.repository.RepoCallback;

import java.util.ArrayList;
import java.util.List;

import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig;
import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.playlist.SerialInfoActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.ChannelAdapter;
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.SeasonAdapter;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentHomeBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.BaseFragment;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;

public class SerialSeasonsClass
        extends BaseFragment implements RepoCallback<List<ResponseData.Season>>, ChannelAdapter.OnItemClickListener {

    final String THISCLAZZNAME = getClass().getSimpleName();

    protected static final String KEY_INTENT_CHANNEL_ID = "key_data1";
    protected FragmentHomeBinding binding;
    private PrefManager prefManager;
    private AdsPref adsPref;
    private AdNetwork adNetwork;

    protected SeasonAdapter categoriesAdapter;
    private List<ResponseData.Season> data;
    private int seriesId;


    public static Fragment newInstance(int seriesId, ArrayList<ResponseData.Season> data) {
        SerialSeasonsClass fragment = new SerialSeasonsClass();
        Bundle args = new Bundle();
        args.putSerializable(KEY_INTENT_CHANNEL_ID, data);
        args.putSerializable(SerialInfoActivity.KEY_SERIES_ID, seriesId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle intent = getArguments();
        if (intent != null && intent.containsKey(KEY_INTENT_CHANNEL_ID)) {
            data = (List<ResponseData.Season>) getArguments().getSerializable(KEY_INTENT_CHANNEL_ID);
            seriesId = getArguments().getInt(SerialInfoActivity.KEY_SERIES_ID, -99);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize ViewBinding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        prefManager = new PrefManager(getActivity());
        adsPref = new AdsPref(getActivity());
        adNetwork = new AdNetwork(getActivity());
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_LIST);

//        showRefresh(true);
//        binding.swipeRefreshLayout.setOnRefreshListener(() -> refreshData());

        binding.recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        binding.recyclerView.setLayoutManager(gridLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), gridLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.custom_divider));
        binding.recyclerView.addItemDecoration(dividerItemDecoration);
        categoriesAdapter = new SeasonAdapter(data, new SeasonAdapter.OnSeasonClickListener() {
            @Override
            public void onSeasonClick(ResponseData.Season season) {
                if (BuildConfig.DEBUG) {
                    //Toast.makeText(getContext(), "@@@" + season.getId(), Toast.LENGTH_SHORT).show();
                    //DLog.d("@@@@ http://iptv.icsnleb.com:25461/series/12/12/" + seriesId + "/" + season.getId());

                }
            }
        });
        binding.recyclerView.setAdapter(categoriesAdapter);
        loadCategory();
        return view;
    }

    @Override
    public void successResult(List<ResponseData.Season> data) {

    }

    @Override
    public void errorResult(String err) {

    }

    @Override
    public void onItemClick(View view, Channel obj, int position) {

    }

    public void showInterstitialAd() {
        adNetwork.showInterstitialAdNetwork(INTERSTITIAL_POST_LIST, adsPref.getInterstitialAdInterval());
    }


    protected void loadCategory() {
        if (data != null) {
            setBadgeText(THISCLAZZNAME, String.valueOf(data.size()));
//            for (ResponseData.Season season : data) {
//
//            }
//            categoriesAdapter.swapData(data);
        }
    }
}
