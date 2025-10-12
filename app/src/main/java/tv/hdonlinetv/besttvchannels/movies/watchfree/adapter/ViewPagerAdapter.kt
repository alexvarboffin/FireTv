package tv.hdonlinetv.besttvchannels.movies.watchfree.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(context: FragmentActivity, fm: FragmentManager?, fragments: List<Fragment>
) : FragmentStateAdapter(context) {
    var context: Context?
    private val fragmentList: List<Fragment>

    init {
        this.context = context
        this.fragmentList = fragments
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }


    //    @NonNull
    //    @Override
    //    public Fragment getItem(int position) {
    //        if (position == 0) {
    //            return new ALatestFragment();
    //        } else if (position == 1) {
    //            return new CategoryFragment();
    //        } else if (position == 2) {
    //            return new FavoritesFragment();
    //        }
    //        return new Fragment();
    //    }
    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    fun getItem(i: Int): Fragment? {
        return fragmentList[i]
    }
    //    @Override
    //    public int getCount() {
    //        return totalTabs;
    //    }
}
