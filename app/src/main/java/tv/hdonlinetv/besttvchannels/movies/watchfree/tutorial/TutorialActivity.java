package tv.hdonlinetv.besttvchannels.movies.watchfree.tutorial;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayoutMediator;
import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityFaqBinding;

public class TutorialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityFaqBinding binding = ActivityFaqBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText(R.string.tab_tutorial);
                            tab.setIcon(R.drawable.ic_information);

                            break;
                        case 1:
                            tab.setText(R.string.tab_faqs);
                            tab.setIcon(R.drawable.ic_faq);

                            break;
                    }
                }).attach();
    }
}
