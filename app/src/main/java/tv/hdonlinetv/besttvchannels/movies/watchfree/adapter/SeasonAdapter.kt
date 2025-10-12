package tv.hdonlinetv.besttvchannels.movies.watchfree.adapter

import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.m3u.data.database.model.ResponseData.Season
import com.walhalla.ui.DLog.d
import tv.hdonlinetv.besttvchannels.movies.watchfree.R
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.SeasonAdapter.SeasonViewHolder
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ItemSeasonBinding

class SeasonAdapter(
    private val seasons: MutableList<Season>?,
    private val listener: OnSeasonClickListener?
) : RecyclerView.Adapter<SeasonViewHolder>() {
    interface OnSeasonClickListener {
        fun onSeasonClick(season: Season?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeasonViewHolder {
        val binding = ItemSeasonBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SeasonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SeasonViewHolder, position: Int) {
        val season = seasons!![position]
        holder.bind(season)
    }

    override fun getItemCount(): Int {
        return if (seasons != null) seasons.size else 0
    }

    inner class SeasonViewHolder(private val binding: ItemSeasonBinding) :
        RecyclerView.ViewHolder(
            binding.getRoot()
        ) {
        fun bind(season: Season) {
            binding.tvName.setText(season.name)
            binding.tvAirDate.setText(season.airDate)
            binding.tvEpisodeCount.setText("Episodes: " + season.getEpisodeCount())
            binding.tvOverview.text = season.overview
            val m = season.cover
            if (TextUtils.isEmpty(m)) {
                Glide.with(binding.getRoot().context)
                    .load(R.drawable.placeholder) //.placeholder(R.drawable.ic_tv_icon_white)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.cover)
            } else {
                Glide.with(binding.getRoot().context)
                    .load(m)
                    .placeholder(R.drawable.placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            if (e != null) {
                                d("@@bb@@" + m + " " + e.localizedMessage)
                            }
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable?>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    })
                    .into(binding.cover)
            }
            itemView.setOnClickListener { v: View? ->
                if (listener != null) {
                    listener.onSeasonClick(season)
                }
            }
        }
    }
}

