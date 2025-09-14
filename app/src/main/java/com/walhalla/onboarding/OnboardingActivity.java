package com.walhalla.onboarding;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;
import com.walhalla.terms.PrivacyPolicyActivity;
import com.walhalla.terms.TermsFragment;

import java.util.ArrayList;
import java.util.List;

import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.MainActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityOnboardBinding;


public class OnboardingActivity extends AppCompatActivity
        implements FragmentFirebasePolicy.IPrivacyPolicy, TermsFragment.ITerms {

    private int currentPage = 0;
    private ActivityOnboardBinding binding;
    private OnboardAdapter onboardAdapter;
    private OnboardingManager onboardingManager;

    private boolean areTermsAccepted;
    private Boolean isFirebaseAccepted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onboardingManager = new OnboardingManager(this);
        if (onboardingManager.isOnboarding()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        binding = ActivityOnboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.viewPager.setUserInputEnabled(false);
        onboardAdapter = new OnboardAdapter(this);
        binding.viewPager.setAdapter(onboardAdapter);

        binding.btnNext.setOnClickListener(v -> handleNextButtonClick());
        binding.flexibleIndicator.initViewPager(binding.viewPager);
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                updateNextButtonText(currentPage);
            }
        });
    }

    private void handleNextButtonClick() {
        if (currentPage == 0) {
            binding.viewPager.setCurrentItem(currentPage + 1);
        } else if (currentPage == 1) {
            if (areTermsAccepted) {
                binding.viewPager.setCurrentItem(currentPage + 1);
            } else {
                Fragment m = onboardAdapter.getItem(currentPage);
                if (m instanceof TermsFragment) {
                    TermsFragment n = (TermsFragment) m;
                    n.showError();
                }
                showSnackbar(R.string.error_msg_terms);
            }
        } else if (currentPage == 2) {
            if (isFirebaseAccepted != null) {
                binding.viewPager.setCurrentItem(currentPage + 1);
            } else {
                showSnackbar(R.string.error_msg_firebase);
            }
        } else if (currentPage == 3) {
            onboardingManager.isOnboarding(true);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void updateNextButtonText(int currentPage) {
        if (currentPage == 3) {
            binding.btnNext.setText(R.string.onboard_action_finish);
        } else {
            binding.btnNext.setText(R.string.onboard_action_next);
        }
    }

    private void showSnackbar(int messageResId) {
        Snackbar.make(binding.coordinator, messageResId, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(this, R.color.snackbar_background))
                .setAction(android.R.string.ok, null)
                .show();
    }

    @Override
    public void launchPrivacyPolicy() {
        startActivity(new Intent(this, PrivacyPolicyActivity.class));
    }

    @Override
    public void isFirebaseAccepted(boolean accepted) {
        isFirebaseAccepted = accepted;
    }

    @Override
    public void isTermsAccepted(boolean accepted) {
        areTermsAccepted = accepted;
    }

    public static class OnboardAdapter extends FragmentStateAdapter {

        private final List<Fragment> fragmentList;

        public OnboardAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
            fragmentList = new ArrayList<>();
            fragmentList.add(Info1Fragment.newInstance());
            fragmentList.add(new TermsFragment());
            fragmentList.add(FragmentFirebasePolicy.newInstance());
            fragmentList.add(Info3Fragment.newInstance());
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getItemCount() {
            return fragmentList.size();
        }

        public Fragment getItem(int currentPage) {
            return fragmentList.get(currentPage);
        }
    }

}
