package tv.hdonlinetv.besttvchannels.movies.watchfree.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;


import java.util.List;


public class ViewPagerAdapter extends FragmentStateAdapter {

    Context context;
    private final List<Fragment> fragmentList;

    public ViewPagerAdapter(FragmentActivity context, FragmentManager fm, List<Fragment> fragments) {
        super(context);
        this.context = context;
        this.fragmentList = fragments;
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
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


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    public Fragment getItem(int i) {
        return fragmentList.get(i);
    }

//    @Override
//    public int getCount() {
//        return totalTabs;
//    }
}
