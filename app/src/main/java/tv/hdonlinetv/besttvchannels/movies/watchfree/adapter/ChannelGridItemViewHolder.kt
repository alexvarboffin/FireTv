package tv.hdonlinetv.besttvchannels.movies.watchfree.adapter

import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.walhalla.data.model.Channel
import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ItemChannelGridBinding

class ChannelGridItemViewHolder(private val binding: ItemChannelGridBinding) :
    RecyclerView.ViewHolder(
        binding.getRoot()
    ) {
    fun bind(channel: Channel) {
        binding.tvCategory.text = channel.name
        binding.mainCategory.text = channel.cat

        val visibility = if (channel.name.contains("Geo-blocked")) View.VISIBLE else View.GONE
        binding.geolock.setVisibility(visibility)

        binding.geolock.setOnClickListener { v: View? ->
            Toast.makeText(
                binding.geolock.context,
                "Geo-blocked", Toast.LENGTH_SHORT
            ).show()
        }

        if (BuildConfig.DEBUG) {
            if (!TextUtils.isEmpty(channel.extReferer) || !TextUtils.isEmpty(channel.extUserAgent)) {
                binding.getRoot().setBackgroundColor(Color.RED)
            }
            if (!TextUtils.isEmpty(channel.ua)) {
                binding.getRoot().setBackgroundColor(Color.YELLOW)
            }
        }
    }
}
