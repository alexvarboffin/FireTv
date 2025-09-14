package tv.hdonlinetv.besttvchannels.movies.watchfree.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig;
import tv.hdonlinetv.besttvchannels.movies.watchfree.Const;

import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.DetailsActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.player.PlrActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentFavoriteBinding;

import com.walhalla.data.model.Channel;

import com.walhalla.data.repository.FavoritePresenter;

import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.xtream.CaseChannelListFragment;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;

import java.util.List;

public class FavoritesFragment extends CaseChannelListFragment {

    private FragmentFavoriteBinding binding;

    private final String THISCLAZZNAME = getClass().getSimpleName();
    private PrefManager prf;
    private FavoritePresenter presenter;


    public FavoritesFragment() {
        // Required empty public constructor
    }

    private static final String ARG_PLAYLIST_ID = "ARG_PLAYLIST_ID";
    private long playlistId;

    public static FavoritesFragment newInstance(long playlistId) {
        FavoritesFragment fragment = new FavoritesFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PLAYLIST_ID, playlistId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Handler handler = new Handler(Looper.getMainLooper());
        presenter = new FavoritePresenter(handler, getContext());
        prf = new PrefManager(getContext());
        if (getArguments() != null) {
            playlistId = getArguments().getLong(ARG_PLAYLIST_ID, -1);
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
        presenter.getFavorite(playlistId, this);
    }


//    private void initCheck() {
//        if (prf.loadNightModeState()) {
//            Log.d("Dark", "MODE");
//        } else {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // set status text dark
//        }
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClick(View view, Channel channel, int position) {
        boolean isDetailsMode = prf.isDetailsMode();
        if (isDetailsMode) {
            long channelId = channel._id;
            Intent intent = DetailsActivity.newInstance(getContext(), channelId);
            startActivity(intent);
            //@@@showInterstitialAd();
        } else {
            Intent intents = PlrActivity.playerIntent(getContext(), channel);
            startActivity(intents);
        }
    }

    @Override
    public void successResult(List<Channel> tmp) {
        binding.noFavorite.setVisibility(tmp.isEmpty() ? View.VISIBLE : View.GONE);
        FavoritesFragment.this.setBadgeText(THISCLAZZNAME, String.valueOf(tmp.size()));
        channelAdapter.swapData(tmp);
    }

    @Override
    public void errorResult(String err) {

    }
}
