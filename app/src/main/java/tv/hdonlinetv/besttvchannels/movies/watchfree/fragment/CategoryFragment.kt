package tv.hdonlinetv.besttvchannels.movies.watchfree.fragment

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
import com.walhalla.data.model.Category
import com.walhalla.data.repository.CategoryPresenter
import com.walhalla.data.repository.RepoCallback
import tv.hdonlinetv.besttvchannels.movies.watchfree.Const
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.ChannelActivity
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.CategoryAdapter
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentHomeBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.model.CategoryUI
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager

class CategoryFragment : BaseFragment(), RepoCallback<List<Category>> {
    private val THISCLAZZNAME: String = javaClass.simpleName
    private var prefManager: PrefManager? = null
    private var binding: FragmentHomeBinding? = null // Replace with your actual binding class

    //DatabaseReference categoryReference;
    var categoriesAdapter: CategoryAdapter? = null
    var adNetwork: AdNetwork? = null
    var adsPref: AdsPref? = null
    private var presenter: CategoryPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val handler = Handler(Looper.getMainLooper())
        presenter = CategoryPresenter(handler, requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize ViewBinding
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view: View = binding!!.getRoot()
        prefManager = PrefManager(activity)
        adsPref = AdsPref(activity)
        adNetwork = AdNetwork(requireActivity())
        adNetwork!!.loadInterstitialAdNetwork(Constant.INTERSTITIAL_POST_LIST)

        //        showRefresh(true);
//        binding.swipeRefreshLayout.setOnRefreshListener(() -> refreshData());
        binding!!.recyclerView.setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(context, 2)
        binding!!.recyclerView.setLayoutManager(gridLayoutManager)

        categoriesAdapter = CategoryAdapter(requireContext(), ArrayList())
        binding!!.recyclerView.setAdapter(categoriesAdapter)
        categoriesAdapter!!.setOnItemClickListener(tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.OnCategoryItemClickListener { view0: View, obj: CategoryUI, position: Int ->
            val intent = Intent(context, ChannelActivity::class.java)
            intent.putExtra(Const.KEY_CATEGOTY_NAME, obj.getName())
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
    /*               else if (id == R.id.action_settings)
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
        presenter!!.getAllCategories(this)
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
    //        categoryList.clear();
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

    /**
     * 
     * tmp0.add(0, categoryUI); - Invert list
     */
    override fun successResult(data: List<Category>) {
        val tmp0: MutableList<CategoryUI> = ArrayList()
        for (category in data) {
            val categoryUI = CategoryUI(
                category.name, category.desc, category.thumb
            )
            tmp0.add(categoryUI)
        }
        this@CategoryFragment.setBadgeText(THISCLAZZNAME, tmp0.size.toString())
        categoriesAdapter!!.swapData(tmp0)
        binding?.lytNoItem?.root?.visibility =
            if (tmp0.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun errorResult(err: String) {
    }
}
