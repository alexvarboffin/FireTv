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
import com.m3u.data.database.model.ResponseData
import com.walhalla.ui.DLog.d
import tv.hdonlinetv.besttvchannels.movies.watchfree.R
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ItemEpisodeBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ItemEpisodeHeaderBinding

class EpisodeAdapter(// Используем List<Object> для хранения и заголовков, и эпизодов.
    private val items: MutableList<Any?>, private val listener: OnEpisodeClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    interface OnEpisodeClickListener {
        fun onEpisodeClick(episode: ResponseData.Episode?)
    }

    override fun getItemViewType(position: Int): Int {
        if (items.get(position) is String) {
            return TYPE_HEADER
        } else {
            return TYPE_EPISODE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == TYPE_HEADER) {
            val headerBinding = ItemEpisodeHeaderBinding.inflate(inflater, parent, false)
            return HeaderViewHolder(headerBinding)
        } else {
            val episodeBinding = ItemEpisodeBinding.inflate(inflater, parent, false)
            return EpisodeViewHolder(episodeBinding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bind(items.get(position) as String?)
        } else if (holder is EpisodeViewHolder) {
            holder.bind((items.get(position) as com.m3u.data.database.model.ResponseData.Episode?)!!)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // ViewHolder для эпизодов
    internal inner class EpisodeViewHolder(private val binding: ItemEpisodeBinding) :
        RecyclerView.ViewHolder(
            binding.getRoot()
        ) {
        fun bind(episode: ResponseData.Episode) {
            binding.tvEpisodeTitle.text = episode.title
            binding.tvEpisodeValue.text = episode.episodeNum.toString()
            val info = episode.info

            binding.tvEpisodeReleaseDate.text = info!!.releasedate
            //binding.tvEpisodePlot.setText(info.plot);
            binding.tvEpisodeNum.text = "Episode " + episode.episodeNum

            val m = info.movieImage
            if (TextUtils.isEmpty(m)) {
                Glide.with(binding.getRoot().context)
                    .load(R.drawable.placeholder) //.placeholder(R.drawable.ic_tv_icon_white)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.movieImage)
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
                    .into(binding.movieImage)
            }
            itemView.setOnClickListener { v: View? ->
                listener.onEpisodeClick(
                    episode
                )
            }
        }
    }

    // ViewHolder для заголовков
    internal class HeaderViewHolder(private val binding: ItemEpisodeHeaderBinding) :
        RecyclerView.ViewHolder(
            binding.getRoot()
        ) {
        fun bind(title: String?) {
            binding.tvHeaderTitle.text = title
        }
    }

    companion object {
        private const val TYPE_EPISODE = 0
        const val TYPE_HEADER: Int = 1
    }
}


