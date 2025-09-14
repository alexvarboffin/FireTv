package tv.hdonlinetv.besttvchannels.movies.watchfree.fragment;


import static tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant.INTERSTITIAL_POST_LIST;
import static tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager.TYPE_GRID;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.GridLayoutManager;

import com.walhalla.data.repository.AllChannelPresenter;

import tv.hdonlinetv.besttvchannels.movies.watchfree.Const;
import tv.hdonlinetv.besttvchannels.movies.watchfree.R;

import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.DetailsActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.player.PlrActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.ChannelAdapter;

import com.walhalla.data.model.Channel;
import com.walhalla.ui.DLog;

import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentHomeBinding;

import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.xtream.CaseChannelListFragment;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;

import java.util.List;

public class AllChannelFragment extends CaseChannelListFragment {


    private final String THISCLAZZNAME = getClass().getSimpleName();

    private FragmentHomeBinding binding;
    //DatabaseReference wallpaperReference;

    PrefManager prefManager;
    AdNetwork adNetwork;
    AdsPref adsPref;


    private AlertDialog dialog;

    private GridLayoutManager lm;
    private AllChannelPresenter presenter;


    public AllChannelFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Handler handler = new Handler(Looper.getMainLooper());
        presenter = new AllChannelPresenter(handler, getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        adsPref = new AdsPref(getActivity());
        adNetwork = new AdNetwork(getActivity());
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_LIST);

        prefManager = new PrefManager(getActivity());
        showRefresh(true);
        //@@@@@@@@@@@binding.swipeRefreshLayout.setOnRefreshListener(() -> refreshData());

        loadPictures();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Register the MenuProvider
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.fragment_latest_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.actionDisplayChannels0) {
                    createAlertDialog(getContext(), prefManager);
                    return true;
                }
//                else if (id == R.id.action_settings) {
//                    // Handle settings action
//                    Toast.makeText(getContext(), "Settings clicked", Toast.LENGTH_SHORT).show();
//                    return true;
//                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    public void createAlertDialog(Context context, PrefManager prf) {
        String[] values = getResources().getStringArray(R.array.layout_options);


        int checkeditem = prf.getChannelDisplayItemTypeIndex();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_title_display_channels);
        builder.setSingleChoiceItems(values, checkeditem, (dialog, item) -> {

            String type = "";

            switch (item) {
                case 0:
                    int m = 3;
                    type = TYPE_GRID;
                    prf.setString(Const.KEY_COL_COUNT, type);
                    prf.setInt(Const.KEY_CHANNEL_COLUMNS, m);
                    resumeAdapter(m, type);
                    break;
                case 1:
                    int m0 = 1;
                    type = PrefManager.TYPE_LIST;
                    prf.setString(Const.KEY_COL_COUNT, type);
                    prf.setInt(Const.KEY_CHANNEL_COLUMNS, m0);
                    resumeAdapter(m0, type);
                    break;
            }
            this.dialog.dismiss();
        });
        dialog = builder.create();
        dialog.show();
    }

    private void resumeAdapter(int m, String type) {
        DLog.d("@@@" + type + "@@@" + m);
        List<Channel> tmp = channelAdapter.getItems();
        binding.recyclerView.setAdapter(null);
        binding.recyclerView.setLayoutManager(null);
        channelAdapter = null;

        channelAdapter = new ChannelAdapter(getContext(), tmp);
        lm = new GridLayoutManager(getContext(), m);
        binding.recyclerView.setLayoutManager(lm);
        binding.recyclerView.setAdapter(channelAdapter);
        channelAdapter.setOnItemClickListener(this);
        channelAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchWallpapers();
    }

    private void loadPictures() {

        //binding.recyclerView.setHasFixedSize(true);
        int m = prefManager.getInt(Const.KEY_CHANNEL_COLUMNS);
        lm = new GridLayoutManager(getContext(), m);
        binding.recyclerView.setLayoutManager(lm);
        binding.recyclerView.setAdapter(channelAdapter);

        //wallpaperReference = FirebaseDatabase.getInstance().getReference("Channels");
        //fetchWallpapers();

        channelAdapter.setOnItemClickListener(this);
    }

    public void showInterstitialAd() {
        adNetwork.showInterstitialAdNetwork(INTERSTITIAL_POST_LIST, adsPref.getInterstitialAdInterval());
    }

    private void fetchWallpapers() {
        int sortOption = prefManager.getSortOption();
        presenter.getAllChannels(sortOption, this);
    }


//    private void fetchWallpapers() {
//        wallpaperReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                showRefresh(false);
//                if (dataSnapshot.exists()) {
//                    for (DataSnapshot wallpaperSnapshot : dataSnapshot.getChildren()) {
//                        try {
//                            DLog.d("0000000000000000");
//                            String id = wallpaperSnapshot.child("id").getValue(String.class);
//                            String wallpaper = wallpaperSnapshot.child(Const.CH_IMG).getValue(String.class);
//                            String name = wallpaperSnapshot.child(Const.CH_NAME).getValue(String.class);
//                            String category = wallpaperSnapshot.child(Const.CH_CAT).getValue(String.class);
//                            String type = wallpaperSnapshot.child(Const.CH_TYPE).getValue(String.class);
//                            String link = wallpaperSnapshot.child(Const.CH_LNK).getValue(String.class);
//                            String desc = wallpaperSnapshot.child(Const.CH_DESC).getValue(String.class);
//                            String language = wallpaperSnapshot.child(Const.CH_LANG).getValue(String.class);
//
//                            Channel wallpaper1 = new Channel(id, wallpaper, name, category, type, link, desc, language);
//                            wallpaperList.add(0, wallpaper1);
//                        } catch (Exception e) {
//                            DLog.handleException(e);
//                        }
//                    }
//                    channelAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//            }
//        });
//    }

//    private void refreshData() {
//        wallpaperList.clear();
//        channelAdapter.notifyDataSetChanged();
//        new Handler().postDelayed(this::loadPictures, 2000);
//    }

    private void showRefresh(boolean show) {
//        binding.swipeRefreshLayout.setRefreshing(false);

//        if (show) {
//            binding.swipeRefreshLayout.setRefreshing(true);
//        } else {
//            new Handler().postDelayed(() -> {
//                binding.swipeRefreshLayout.setRefreshing(false);
//            }, 500);
//        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void successResult(List<Channel> data) {
        showRefresh(false);
        AllChannelFragment.this.setBadgeText(THISCLAZZNAME, String.valueOf(data.size()));
        channelAdapter.swapData(data);
    }

    @Override
    public void errorResult(String err) {

    }

    @Override
    public void onItemClick(View view, Channel channel, int position) {
        boolean isDetailsMode = prefManager.isDetailsMode();
        if (isDetailsMode) {
            long channelId = channel._id;
            Intent intent = DetailsActivity.newInstance(getContext(), channelId);
            startActivity(intent);
            showInterstitialAd();
        } else {
            Intent intents = PlrActivity.playerIntent(getContext(), channel);
            startActivity(intents);
        }
    }
}