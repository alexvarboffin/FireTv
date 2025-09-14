package tv.hdonlinetv.besttvchannels.movies.watchfree.tutorial;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> new TutorialFragment();
            case 1 -> new FAQsFragment();
            default -> new TutorialFragment();
        };
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}