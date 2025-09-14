package tv.hdonlinetv.besttvchannels.movies.watchfree.activity;

import static tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant.BANNER_HOME;
import static tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant.INTERSTITIAL_POST_LIST;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;


import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.player.PlrActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.ChannelAdapter;
import tv.hdonlinetv.besttvchannels.movies.watchfree.Const;

import com.walhalla.data.model.Channel;
import com.walhalla.data.repository.AllChannelPresenter;
import com.walhalla.data.repository.RepoCallback;

import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityChannelsBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;

import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends BaseActivity implements RepoCallback<List<Channel>>, ChannelAdapter.OnItemClickListener {

    private ActivityChannelsBinding binding;

    PrefManager prf;
    //DatabaseReference wallpaperReference;


    //, favList;
    ChannelAdapter channelAdapter;
    String categoryName;
    private final String TAG = SearchActivity.class.getSimpleName();
    AdNetwork adNetwork;
    AdsPref adsPref;

    String upperCase;
    private AllChannelPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChannelsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Handler handler = new Handler(Looper.getMainLooper());
        presenter = new AllChannelPresenter(handler, this);
        prf = new PrefManager(this);

        Intent intent = getIntent();
        categoryName = intent.getStringExtra(Const.KEY_CATEGOTY_NAME);

        upperCase = categoryName.substring(0, 1).toUpperCase() + categoryName.substring(1);

        setSupportActionBar(binding.toolbar);
        setTitle(categoryName);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        adsPref = new AdsPref(this);
        adNetwork = new AdNetwork(this);
        adNetwork.loadBannerAdNetwork(BANNER_HOME);
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_LIST);

        //@@@@    binding.swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        binding.recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, prf.getInt(Const.KEY_CHANNEL_COLUMNS));
        binding.recyclerView.setLayoutManager(gridLayoutManager);
        channelAdapter = new ChannelAdapter(this, new ArrayList<>());
        binding.recyclerView.setAdapter(channelAdapter);
        loadPictures();

        channelAdapter.setOnItemClickListener(this);
    }

    public void showInterstitialAd() {
        adNetwork.showInterstitialAdNetwork(INTERSTITIAL_POST_LIST, adsPref.getInterstitialAdInterval());
    }

    private void loadPictures() {
        //favList = new ArrayList<>();
        fetchWallpapers(categoryName);
    }

    private void fetchWallpapers(final String categoryName) {
        binding.lytNoItem.getRoot().setVisibility(View.GONE);

        presenter.searchChannel(categoryName, this);

    }

//    private void fetchWallpapers(final String categoryName) {wallpaperReference = FirebaseDatabase.getInstance().getReference("Channels");
//        wallpaperReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                showRefresh(false);
//                if (dataSnapshot.exists()) {
//                    binding.lytNoItem.getRoot().setVisibility(View.GONE);
//                    for (DataSnapshot wallpaperSnapshot : dataSnapshot.getChildren()) {
//
//                        String id = wallpaperSnapshot.child("id").getValue(String.class);
//                        String wallpaper = wallpaperSnapshot.child(Const.CH_IMG).getValue(String.class);
//                        String name = wallpaperSnapshot.child(Const.CH_NAME).getValue(String.class);
//                        String category = wallpaperSnapshot.child(Const.CH_CAT).getValue(String.class);
//                        String type = wallpaperSnapshot.child(Const.CH_TYPE).getValue(String.class);
//                        String link = wallpaperSnapshot.child(Const.CH_LNK).getValue(String.class);
//                        String desc = wallpaperSnapshot.child(Const.CH_DESC).getValue(String.class);
//                        String language = wallpaperSnapshot.child(Const.CH_LANG).getValue(String.class);
//
//                        Channel wallpaper1 = new Channel(id, wallpaper, name, category, type, link, desc, language);
//
//                        favList.add(0, wallpaper1);
//                    }
//                    for (Channel wallpaper : favList) {
//                        if (wallpaper.getName().contains(categoryName) || wallpaper.getCat().contains(categoryName) ||
//                                wallpaper.getName().contains(upperCase)) {
//                            wallpaperList.add(wallpaper);
//                        }
//                    }
//                    if (wallpaperList.size() == 0) {
//                        binding.lytNoItem.setVisibility(View.VISIBLE);
//                    }
//                    channelAdapter.notifyDataSetChanged();
//                } else {
//                    binding.lytNoItem.setVisibility(View.VISIBLE);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(SearchActivity.this, "Empty", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    private void refreshData() {
//        wallpaperList.clear();
//        channelAdapter.notifyDataSetChanged();
//        new Handler().postDelayed(this::loadPictures, 2000);
//    }

    private void showRefresh(boolean show) {
//        binding.swipeRefreshLayout.setRefreshing(show);
//        if (!show) {
//            new Handler().postDelayed(() -> {
//                binding.swipeRefreshLayout.setRefreshing(false)
//            }, 500);
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCheck();
    }

    private void initCheck() {
        if (prf.loadNightModeState()) {
            Log.d("Dark", "MODE");
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);   // set status text dark
        }
    }

    @Override
    public void successResult(List<Channel> tmp) {
        if (tmp.isEmpty()) {
            binding.lytNoItem.getRoot().setVisibility(View.VISIBLE);
        }
        channelAdapter.swapData(tmp);
    }

    @Override
    public void errorResult(String err) {

    }

    @Override
    public void onItemClick(View view, Channel channel, int position) {
        boolean isDetailsMode = prf.isDetailsMode();
        if (isDetailsMode) {
            long channelId = channel._id;
            Intent intent = DetailsActivity.newInstance(this, channelId);
            startActivity(intent);
            showInterstitialAd();
        } else {
            Intent intents = PlrActivity.playerIntent(this, channel);
            startActivity(intents);
        }
    }
}