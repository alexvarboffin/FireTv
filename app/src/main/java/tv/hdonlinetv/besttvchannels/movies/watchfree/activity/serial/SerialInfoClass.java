package tv.hdonlinetv.besttvchannels.movies.watchfree.activity.serial;

import static tv.hdonlinetv.besttvchannels.movies.watchfree.utils.DataUtils.convertUnixToDate;

import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.m3u.data.parser.xtream.XtreamChannelInfo;
import com.walhalla.data.model.Channel;
import com.walhalla.data.repository.RepoCallback;
import com.walhalla.ui.plugins.Launcher;

import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.playlist.SerialInfoActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.ChannelAdapter;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.InfoLayoutBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.BaseFragment;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;

public class SerialInfoClass
        extends BaseFragment
        implements RepoCallback<XtreamChannelInfo.Info>, ChannelAdapter.OnItemClickListener {

    protected final String THISCLAZZNAME = getClass().getSimpleName();

    XtreamChannelInfo.Info data;
    protected static final String KEY_INTENT_CHANNEL_ID = "key_data1";
    protected @NonNull InfoLayoutBinding binding;
    private PrefManager prefManager;
    private AdsPref adsPref;
    private AdNetwork adNetwork;
    private int seriesId;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle intent = getArguments();
        if (intent != null && intent.containsKey(KEY_INTENT_CHANNEL_ID)) {
            data = (XtreamChannelInfo.Info) getArguments().getSerializable(KEY_INTENT_CHANNEL_ID);
            seriesId = getArguments().getInt(SerialInfoActivity.KEY_SERIES_ID, -99);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = InfoLayoutBinding.inflate(inflater, container, false);
        binding.tvNameText.setText(data.getName());
        binding.tvCastText.setText(data.getCast());
        binding.tvCategoryIdText.setText(data.getCategoryId());
        binding.tvDirectorText.setText(data.getDirector());
        binding.tvEpisodeRunTimeText.setText(data.getEpisodeRunTime());
        binding.tvGenreText.setText(data.getGenre());
        binding.tvLastModifiedText.setText(convertUnixToDate(data.getLastModified()));
        binding.tvPlotText.setText(data.getPlot());
        binding.tvRatingText.setText(data.getRating());
        binding.tvRating5basedText.setText(data.getRating5based());
        binding.tvReleaseDateText.setText(data.getReleaseDate());

        String m = data.getYoutubeTrailer();
        if (!TextUtils.isEmpty(m)) {
            if (!m.startsWith("http")) {
                m = "https://www.youtube.com/watch?v=" + m;
            }
            binding.tvYoutubeTrailerText.setText(m);
            binding.tvYoutubeTrailerText.setPaintFlags(binding.tvYoutubeTrailerText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            String finalM = m;
            binding.tvYoutubeTrailerText.setOnClickListener(v->{
                Launcher.openBrowser(getContext(), finalM);
            });

        }

        return binding.getRoot();
    }

    public static Fragment newInstance(int seriesId, XtreamChannelInfo.Info data) {
        SerialInfoClass fragment = new SerialInfoClass();
        Bundle args = new Bundle();
        args.putSerializable(KEY_INTENT_CHANNEL_ID, data);
        args.putSerializable(SerialInfoActivity.KEY_SERIES_ID, seriesId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    protected void loadCategory() {
//        setBadgeText(THISCLAZZNAME, String.valueOf(data.size()));
//        categoriesAdapter.swapData(data);
    }

    @Override
    public void successResult(XtreamChannelInfo.Info data) {

    }

    @Override
    public void errorResult(String err) {

    }

    @Override
    public void onItemClick(View view, Channel obj, int position) {

    }
}
