package tv.hdonlinetv.besttvchannels.movies.watchfree.activity.playlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.rahimlis.badgedtablayout.BadgedTabLayout
import com.walhalla.data.model.PlaylistImpl
import com.walhalla.ui.DLog.d
import com.walhalla.ui.plugins.DialogAbout.aboutDialog
import com.walhalla.ui.plugins.Launcher.openBrowser
import com.walhalla.ui.plugins.Launcher.rateUs
import com.walhalla.ui.plugins.Module_U.feedback
import com.walhalla.ui.plugins.Module_U.shareThisApp
import tv.hdonlinetv.besttvchannels.movies.watchfree.Const
import tv.hdonlinetv.besttvchannels.movies.watchfree.R
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.BaseActivity
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.FavoriteActivity
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.SearchActivity
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.SettingsActivity
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.ViewPagerAdapter
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityPlaylistBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.FavoritesFragment
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.IOnFragmentInteractionListener
import tv.hdonlinetv.besttvchannels.movies.watchfree.tutorial.TutorialActivity
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.GDPR
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager

class PlaylistActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    IOnFragmentInteractionListener {
    private var binding: ActivityPlaylistBinding? = null

    //private LocalDatabaseRepo favoriteDatabase;
    var actionBarDrawerToggle: ActionBarDrawerToggle? = null


    private var prf: PrefManager? = null

    private val doubleBackToExitPressedOnce = false
    private var prefManager: PrefManager? = null
    private var dialog: AlertDialog? = null
    private var tabAdapter: ViewPagerAdapter? = null
    private var tabTitles: Array<String> = emptyArray()

    private var playlist: PlaylistImpl? = null
    private var fragmentList: MutableList<Fragment> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        prefManager = PrefManager(this)
        setSupportActionBar(binding!!.toolbar)
        handleIntent0(intent)

        //        LocalDatabaseRepo repo = LocalDatabaseRepo.getStoreInfoDatabase(this);
//        List<Channel> allChannels = repo.getChannelsInPlaylist(playlist._id);
//        List<Channel> favoriteChannels = new ArrayList<>();
//        for (Channel channel : allChannels) {
//            if (channel.isFavorite()) {
//                favoriteChannels.add(channel);
//            }
//        }
        prf = PrefManager(this)
        wads()


        drawer()

        //main Fragment
        makeTabs(binding!!.tabLayout)


        binding!!.tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val playlistId = playlist!!._id
        d("{}$playlistId")

        var0(playlistId)

        binding!!.viewPager.adapter = tabAdapter

        //binding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tabLayout));
        TabLayoutMediator(
            binding!!.tabLayout, binding!!.viewPager
        ) { tab: TabLayout.Tab, position: Int -> tab.setText(tabTitles[position]) }.attach()

        binding!!.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding!!.viewPager.currentItem = tab.position
                invalidateFragmentMenus(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })


        //binding.viewPager.setOffscreenPageLimit(1);
        binding!!.searchView.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.d("SimpleSearchView", "Submit:$query")
                val intent = Intent(
                    this@PlaylistActivity,
                    SearchActivity::class.java
                )
                intent.putExtra(Const.KEY_CATEGOTY_NAME, query)
                startActivity(intent)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Log.d("SimpleSearchView", "Text changed:$newText")
                return false
            }

            override fun onQueryTextCleared(): Boolean {
                Log.d("SimpleSearchView", "Text cleared")
                return false
            }
        })

        //        binding.addFab.setVisibility(View.GONE);
//        binding.addFab.setOnClickListener(v -> {
//            startActivity(new Intent(this, PlaylistManagementActivity.class));
//        });
    }

    private fun drawer() {
//        binding.navView.setNavigationItemSelectedListener(this);
//        actionBarDrawerToggle = new ActionBarDrawerToggle(this,
//                binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle);
//        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
//        actionBarDrawerToggle.syncState();
//
//        binding.toolbar.setNavigationIcon(R.drawable.ic_action_action);
    }

    private fun var0(id: Long) {
        tabTitles = resources.getStringArray(R.array.tab_titles_playlist)

        fragmentList = ArrayList()
        fragmentList.add(AllChannelInPlaylistFragment.newInstance(id))
        fragmentList.add(CategoriesForPlaylistFragment.newInstance(id))
        fragmentList.add(FavoritesFragment.newInstance(id))
        //fragmentList.add(new PlaylistFragment());
        tabAdapter = ViewPagerAdapter(
            this,
            supportFragmentManager, fragmentList
        )
    }

    private fun handleIntent0(intent: Intent?) {
        if (intent != null) {
            val name = intent.getStringExtra(EXTRA_NAME)
            val fileName = intent.getStringExtra(EXTRA_FILE_NAME)
            val importDate = intent.getLongExtra(EXTRA_IMPORT_DATE, 0)
            val channelCount = intent.getIntExtra(EXTRA_CHANNEL_COUNT, 0)
            val autoUpdate = intent.getBooleanExtra(EXTRA_AUTO_UPDATE, false)

            playlist = PlaylistImpl(name!!, fileName, importDate, channelCount, autoUpdate, -1)
            playlist!!._id = intent.getLongExtra(EXTRA_ID, -1)
        }
    }

    private fun invalidateFragmentMenus(position: Int) {
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
        invalidateOptionsMenu() //or respectively its support method.
    }

    private fun makeTabs(tabLayout: BadgedTabLayout) {
//        tabLayout.addTab(tabLayout.newTab().setText("Channel"));
//        tabLayout.addTab(tabLayout.newTab().setText("Category"));
//        tabLayout.addTab(tabLayout.newTab().setText("Favorites"));
    }

    private fun wads() {
        val adsPref = AdsPref(this)

        if (adsPref.adStatus == Constant.AD_STATUS_ON) {
            when (adsPref.adType) {
                Constant.ADMOB -> {
                    MobileAds.initialize(
                        this
                    ) { initializationStatus: InitializationStatus ->
                        val statusMap =
                            initializationStatus.adapterStatusMap
                        for (adapterClass in statusMap.keys) {
                            val status =
                                checkNotNull(statusMap[adapterClass])
                            Log.d(
                                "MyApp",
                                String.format(
                                    "Adapter name: %s, Description: %s, Latency: %d",
                                    adapterClass,
                                    status.description,
                                    status.latency
                                )
                            )
                            Log.d(
                                "Open Bidding",
                                "FAN open bidding with AdMob as mediation partner selected"
                            )
                        }
                    }
                    GDPR().init(this)
                }
            }
        }

        val adNetwork = AdNetwork(this)
        adNetwork.loadBannerAdNetwork(Constant.BANNER_HOME)
        adNetwork.loadInterstitialAdNetwork(Constant.INTERSTITIAL_POST_LIST)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        //@@@ binding.drawerLayout.closeDrawer(GravityCompat.START);
//        if (menuItem.getItemId() == R.id.nav_unlock_premium) {
//            startActivity(new Intent(MainActivity.this,PrimeActivity.class));
//        } else
        if (menuItem.itemId == R.id.nav_home) {
        } else if (menuItem.itemId == R.id.nav_profile) {
            startActivity(Intent(this, FavoriteActivity::class.java))
        } else if (menuItem.itemId == R.id.nav_setting) {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        } else if (menuItem.itemId == R.id.nav_about) {
            aboutDialog(this)
        } else if (menuItem.itemId == R.id.nav_share) {
            shareThisApp(this)
        } else if (menuItem.itemId == R.id.nav_rate) {
            rateUs(this)
        } else if (menuItem.itemId == R.id.nav_feedback) {
            feedback(this)
        } else if (menuItem.itemId == R.id.action_privacy_policy) {
            openBrowser(this, getString(R.string.url_privacy_policy))
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        initCheck()
    }

    private fun initCheck() {
        if (prf!!.loadNightModeState()) {
            Log.d("Dark", "MODE")
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // set status text dark
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
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)

        val item = menu.findItem(R.id.action_search)
        binding!!.searchView.setMenuItem(item)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle arrow click here
        if (item.itemId == android.R.id.home) {
            finish() // close this activity and return to preview activity (if there is any)
        } else if (item.itemId == R.id.actionInfo) {
            startActivity(Intent(this, TutorialActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
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
    override fun onDestroy() {
        super.onDestroy()
        if (dialog != null) {
            dialog!!.dismiss()
            dialog = null
        }
    }

    //    @Override
    //    public void onBackPressed() {
    /*        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
    {
        * /            binding.drawerLayout.closeDrawer(GravityCompat.START);
        * /
    } else
    {
        * /            AlertDialog.Builder builder = new AlertDialog.Builder(this);
        * /            builder.setMessage("Are you sure you want to exit?")
        * /                    .setTitle("Exit")
        * /                    .setIcon(R.drawable.logo)
        * /                    .setCancelable(false)
        * /                    .setPositiveButton("Yes", (dialog, id) -> MainActivity.super.onBackPressed())
        * /                    .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        * /            AlertDialog alert = builder.create();
        * /            alert.show();
        * /
    } */
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
    /*                Dialog
    * /                new AlertDialog.Builder(this)
    * /                        .setIcon(android.R.drawable.ic_dialog_alert)
    * /                        .setTitle("Leaving this App?")
    * /                        .setMessage("Are you sure you want to close this application?")
    * /                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
    {
        * /                            @Override
        * /                            public void onClick(DialogInterface dialog, int which) {
        * /                                finish();
        * /
    }
        * /
        * /
    })
    * /                        .setNegativeButton("No", null)
    * /                        .show(); */
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
    private fun backPressedToast() {
        //View view = findViewById(R.id.cLayout);
        //View view = findViewById(android.R.id.content);
        Snackbar.make(
            binding!!.coordinatorLayout,
            R.string.press_again_to_exit,
            Snackbar.LENGTH_LONG
        ).setAction("Action", null).show()
    }

    override fun setBadgeText(fragmentName: String, msg: String) {
        val index = getFragmentIndex(fragmentName)
        if (index != -1) {
            binding!!.tabLayout.setBadgeText(index, msg)
        }
    }

    private fun getFragmentIndex(fragmentName: String): Int {
        for (i in fragmentList!!.indices) {
            val fragment = fragmentList!![i]
            if (fragment.javaClass.simpleName == fragmentName) {
                return i
            }
        }
        return -1 // Фрагмент не найден
    }

    companion object {
        private const val EXTRA_NAME = "EXTRA_NAME"
        private const val EXTRA_FILE_NAME = "EXTRA_FILE_NAME"
        private const val EXTRA_IMPORT_DATE = "EXTRA_IMPORT_DATE"
        private const val EXTRA_CHANNEL_COUNT = "EXTRA_CHANNEL_COUNT"
        private const val EXTRA_AUTO_UPDATE = "EXTRA_AUTO_UPDATE"
        private const val EXTRA_ID = "EXTRA_ID"


        private const val TAG = "@@@"
        @JvmStatic
        fun newInstance(context: Context?, playlist: PlaylistImpl): Intent {
            val intent = Intent(context, PlaylistActivity::class.java)
            intent.putExtra(EXTRA_ID, playlist._id)
            intent.putExtra(EXTRA_NAME, playlist.getTitle())
            intent.putExtra(EXTRA_FILE_NAME, playlist.fileName)
            intent.putExtra(EXTRA_IMPORT_DATE, playlist.importDate)
            intent.putExtra(EXTRA_CHANNEL_COUNT, playlist.count)
            intent.putExtra(EXTRA_AUTO_UPDATE, playlist.isAutoUpdate)
            return intent
        }
    }
}