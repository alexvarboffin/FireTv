package tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.xtream;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.walhalla.data.model.Channel;
import com.walhalla.data.repository.RepoCallback;

import java.util.ArrayList;
import java.util.List;

import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.ChannelAdapter;
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.BaseFragment;

public abstract class CaseChannelListFragment
        extends BaseFragment implements RepoCallback<List<Channel>>, ChannelAdapter.OnItemClickListener {

    protected ChannelAdapter channelAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        channelAdapter = new ChannelAdapter(getContext(), new ArrayList<>());
    }
}
