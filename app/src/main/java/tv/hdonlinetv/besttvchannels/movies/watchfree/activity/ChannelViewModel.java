package tv.hdonlinetv.besttvchannels.movies.watchfree.activity;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.walhalla.data.model.Channel;

public class ChannelViewModel extends ViewModel {

    private final MutableLiveData<Channel> channelLiveData = new MutableLiveData<>();

    public LiveData<Channel> getChannel() {
        return channelLiveData;
    }

    public void setChannel(Channel channel) {
        channelLiveData.setValue(channel);
    }

    public void toggleFavorite() {
        Channel channel = channelLiveData.getValue();
        if (channel != null) {
            channel.liked = channel.liked > 0 ? 0 : 1;
            channelLiveData.setValue(channel);
        }
    }
}