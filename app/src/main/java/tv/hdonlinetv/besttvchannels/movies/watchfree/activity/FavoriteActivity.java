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

import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.player.PlrActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.ChannelAdapter;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityFavoriteBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.Const;

import com.walhalla.data.model.Channel;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;

import com.walhalla.data.repository.AllChannelPresenter;
import com.walhalla.data.repository.RepoCallback;
import com.walhalla.ui.DLog;

import java.util.List;

public class FavoriteActivity extends BaseActivity implements ChannelAdapter.OnItemClickListener {

    private ActivityFavoriteBinding binding;
    private ChannelAdapter favoriteAdapter;


    private PrefManager prf;
    private AdNetwork adNetwork;
    private AdsPref adsPref;
    private final String TAG = FavoriteActivity.class.getSimpleName();
    private AllChannelPresenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Инициализируйте ViewBinding
        binding = ActivityFavoriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Настройка тулбара
        setSupportActionBar(binding.toolbar);
        setTitle("Favorite");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Инициализация других переменных
        prf = new PrefManager(this);
        adsPref = new AdsPref(this);
        adNetwork = new AdNetwork(this);
        adNetwork.loadBannerAdNetwork(BANNER_HOME);
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_LIST);


        // Настройка RecyclerView
        binding.rec.setHasFixedSize(true);
        binding.rec.setLayoutManager(new GridLayoutManager(this, prf.getInt(Const.KEY_CHANNEL_COLUMNS)));

    }

    public void showInterstitialAd() {
        adNetwork.showInterstitialAdNetwork(INTERSTITIAL_POST_LIST, adsPref.getInterstitialAdInterval());
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
        Handler handler = new Handler(Looper.getMainLooper());
        presenter = new AllChannelPresenter(handler, this);
        presenter.getAllFavorite(new RepoCallback<List<Channel>>() {
            @Override
            public void successResult(List<Channel> data) {
                List<Channel> mm = data;
                favoriteAdapter = new ChannelAdapter(getApplicationContext(), mm);
                binding.rec.setAdapter(favoriteAdapter);

                // Обработка кликов на элементы
                favoriteAdapter.setOnItemClickListener(FavoriteActivity.this);
                binding.noFavorite.setVisibility(mm.isEmpty() ? View.VISIBLE : View.GONE);
                DLog.d("@@@@");
            }

            @Override
            public void errorResult(String err) {

            }
        });

    }

    private void initCheck() {
        if (prf.loadNightModeState()) {
            Log.d("Dark", "MODE");
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // set status text dark
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
