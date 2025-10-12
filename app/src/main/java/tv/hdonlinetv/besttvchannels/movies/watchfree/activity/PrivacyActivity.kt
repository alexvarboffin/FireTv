package tv.hdonlinetv.besttvchannels.movies.watchfree.activity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import tv.hdonlinetv.besttvchannels.movies.watchfree.R
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityPrivacyPolicyBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager

class PrivacyActivity : BaseActivity() {
    private var binding: ActivityPrivacyPolicyBinding? = null
    private var prefManager: PrefManager? = null
    private val TAG: String = PrivacyActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())



        prefManager = PrefManager(this)
        binding!!.layoutLoadMore.visibility = View.VISIBLE
        getPrivacyPolicy(binding!!.webView)
        initCheck()
    }

    private fun getPrivacyPolicy(webView: WebView) {
        val htmlUrl = intent.getStringExtra(EXTRA_HTML_URL0)
        val title = intent.getStringExtra(EXTRA_TITLE)

        setSupportActionBar(binding!!.toolbar)
        setTitle(if (title == null) getString(R.string.policy_privacy) else title)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        // Enable JavaScript for the WebView
        webView.getSettings().setJavaScriptEnabled(true)
        if (htmlUrl != null) {
            webView.loadUrl(htmlUrl)
        }

        //        DatabaseReference policyReference = FirebaseDatabase.getInstance().getReference("policy");
//        policyReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    binding.layoutLoadMore.setVisibility(View.GONE);
//                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                        String policy = ds.child("desc").getValue(String.class);
//                        binding.textViewPolicy.setText(policy);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // Handle possible errors.
//            }
//        });
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initCheck() {
        if (prefManager!!.loadNightModeState()) {
            Log.d("Dark", "MODE")
        } else {
            window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
    }

    companion object {
        const val EXTRA_HTML_URL0: String = "html_url"
        const val EXTRA_TITLE: String = "title" // Add title key
    }
}
