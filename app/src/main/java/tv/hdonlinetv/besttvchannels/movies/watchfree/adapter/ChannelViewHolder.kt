package tv.hdonlinetv.besttvchannels.movies.watchfree.adapter

import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.walhalla.data.model.Channel
import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig
import tv.hdonlinetv.besttvchannels.movies.watchfree.R

class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val geolock: ImageView
    @JvmField
    var imageView: ImageView?
    var textView: TextView
    var mainCategory: TextView

    init {
        geolock = itemView.findViewById<ImageView>(R.id.geolock)
        imageView = itemView.findViewById<ImageView?>(R.id.movieImage)
        textView = itemView.findViewById<TextView>(R.id.tvCategory)
        mainCategory = itemView.findViewById<TextView>(R.id.mainCategory)
    }

    fun bind(channel: Channel) {
        textView.setText(channel.name)
        mainCategory.setText(channel.cat)

        val m =
            if (channel.name != null && channel.name.contains("Geo-blocked")) View.VISIBLE else View.GONE
        geolock.setVisibility(m)

        geolock.setOnClickListener(View.OnClickListener { v: View? ->
            Toast.makeText(
                geolock.getContext(),
                "Geo-blocked", Toast.LENGTH_SHORT
            ).show()
        })

        if (BuildConfig.DEBUG) {
            if (!TextUtils.isEmpty(channel.extReferer) || !TextUtils.isEmpty(channel.extUserAgent)) {
                itemView.setBackgroundColor(Color.RED)
            }
            if (!TextUtils.isEmpty(channel.ua)) {
                itemView.setBackgroundColor(Color.YELLOW)
            }
        }
    }
}