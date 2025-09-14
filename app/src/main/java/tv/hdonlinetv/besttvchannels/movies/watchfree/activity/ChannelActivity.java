package tv.hdonlinetv.besttvchannels.movies.watchfree.activity;

import static tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant.BANNER_HOME;
import static tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant.INTERSTITIAL_POST_LIST;

import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.walhalla.data.model.Channel;

import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.player.PlrActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.ChannelAdapter;

import tv.hdonlinetv.besttvchannels.movies.watchfree.Const;

import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityChannelsBinding;

import com.walhalla.data.repository.AllChannelPresenter;
import com.walhalla.data.repository.RepoCallback;

import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;

import java.util.ArrayList;
import java.util.List;

public class ChannelActivity extends BaseActivity implements ChannelAdapter.OnItemClickListener {

    PrefManager prf;

    //DatabaseReference wallpaperReference;
    //List<Channel> favList;
    ChannelAdapter channelAdapter;

    String categoryName;
    private final String TAG = ChannelActivity.class.getSimpleName();
    AdNetwork adNetwork;
    AdsPref adsPref;

    private ActivityChannelsBinding binding;
    private AllChannelPresenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Инициализируйте ViewBinding
        binding = ActivityChannelsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Handler handler = new Handler(Looper.getMainLooper());
        presenter = new AllChannelPresenter(handler, this);

        prf = new PrefManager(this);
        handleIntent0();

        setTitle(categoryName);
        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        adsPref = new AdsPref(this);
        adNetwork = new AdNetwork(this);
        adNetwork.loadBannerAdNetwork(BANNER_HOME);
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_LIST);

        handleRefresh();

        loadPictures();

        channelAdapter.setOnItemClickListener(this);
    }


    private void handleIntent0() {
        Intent intent = getIntent();
        categoryName = intent.getStringExtra(Const.KEY_CATEGOTY_NAME);
    }

    public void showInterstitialAd() {
        adNetwork.showInterstitialAdNetwork(INTERSTITIAL_POST_LIST, adsPref.getInterstitialAdInterval());
    }

    private void loadPictures() {
//        favList = new ArrayList<>();
//        wallpaperList = new ArrayList<>();

        binding.recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, prf.getInt(Const.KEY_CHANNEL_COLUMNS));
        binding.recyclerView.setLayoutManager(gridLayoutManager);

        channelAdapter = new ChannelAdapter(this, new ArrayList<>());
        binding.recyclerView.setAdapter(channelAdapter);


        fetchWallpapers(categoryName);
    }


    private void fetchWallpapers(final String categoryName) {
        Handler handler = new Handler(Looper.getMainLooper());

        presenter.getChannelsInCategory(categoryName, new RepoCallback<List<Channel>>() {
            @Override
            public void successResult(List<Channel> data) {
                if (data.isEmpty()) {
                    binding.lytNoItem.getRoot().setVisibility(View.VISIBLE);
                }
                channelAdapter.swapData(data);
            }

            @Override
            public void errorResult(String err) {

            }
        });

    }

//    private void fetchWallpapers(final String categoryName) {
//        wallpaperReference = FirebaseDatabase.getInstance().getReference("Channels");
//        wallpaperReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                showRefresh(false);
//                if (dataSnapshot.exists()) {
//                    for (DataSnapshot wallpaperSnapshot : dataSnapshot.getChildren()) {
//                        binding.lytNoItem.getRoot().setVisibility(View.GONE);
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
//                    for (int i = 0; i < favList.size(); i++) {
//                        if (favList.get(i).getCat().equals(categoryName)) {
//                            wallpaperList.add(favList.get(i));
//                            if (wallpaperList.size() == 0) {
//                                Toast.makeText(ChannelActivity.this, "NO DATA", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    }
//                    if (wallpaperList.size() == 0) {
//                        binding.lytNoItem.getRoot().setVisibility(View.VISIBLE);
//                    }
//                    channelAdapter.notifyDataSetChanged();
//                } else {
//                    binding.lytNoItem.getRoot().setVisibility(View.VISIBLE);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(ChannelActivity.this, "Empty", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    private void refreshData() {
//        wallpaperList.clear();
//        channelAdapter.notifyDataSetChanged();
//        new Handler().postDelayed(this::loadPictures, 2000);
//    }

    private void handleRefresh() {
//        binding.swipeRefreshLayout.setOnRefreshListener(() -> refreshData());
//        showRefresh(true);
    }

    private void showRefresh(boolean show) {
//        if (show) {
//            binding.swipeRefreshLayout.setRefreshing(true);
//        } else {
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