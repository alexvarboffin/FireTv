package tv.hdonlinetv.besttvchannels.movies.watchfree.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.walhalla.data.model.Channel


class ChannelViewModel : ViewModel() {
    private val channelLiveData = MutableLiveData<Channel>()

    val channel: LiveData<Channel>
        get() = channelLiveData

    fun setChannel(channel: Channel) {
        channelLiveData.value = channel
    }

    fun toggleFavorite() {
        val channel = channelLiveData.value
        if (channel != null) {
            channel.liked = if (channel.liked > 0) 0 else 1
            channelLiveData.value = channel!!
        }
    }
}