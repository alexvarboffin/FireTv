package tv.hdonlinetv.besttvchannels.movies.watchfree.activity;

import static tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant.BANNER_HOME;
import static tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant.INTERSTITIAL_POST_LIST;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.player.PlrActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityDetailsBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig;


import tv.hdonlinetv.besttvchannels.movies.watchfree.R;

import com.walhalla.data.model.Channel;

import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;

import com.walhalla.data.repository.AllChannelPresenter;
import com.walhalla.data.repository.RepoCallback;
import com.walhalla.ui.DLog;

public class DetailsActivity extends BaseActivity implements RepoCallback<Channel> {

    private static final String KEY_INTENT_CHANNEL_ID = "channel_id";
    long channelId;


    private PrefManager prf;
    private Menu mMenuItem;
    private ActivityDetailsBinding binding;

    private AdNetwork adNetwork;
    private AdsPref adsPref;
    private boolean fullscreen = false;
    private AllChannelPresenter presenter;
    private ChannelViewModel viewModel;

    public static Intent newInstance(@NonNull Context context, long channelId) {
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra(KEY_INTENT_CHANNEL_ID, channelId);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        Handler handler = new Handler(Looper.getMainLooper());
        presenter = new AllChannelPresenter(handler, this);

        // Инициализация прочих переменных
        prf = new PrefManager(this);
        adsPref = new AdsPref(this);
        adNetwork = new AdNetwork(this);
        adNetwork.loadBannerAdNetwork(BANNER_HOME);
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_LIST);

        viewModel = new ViewModelProvider(this).get(ChannelViewModel.class);

        handleIntent0();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void loaddata(long channelId) {
        if (BuildConfig.DEBUG) {
            DLog.d("@@CHANNEL->>@@" + viewModel.getChannel().getValue() + "@@@" + channelId);
        }
        if (channelId > 0) {
            presenter.getChannelById(channelId, this);
        }
    }

    private void updateUI(final Channel channel) {
        if (BuildConfig.DEBUG) {
            DLog.d("@@CHANNEL->>@@" + viewModel.getChannel().getValue());
        }
        // Установите название активности
        setTitle(channel.getName());
        setMenuIcon(channel.liked > 0);
        binding.channelName.setText(channel.getName());

        String dsk = channel.getDesc();
        if (BuildConfig.DEBUG) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            dsk = gson.toJson(channel);
        }
        binding.channelDetails.setText(dsk);


        binding.tvCategory.setText("Category: " + channel.getCat());

        // Загрузка изображения
        String channelImage = channel.getCover();
        Glide.with(this)
                .load(channelImage)
                .placeholder(R.drawable.placeholder)
                .listener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                        DLog.d("@@@@" + channelImage);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).into(binding.channelImage);

        // Обработка клика на кнопку воспроизведения
        binding.channelPlay.setOnClickListener(view -> {
            if (channel.getType().equals("youtube")) {
                String[] separated = channel.getLnk().split("=");
                Intent intents = new Intent(DetailsActivity.this, ActivityYoutubePlayer.class);
                intents.putExtra("id", separated[1]);
                startActivity(intents);
            } else {

//                //String channelImage0 = channel.getLang();
//                String channelImage0 = channel.getImg();
//
//                Intent intents = PlayerActivity.newInstance0(
//                        this, channel.getName(),
//                        channelImage0, channel.getLnk()
//                );


                Intent intents = PlrActivity.playerIntent(this, channel);
                startActivity(intents);
            }
        });
    }

    private void handleIntent0() {
        Intent intent = getIntent();
        if (intent.hasExtra(KEY_INTENT_CHANNEL_ID)) {
            channelId = intent.getLongExtra(KEY_INTENT_CHANNEL_ID, -1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_favorite, menu);
        mMenuItem = menu;
        DLog.d("@@@");

        if (viewModel == null) {
            return true;
        }
        Channel channel = viewModel.getChannel().getValue();
        if (channel == null) {
            return true;
        }
        setMenuIcon(channel.liked > 0);

//        presenter.isFavoriteChannel(channel, new RepoCallback<>() {
//            @Override
//            public void successResult(Boolean isFavoriteChannel) {
//                if (isFavoriteChannel)
//                    mMenuItem.getItem(0).setIcon(R.drawable.ic_favorite_fill);
//                else
//                    --false--
//            }
//
//            @Override
//            public void errorResult(String err) {
//
//            }
//        });
        return true;
    }

    private void setMenuIcon(boolean b) {
        if (mMenuItem != null) {
            mMenuItem.getItem(0).setIcon(
                    b ? R.drawable.ic_favorite_fill_green : R.drawable.ic_favorite_border);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_fav) {

            if (viewModel == null) {
                return super.onOptionsItemSelected(item);
            }
            Channel channel = viewModel.getChannel().getValue();
            if (channel == null) {
                return super.onOptionsItemSelected(item);
            }

//            presenter.isFavoriteChannel(channel, new RepoCallback<Boolean>() {
//                @Override
//                public void successResult(Boolean isFavoriteChannel) {
//                    if (!isFavoriteChannel) {
//                        mMenuItem.getItem(0).setIcon(R.drawable.ic_favorite_fill);
//                        Toast.makeText(DetailsActivity.this, R.string.add_fav, Toast.LENGTH_SHORT).show();
//                        try {
////                    Gson GSON = new GsonBuilder().setPrettyPrinting().create();
////                    DLog.d(GSON.toJson(channel));
//                            repo.addFavorite(channel);
//                        } catch (Exception e) {
//                            DLog.handleException(e);
//                        }
//                    } else {
//                        --false--
//                        Toast.makeText(DetailsActivity.this, R.string.remove_fav, Toast.LENGTH_SHORT).show();
//                        repo.deleteFavorite(channel);
//                    }
//                }
//
//                @Override
//                public void errorResult(String err) {
//
//                }
//            });

            if (channel.liked <= 0) {
                Toast.makeText(DetailsActivity.this, R.string.add_fav, Toast.LENGTH_SHORT).show();
                try {
//                    Gson GSON = new GsonBuilder().setPrettyPrinting().create();
//                    DLog.d(GSON.toJson(channel));
                    presenter.addFavorite(channel, new RepoCallback<Integer>() {
                        @Override
                        public void successResult(Integer data) {
                            setMenuIcon(true);
                        }

                        @Override
                        public void errorResult(String err) {

                        }
                    });
                } catch (Exception e) {
                    DLog.handleException(e);
                }
            } else {
                setMenuIcon(false);
                Toast.makeText(DetailsActivity.this, R.string.remove_fav, Toast.LENGTH_SHORT).show();
                presenter.deleteFavorite(channel);
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DLog.d("@@@");
        initCheck();
        loaddata(channelId);
    }

    private void initCheck() {
        if (prf.loadNightModeState()) {
            Log.d("Dark", "MODE");
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // set status text dark
        }
    }

    @Override
    public void successResult(Channel channel) {
        viewModel.getChannel().observe(this, this::updateUI);
        viewModel.setChannel(channel);
    }

    @Override
    public void errorResult(String err) {

    }
}
