package tv.hdonlinetv.besttvchannels.movies.watchfree.activity.playlist;

import static tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant.ADMOB;
import static tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant.AD_STATUS_ON;
import static tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant.BANNER_HOME;
import static tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant.INTERSTITIAL_POST_LIST;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.ferfalk.simplesearchview.SimpleSearchView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.rahimlis.badgedtablayout.BadgedTabLayout;
import com.walhalla.data.model.PlaylistImpl;
import com.walhalla.ui.DLog;
import com.walhalla.ui.plugins.Launcher;
import com.walhalla.ui.plugins.Module_U;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.hdonlinetv.besttvchannels.movies.watchfree.Const;
import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.BaseActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.FavoriteActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.SearchActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.SettingsActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.ViewPagerAdapter;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityPlaylistBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.FavoritesFragment;
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.IOnFragmentInteractionListener;
import tv.hdonlinetv.besttvchannels.movies.watchfree.tutorial.TutorialActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.GDPR;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;

public class PlaylistActivity extends BaseActivity

        implements NavigationView.OnNavigationItemSelectedListener,
        IOnFragmentInteractionListener {

    private static final String EXTRA_NAME = "EXTRA_NAME";
    private static final String EXTRA_FILE_NAME = "EXTRA_FILE_NAME";
    private static final String EXTRA_IMPORT_DATE = "EXTRA_IMPORT_DATE";
    private static final String EXTRA_CHANNEL_COUNT = "EXTRA_CHANNEL_COUNT";
    private static final String EXTRA_AUTO_UPDATE = "EXTRA_AUTO_UPDATE";
    private static final String EXTRA_ID = "EXTRA_ID";


    private @NonNull ActivityPlaylistBinding binding;
    //private LocalDatabaseRepo favoriteDatabase;

    ActionBarDrawerToggle actionBarDrawerToggle;


    private static final String TAG = "@@@";
    private PrefManager prf;

    private boolean doubleBackToExitPressedOnce;
    private PrefManager prefManager;
    private AlertDialog dialog;
    private ViewPagerAdapter tabAdapter;
    private String[] tabTitles;

    private PlaylistImpl playlist;
    private List<Fragment> fragmentList;


    public static Intent newInstance(Context context, PlaylistImpl playlist) {
        Intent intent = new Intent(context, PlaylistActivity.class);
        intent.putExtra(EXTRA_ID, playlist._id);
        intent.putExtra(EXTRA_NAME, playlist.getTitle());
        intent.putExtra(EXTRA_FILE_NAME, playlist.getFileName());
        intent.putExtra(EXTRA_IMPORT_DATE, playlist.getImportDate());
        intent.putExtra(EXTRA_CHANNEL_COUNT, playlist.getCount());
        intent.putExtra(EXTRA_AUTO_UPDATE, playlist.isAutoUpdate());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaylistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        prefManager = new PrefManager(this);
        setSupportActionBar(binding.toolbar);
        handleIntent0(getIntent());
//        LocalDatabaseRepo repo = LocalDatabaseRepo.getStoreInfoDatabase(this);
//        List<Channel> allChannels = repo.getChannelsInPlaylist(playlist._id);
//        List<Channel> favoriteChannels = new ArrayList<>();
//        for (Channel channel : allChannels) {
//            if (channel.isFavorite()) {
//                favoriteChannels.add(channel);
//            }
//        }

        prf = new PrefManager(this);
        wads();


        drawer();

        //main Fragment
        makeTabs(binding.tabLayout);


        binding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        long playlistId = playlist._id;
        DLog.d("{}" + playlistId);

        var0(playlistId);

        binding.viewPager.setAdapter(tabAdapter);

        //binding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tabLayout));
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(tabTitles[position])).attach();

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPager.setCurrentItem(tab.getPosition());
                invalidateFragmentMenus(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        //binding.viewPager.setOffscreenPageLimit(1);


        binding.searchView.setOnQueryTextListener(new SimpleSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(@NonNull String query) {
                Log.d("SimpleSearchView", "Submit:" + query);
                Intent intent = new Intent(PlaylistActivity.this, SearchActivity.class);
                intent.putExtra(Const.KEY_CATEGOTY_NAME, query);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(@NonNull String newText) {
                Log.d("SimpleSearchView", "Text changed:" + newText);
                return false;
            }

            @Override
            public boolean onQueryTextCleared() {
                Log.d("SimpleSearchView", "Text cleared");
                return false;
            }
        });

//        binding.addFab.setVisibility(View.GONE);
//        binding.addFab.setOnClickListener(v -> {
//            startActivity(new Intent(this, PlaylistManagementActivity.class));
//        });
    }

    private void drawer() {
//        binding.navView.setNavigationItemSelectedListener(this);
//        actionBarDrawerToggle = new ActionBarDrawerToggle(this,
//                binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle);
//        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
//        actionBarDrawerToggle.syncState();
//
//        binding.toolbar.setNavigationIcon(R.drawable.ic_action_action);
    }

    private void var0(final long id) {
        tabTitles = getResources().getStringArray(R.array.tab_titles_playlist);

        fragmentList = new ArrayList<>();
        fragmentList.add(AllChannelInPlaylistFragment.newInstance(id));
        fragmentList.add(CategoriesForPlaylistFragment.newInstance(id));
        fragmentList.add(FavoritesFragment.newInstance(id));
        //fragmentList.add(new PlaylistFragment());
        tabAdapter = new ViewPagerAdapter(this, getSupportFragmentManager(), fragmentList);
    }

    private void handleIntent0(Intent intent) {
        if (intent != null) {
            String name = intent.getStringExtra(EXTRA_NAME);
            String fileName = intent.getStringExtra(EXTRA_FILE_NAME);
            long importDate = intent.getLongExtra(EXTRA_IMPORT_DATE, 0);
            int channelCount = intent.getIntExtra(EXTRA_CHANNEL_COUNT, 0);
            boolean autoUpdate = intent.getBooleanExtra(EXTRA_AUTO_UPDATE, false);

            playlist = new PlaylistImpl(name, fileName, importDate, channelCount, autoUpdate, -1);
            playlist._id = intent.getLongExtra(EXTRA_ID, -1);
        }
    }

    private void invalidateFragmentMenus(int position) {
//        v1
//        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
//            mPagerAdapter.getItem(i).setHasOptionsMenu(i == position);
//        }
//        if (getActivity() != null) {
//            getActivity().invalidateOptionsMenu(); //or respectively its support method.
//        }
//        for (int i = 0; i < tabAdapter.getItemCount(); i++) {
//            //int item = mBinding.viewpager.getCurrentItem();
//            tabAdapter.getItem(i).setHasOptionsMenu(/*i == item && */i == position);
//            DLog.d("000000 " + i + " " + position);
//        }
        invalidateOptionsMenu(); //or respectively its support method.
    }

    private void makeTabs(BadgedTabLayout tabLayout) {
//        tabLayout.addTab(tabLayout.newTab().setText("Channel"));
//        tabLayout.addTab(tabLayout.newTab().setText("Category"));
//        tabLayout.addTab(tabLayout.newTab().setText("Favorites"));

    }

    private void wads() {
        AdsPref adsPref = new AdsPref(this);

        if (adsPref.getAdStatus().equals(AD_STATUS_ON)) {
            switch (adsPref.getAdType()) {
                case ADMOB:
                    MobileAds.initialize(this, initializationStatus -> {
                        Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                        for (String adapterClass : statusMap.keySet()) {
                            AdapterStatus status = statusMap.get(adapterClass);
                            assert status != null;
                            Log.d("MyApp", String.format("Adapter name: %s, Description: %s, Latency: %d", adapterClass, status.getDescription(), status.getLatency()));
                            Log.d("Open Bidding", "FAN open bidding with AdMob as mediation partner selected");
                        }
                    });
                    new GDPR().init(this);
                    break;
//                case APPLOVIN:
//                    AppLovinSdk.getInstance(this).setMediationProvider(AppLovinMediationProvider.MAX);
//                    AppLovinSdk.getInstance(this).initializeSdk(config -> {
//                    });
//                    final String sdkKey = AppLovinSdk.getInstance(getApplicationContext()).getSdkKey();
//                    if (!sdkKey.equals(getString(R.string.applovin_sdk_key))) {
//                        Log.e(TAG, "AppLovin ERROR : Please update your sdk key in the manifest file.");
//                    }
//                    Log.d(TAG, "AppLovin SDK Key : " + sdkKey);
//                    break;
            }
        }

        AdNetwork adNetwork = new AdNetwork(this);
        adNetwork.loadBannerAdNetwork(BANNER_HOME);
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_LIST);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
       //@@@ binding.drawerLayout.closeDrawer(GravityCompat.START);
//        if (menuItem.getItemId() == R.id.nav_unlock_premium) {
//            startActivity(new Intent(MainActivity.this,PrimeActivity.class));
//        } else
        if (menuItem.getItemId() == R.id.nav_home) {


        } else if (menuItem.getItemId() == R.id.nav_profile) {
            startActivity(new Intent(this, FavoriteActivity.class));
        } else if (menuItem.getItemId() == R.id.nav_setting) {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        } else if (menuItem.getItemId() == R.id.nav_about) {
            Module_U.aboutDialog(this);
        }
//       else if (menuItem.getItemId() == R.id.nav_insta) {
//            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
//                    Uri.parse("http://www.instagram.com/" + Config.INSTAGRAM));
//            startActivity(browserIntent);
//        }
        else if (menuItem.getItemId() == R.id.nav_share) {
            Module_U.shareThisApp(this);
        } else if (menuItem.getItemId() == R.id.nav_rate) {
            Launcher.rateUs(this);
        } else if (menuItem.getItemId() == R.id.nav_feedback) {
            Module_U.feedback(this);
        } else if (menuItem.getItemId() == R.id.action_privacy_policy) {

            Launcher.openBrowser(this, getString(R.string.url_privacy_policy));
        }
        return false;
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


//    private void showAbout() {
//        final Dialog customDialog;
//        LayoutInflater inflater = getLayoutInflater();
//        View customView = inflater.inflate(R.layout.dialog_about, null);
//        customDialog = new Dialog(this, R.style.DialogCustomTheme);
//        customDialog.setContentView(customView);
//        TextView tvClose = customDialog.findViewById(R.id.tvClose);
//
//        tvClose.setOnClickListener(v -> customDialog.dismiss());
//        customDialog.show();
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        binding.searchView.setMenuItem(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
//        else if (item.getItemId() == R.id.actionDisplayChannels) {
//            createAlertDialog(this, prefManager);
//        }
        else if (item.getItemId() == R.id.actionInfo) {
            startActivity(new Intent(this, TutorialActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

//    public void createAlertDialog(Context context, PrefManager prf) {
//        String[] values = getResources().getStringArray(R.array.layout_options);
//
//
//        int checkeditem = prf.getChannelDisplayItemTypeIndex();
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle(R.string.dialog_title_display_channels);
//        builder.setSingleChoiceItems(values, checkeditem, (dialog, item) -> {
//            switch (item) {
//                case 0:
//                    prf.setString(Const.KEY_COL_COUNT, TYPE_GRID);
//                    prf.setInt(Const.KEY_CHANNEL_COLUMNS, 3);
//                    onResume();
//                    startActivity(new Intent(context, MainActivity.class));
//                    finish();
//                    break;
//                case 1:
//                    prf.setString(Const.KEY_COL_COUNT, TYPE_LIST);
//                    prf.setInt(Const.KEY_CHANNEL_COLUMNS, 1);
//                    onResume();
//                    startActivity(new Intent(context, MainActivity.class));
//                    finish();
//                    break;
//            }
//            this.dialog.dismiss();
//        });
//        dialog = builder.create();
//        dialog.show();
//    }

    //-- ALPHABET
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

//    @Override
//    public void onBackPressed() {
////        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
////            binding.drawerLayout.closeDrawer(GravityCompat.START);
////        } else {
////            AlertDialog.Builder builder = new AlertDialog.Builder(this);
////            builder.setMessage("Are you sure you want to exit?")
////                    .setTitle("Exit")
////                    .setIcon(R.drawable.logo)
////                    .setCancelable(false)
////                    .setPositiveButton("Yes", (dialog, id) -> MainActivity.super.onBackPressed())
////                    .setNegativeButton("No", (dialog, id) -> dialog.cancel());
////            AlertDialog alert = builder.create();
////            alert.show();
////        }
//
//        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            binding.drawerLayout.closeDrawer(GravityCompat.START);
//        } else if (binding.viewPager.getCurrentItem() != 0) {
//            binding.viewPager.setCurrentItem(0);
//        } else {
//            //Pressed back => return to home screen
//            int count = getSupportFragmentManager().getBackStackEntryCount();
//            if (getSupportActionBar() != null) {
//                getSupportActionBar().setHomeButtonEnabled(count > 0);
//            }
//            if (count > 0) {
//                FragmentManager fm = getSupportFragmentManager();
//                fm.popBackStack(fm.getBackStackEntryAt(0).getId(),
//                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
//            } else {//count == 0
//
//
////                Dialog
////                new AlertDialog.Builder(this)
////                        .setIcon(android.R.drawable.ic_dialog_alert)
////                        .setTitle("Leaving this App?")
////                        .setMessage("Are you sure you want to close this application?")
////                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
////                            @Override
////                            public void onClick(DialogInterface dialog, int which) {
////                                finish();
////                            }
////
////                        })
////                        .setNegativeButton("No", null)
////                        .show();
//                //super.onBackPressed();
//
//
//                if (doubleBackToExitPressedOnce) {
//                    super.onBackPressed();
//                    //moveTaskToBack(true);
//                    return;
//                }
//
//                this.doubleBackToExitPressedOnce = true;
//                backPressedToast();
//                new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 1500);
//            }
//        }
//    }

    private void backPressedToast() {
        //View view = findViewById(R.id.cLayout);
        //View view = findViewById(android.R.id.content);
        Snackbar.make(binding.coordinatorLayout, R.string.press_again_to_exit, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    @Override
    public void setBadgeText(String fragmentName, String msg) {
        int index = getFragmentIndex(fragmentName);
        if (index != -1) {
            binding.tabLayout.setBadgeText(index, msg);
        }
    }

    private int getFragmentIndex(String fragmentName) {
        for (int i = 0; i < fragmentList.size(); i++) {
            Fragment fragment = fragmentList.get(i);
            if (fragment.getClass().getSimpleName().equals(fragmentName)) {
                return i;
            }
        }
        return -1; // Фрагмент не найден
    }
}