package tv.hdonlinetv.besttvchannels.movies.watchfree.activity.playlist;

import static tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant.INTERSTITIAL_POST_LIST;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import tv.hdonlinetv.besttvchannels.movies.watchfree.Const;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.ChannelActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.CategoryAdapter;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentHomeBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.BaseFragment;
import tv.hdonlinetv.besttvchannels.movies.watchfree.model.CategoryUI;
import com.walhalla.data.repository.AllChannelInPlaylistPresenter;
import com.walhalla.data.repository.RepoCallback;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;

public class CategoriesForPlaylistFragment extends BaseFragment implements RepoCallback<List<CategoryUI>> {
    private final String THISCLAZZNAME = getClass().getSimpleName();
    private PrefManager prefManager;
    private FragmentHomeBinding binding;  // Replace with your actual binding class
    //DatabaseReference categoryReference;

    CategoryAdapter categoriesAdapter;
    AdNetwork adNetwork;
    AdsPref adsPref;

    private AllChannelInPlaylistPresenter presenter;


    public CategoriesForPlaylistFragment() {

    }

    private static final String ARG_PLAYLIST_ID = "ARG_PLAYLIST_ID";
    private long playlistId;

    public static CategoriesForPlaylistFragment newInstance(long playlistId) {
        CategoriesForPlaylistFragment fragment = new CategoriesForPlaylistFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PLAYLIST_ID, playlistId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Handler handler = new Handler(Looper.getMainLooper());
        presenter = new AllChannelInPlaylistPresenter(handler, getContext());
        if (getArguments() != null) {
            playlistId = getArguments().getLong(ARG_PLAYLIST_ID, -1);
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

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        // Register the MenuProvider
//        requireActivity().addMenuProvider(new MenuProvider() {
//            @Override
//            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
//                menuInflater.inflate(R.menu.fragment_category_menu, menu);
//            }
//
//            @Override
//            public boolean onMenuItemSelected(@NonNull MenuItem item) {
//                int id = item.getItemId();
//                if (id == R.id.actionDisplayChannels0) {
//                    createAlertDialog(getContext(), prefManager);
//                    return true;
//                }
////                else if (id == R.id.action_settings) {
////                    // Handle settings action
////                    Toast.makeText(getContext(), "Settings clicked", Toast.LENGTH_SHORT).show();
////                    return true;
////                }
//                return false;
//            }
//        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
//    }

    private void createAlertDialog(Context context, PrefManager prefManager) {
        Toast.makeText(context, "99", Toast.LENGTH_SHORT).show();
    }

    private void loadCategory() {

        //fetchCategory();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchCategory();
    }

    public void showInterstitialAd() {
        adNetwork.showInterstitialAdNetwork(INTERSTITIAL_POST_LIST, adsPref.getInterstitialAdInterval());
    }

    private void fetchCategory() {
        //int sortOption = prefManager.getSortOption();
        //List<Channel> mm = repo.getChannelsInPlaylist(playlistId, sortOption);
        presenter.getCategoriesForPlaylist(playlistId, this);
    }

//    private void fetchCategory() {
//        categoryReference = FirebaseDatabase.getInstance().getReference("categories");
//        categoryReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                showRefresh(false);
//
//                DLog.d(String.valueOf(dataSnapshot));
//
//                if (dataSnapshot.exists()) {
//                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                        String name = snapshot.getKey();
//                        String desc = snapshot.child("desc").getValue(String.class);
//                        String thumb = snapshot.child("thumbnail").getValue(String.class);
//                        CategoryUI category = new CategoryUI(name, desc, thumb);
//                        categoryList.add(0, category);
//                    }
//                    categoriesAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                if (BuildConfig.DEBUG) {
//                    Toast.makeText(getContext(), "" + databaseError.getMessage(),
//                            Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }

//    private void refreshData() {
//        categoriesAdapter.notifyDataSetChanged();
//        new Handler().postDelayed(this::loadCategory, 2000);
//    }

//    private void showRefresh(boolean show) {
//        if (show) {
//            binding.swipeRefreshLayout.setRefreshing(true);
//        } else {
//            new Handler().postDelayed(() -> {
//                binding.swipeRefreshLayout.setRefreshing(false)
//            }, 500);
//        }
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void successResult(List<CategoryUI> data) {
        CategoriesForPlaylistFragment.this.setBadgeText(THISCLAZZNAME, String.valueOf(data.size()));
        categoriesAdapter.swapData(data);
    }

    @Override
    public void errorResult(String err) {

    }
}
