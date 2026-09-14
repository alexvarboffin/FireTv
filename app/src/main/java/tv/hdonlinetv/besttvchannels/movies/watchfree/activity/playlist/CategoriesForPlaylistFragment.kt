package tv.hdonlinetv.besttvchannels.movies.watchfree.activity.playlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.walhalla.data.repository.AllChannelInPlaylistPresenter
import com.walhalla.data.repository.RepoCallback
import tv.hdonlinetv.besttvchannels.movies.watchfree.Const
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.ChannelActivity
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.CategoryAdapter
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.OnCategoryItemClickListener
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentHomeBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.BaseFragment
import tv.hdonlinetv.besttvchannels.movies.watchfree.model.CategoryUI
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager

class CategoriesForPlaylistFragment : BaseFragment(), RepoCallback<List<CategoryUI>> {
    private val THISCLAZZNAME: String = javaClass.getSimpleName()
    private var prefManager: PrefManager? = null
    private var binding: FragmentHomeBinding? = null // Replace with your actual binding class

    //DatabaseReference categoryReference;
    var categoriesAdapter: CategoryAdapter? = null
    var adNetwork: AdNetwork? = null
    var adsPref: AdsPref? = null

    private var presenter: AllChannelInPlaylistPresenter? = null


    private var playlistId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val handler = Handler(Looper.getMainLooper())
        presenter = AllChannelInPlaylistPresenter(handler, requireContext())
        if (getArguments() != null) {
            playlistId = requireArguments().getLong(ARG_PLAYLIST_ID, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize ViewBinding
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view: View = binding!!.getRoot()
        prefManager = PrefManager(getActivity())
        adsPref = AdsPref(getActivity())
        adNetwork = AdNetwork(requireActivity())
        adNetwork!!.loadInterstitialAdNetwork(Constant.INTERSTITIAL_POST_LIST)

        //        showRefresh(true);
//        binding.swipeRefreshLayout.setOnRefreshListener(() -> refreshData());
        binding!!.recyclerView.setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(getContext(), 2)
        binding!!.recyclerView.setLayoutManager(gridLayoutManager)

        categoriesAdapter = CategoryAdapter(requireContext(), ArrayList<CategoryUI>())
        binding!!.recyclerView.setAdapter(categoriesAdapter)
        categoriesAdapter!!.setOnItemClickListener(OnCategoryItemClickListener { view0: View?, obj: CategoryUI?, position: Int ->
            val intent = Intent(context, ChannelActivity::class.java)
            intent.putExtra(Const.KEY_CATEGOTY_NAME, obj!!.getName())
            startActivity(intent)
            showInterstitialAd()
        })
        loadCategory()
        return view
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
    /*                else if (id == R.id.action_settings)
    {
        * /                    // Handle settings action
        * /                    Toast.makeText(getContext(), "Settings clicked", Toast.LENGTH_SHORT).show();
        * /                    return true;
        * /
    } */
    //                return false;
    //            }
    //        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    //    }
    private fun createAlertDialog(context: Context?, prefManager: PrefManager?) {
        Toast.makeText(context, "99", Toast.LENGTH_SHORT).show()
    }

    private fun loadCategory() {
        //fetchCategory();
    }

    public override fun onResume() {
        super.onResume()
        fetchCategory()
    }

    fun showInterstitialAd() {
        adNetwork!!.showInterstitialAdNetwork(
            Constant.INTERSTITIAL_POST_LIST,
            adsPref!!.getInterstitialAdInterval()
        )
    }

    private fun fetchCategory() {
        //int sortOption = prefManager.getSortOption();
        //List<Channel> mm = repo.getChannelsInPlaylist(playlistId, sortOption);
        presenter!!.getCategoriesForPlaylist(playlistId, this)
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
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun successResult(data: List<CategoryUI>) {
        this@CategoriesForPlaylistFragment.setBadgeText(THISCLAZZNAME, data.size.toString())
        categoriesAdapter!!.swapData(data)
        binding?.lytNoItem?.root?.visibility =
            if (data.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun errorResult(err: String) {
    }

    companion object {
        private const val ARG_PLAYLIST_ID = "ARG_PLAYLIST_ID"
        fun newInstance(playlistId: Long): CategoriesForPlaylistFragment {
            val fragment = CategoriesForPlaylistFragment()
            val args = Bundle()
            args.putLong(ARG_PLAYLIST_ID, playlistId)
            fragment.setArguments(args)
            return fragment
        }
    }
}
