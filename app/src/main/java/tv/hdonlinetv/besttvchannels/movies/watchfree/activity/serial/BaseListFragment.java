package tv.hdonlinetv.besttvchannels.movies.watchfree.activity.serial;

import static tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant.INTERSTITIAL_POST_LIST;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.walhalla.data.repository.RepoCallback;

import java.util.ArrayList;
import java.util.List;

import tv.hdonlinetv.besttvchannels.movies.watchfree.Const;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.ChannelActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.CategoryAdapter;
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.ChannelAdapter;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentHomeBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.BaseFragment;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;


public abstract class BaseListFragment<T>
        extends BaseFragment implements RepoCallback<List<T>>, ChannelAdapter.OnItemClickListener {

    protected final String THISCLAZZNAME = getClass().getSimpleName();

    T data;
    protected static final String KEY_INTENT_CHANNEL_ID = "key_data1";
    protected FragmentHomeBinding binding;
    private PrefManager prefManager;
    private AdsPref adsPref;
    private AdNetwork adNetwork;

    protected CategoryAdapter categoriesAdapter;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle intent = getArguments();
        if (intent != null && intent.containsKey(KEY_INTENT_CHANNEL_ID)) {
            data =  (T) getArguments().getSerializable(KEY_INTENT_CHANNEL_ID);
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
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        binding.recyclerView.setLayoutManager(gridLayoutManager);

        categoriesAdapter = new CategoryAdapter(getContext(), new ArrayList<>());
        binding.recyclerView.setAdapter(categoriesAdapter);
        categoriesAdapter.setOnItemClickListener((view0, obj, position) -> {
            Intent intent = new Intent(getContext(), ChannelActivity.class);
            intent.putExtra(Const.KEY_CATEGOTY_NAME, obj.getName());
            startActivity(intent);
            showInterstitialAd();
        });
        loadCategory();
        return view;
    }

    protected abstract void loadCategory();


    public void showInterstitialAd() {
        adNetwork.showInterstitialAdNetwork(INTERSTITIAL_POST_LIST, adsPref.getInterstitialAdInterval());
    }
}
