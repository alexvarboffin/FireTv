package tv.hdonlinetv.besttvchannels.movies.watchfree.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityPrivacyPolicyBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;


public class PrivacyActivity extends BaseActivity {

    public static final String EXTRA_HTML_URL0 = "html_url";
    public static final String EXTRA_TITLE = "title";  // Add title key


    private ActivityPrivacyPolicyBinding binding;
    private PrefManager prefManager;
    private final String TAG = PrivacyActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        prefManager = new PrefManager(this);
        binding.layoutLoadMore.setVisibility(View.VISIBLE);
        getPrivacyPolicy(binding.webView);
        initCheck();
    }

    private void getPrivacyPolicy(WebView webView) {

        String htmlUrl = getIntent().getStringExtra(EXTRA_HTML_URL0);
        String title = getIntent().getStringExtra(EXTRA_TITLE);

        setSupportActionBar(binding.toolbar);
        setTitle((title == null) ? getString(R.string.policy_privacy) : title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Enable JavaScript for the WebView
        webView.getSettings().setJavaScriptEnabled(true);
        if (htmlUrl != null) {
            webView.loadUrl(htmlUrl);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initCheck() {
        if (prefManager.loadNightModeState()) {
            Log.d("Dark", "MODE");
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
}
