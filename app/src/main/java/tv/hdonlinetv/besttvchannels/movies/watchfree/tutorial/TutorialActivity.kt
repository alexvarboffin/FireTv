package tv.hdonlinetv.besttvchannels.movies.watchfree.tutorial

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import tv.hdonlinetv.besttvchannels.movies.watchfree.R
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityFaqBinding

class TutorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Android 15+/16: edge-to-edge enforced for targetSdk 35+.
        // Use enableEdgeToEdge (not setDecorFitsSystemWindows). Docs:
        // https://developer.android.com/develop/ui/views/layout/edge-to-edge
        enableEdgeToEdge()

        val binding = ActivityFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.adapter = ViewPagerAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.setText(R.string.tab_tutorial)
                    tab.setIcon(R.drawable.ic_information)
                }
                1 -> {
                    tab.setText(R.string.tab_faqs)
                    tab.setIcon(R.drawable.ic_faq)
                }
            }
        }.attach()
    }
}
