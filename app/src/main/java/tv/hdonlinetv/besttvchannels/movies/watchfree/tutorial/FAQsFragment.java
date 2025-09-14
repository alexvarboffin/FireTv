package tv.hdonlinetv.besttvchannels.movies.watchfree.tutorial;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentFaqsBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.LoadingDialogFragment;

import com.walhalla.data.repository.PlaylistManagementPresenterImpl;

import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.PlaylistManagementView;


public class FAQsFragment extends Fragment implements FAQAdapter.MyCallback, PlaylistManagementView {

    private static final boolean ADEBUG = false;

//    https://iptv-org.github.io/iptv/index.country.m3u
//    https://iptv-org.github.io/iptv/index.language.m3u
//    https://iptv-org.github.io/iptv/index.category.m3u


    private FragmentFaqsBinding binding;
    private String playlist_country_url = "https://iptv-org.github.io/iptv/index.country.m3u";
    private String playlist_uk_url = "https://iptv-org.github.io/iptv/countries/uk.m3u";
    private String playlist_category_url = "https://iptv-org.github.io/iptv/index.m3u";
    private String playlist_free_tv = "https://raw.githubusercontent.com/Free-TV/IPTV/master/playlist.m3u8";

//    https://iptv-org.github.io/api/channels.json
//    https://iptv-org.github.io/api/guides.json
//    https://iptv-org.github.io/api/categories.json ..
//    https://iptv-org.github.io/api/languages.json
//    https://iptv-org.github.io/api/countries.json ..
//    https://iptv-org.github.io/api/subdivisions.json

    private String json_playlist_country_url = "https://iptv-org.github.io/iptv/index.country.m3u";
    private String json_playlist_uk_url = "https://iptv-org.github.io/iptv/countries/uk.m3u";
    private String json_playlist_category_url = "https://iptv-org.github.io/iptv/index.m3u";
    private PlaylistManagementPresenterImpl presenter;
    private LoadingDialogFragment loadingDialog;


    public FAQsFragment() {
        // Required empty public constructor
    }

    String[] mm = new String[]{
            "file:///android_asset/00.html",
//            "file:///android_asset/00.html",
            "file:///android_asset/02.html",
            "file:///android_asset/00.html"

    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFaqsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        Handler handler = new Handler(Looper.getMainLooper());
        presenter = new PlaylistManagementPresenterImpl(handler, this, getContext());
        List<FAQItem> faqItems = new ArrayList<>();
        faqItems.add(new HeaderItem(getString(R.string.frequentlyAskedQuestions)));
        String[] faqQuestions = getResources().getStringArray(R.array.faq_questions);
        int j = 0;
        for (String faqQuestion : faqQuestions) {
            faqItems.add(new FAQQuestion(faqQuestion, mm[j]));
            ++j;
        }

        faqItems.add(new HeaderItem(getString(R.string.sample_playlist_url)));


//        Context context = getContext(); // или getApplicationContext()
//        List<String> lines = AssetHelper.readFilesFromAssets(context, "demo");
//        for (String line : lines) {
//            faqItems.add(new PlaylistItem(
//                    "xxx", line, line
//            ));
//        }


        String zz = playlist_uk_url.replace(".io/iptv/countries/", "../") + " ~49.6Kb";
        String mm = playlist_country_url.replace(".io/iptv/index.", "../") + " ~3.8Mb";
        String cat = playlist_category_url.replace(".io/iptv/index.", "../") + " ~2.6mb";

        faqItems.add(new PlaylistItem(
                getString(R.string.playlist_uk), zz, playlist_uk_url
        ));
        faqItems.add(new PlaylistItem(
                getString(R.string.playlist_country), mm, playlist_country_url
        ));
        faqItems.add(new PlaylistItem(
                getString(R.string.playlist_category), cat, playlist_category_url
        ));
        faqItems.add(new PlaylistItem(
                "Free-TV"
                , playlist_free_tv.replace("hubusercontent.com/Free-TV/IPTV/master", ".../Free-TV/IPTV")
                , playlist_free_tv
        ));

        FAQAdapter adapter = new FAQAdapter(getContext(), faqItems, this);
        binding.recyclerViewFAQs.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewFAQs.setAdapter(adapter);


        return view;
    }

//    https://iptv-org.github.io/iptv/countries/uk.m3u
//    https://iptv-org.github.io/iptv/index.country.m3u
//    https://iptv-org.github.io/iptv/index.m3u
    //https://github.com/iptv-org/


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClisk(String playlistLink) {
        if (ADEBUG) {
            String playlistName = "";
            presenter.onSubscribeUriClick(playlistName, playlistLink);
        } else {
            openLink(getContext(), playlistLink);
        }
    }

    private void openLink(Context context, String url) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied URL", url);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Playlist URL Copied to Clipboard", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Unable to copy to clipboard", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void showPlaylistName(String name) {

    }

    @Override
    public void showValidationError(Integer playlistName, Integer playlistUrl, Integer userName, Integer password) {

    }

    @Override
    public void showError0(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showToast(int resId) {
        Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updatePlaylistContent(String content) {

    }

    @Override
    public void showErrorToast(int clipboardContainsNoText) {

    }

    @Override
    public void onPlaylistUpdated() {

    }

    @Override
    public void showProgressBar() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialogFragment();
            loadingDialog.setCancelable(false);
        }
        loadingDialog.show(getParentFragmentManager(), "loading_dialog");
    }

    @Override
    public void hideProgressBar() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }
}