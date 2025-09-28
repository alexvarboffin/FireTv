package tv.hdonlinetv.besttvchannels.movies.watchfree.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.walhalla.data.model.Channel
import com.walhalla.ui.DLog.d
import tv.hdonlinetv.besttvchannels.movies.watchfree.R
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager
import java.util.Locale
import kotlin.collections.ArrayList
import kotlin.collections.MutableList

class ChannelAdapter(private val context: Context, wallpaperList: List<Channel>) :
    RecyclerView.Adapter<ChannelViewHolder?>(), Filterable {
    val items: MutableList<Channel>

    private var defaultType = false

    val isListType: Boolean
        get() {
            d("=====" + defaultType)
            return defaultType
        }

    //private String type;
    fun setType(type: String) {
        //this.type = type;
        this.defaultType = type == PrefManager.TYPE_LIST

        d("@@@" + type + "@@@" + defaultType)
    }


    private var searchList: List<Channel>
    private val prf: PrefManager?
    private var mOnItemClickListener: OnItemClickListener? = null

    fun swapData(data: List<Channel>) {
        this.items.clear()
        this.items.addAll(data)
        // Обновляем searchList только при изменении данных
        this.searchList = ArrayList<Channel>(data)
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, obj: Channel, position: Int)
    }

    fun setOnItemClickListener(mItemOverflowClickListener: OnItemClickListener?) {
        this.mOnItemClickListener = mItemOverflowClickListener
    }

    init {
        this.items = ArrayList<Channel>(wallpaperList)
        this.searchList = ArrayList<Channel>(wallpaperList)
        this.prf = PrefManager(context)
        this.setType(prf.getChannelDisplayItemType())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val inflater = LayoutInflater.from(parent.getContext())
        if (this.isListType) {
            val view = inflater.inflate(R.layout.item_channel_list, parent, false)
            return ChannelViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_channel_grid, parent, false)
            return ChannelViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position0: Int) {
        val channel = searchList.get(holder.adapterPosition)
        val position = holder.adapterPosition

        if (this.isListType) {
            //String thumb = channel.getLang();
            val thumb = channel.cover
            if (TextUtils.isEmpty(thumb)) {
                Glide.with(context)
                    .load(R.drawable.logo)
                    .placeholder(R.drawable.logo)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            if (e != null) {
                                d("@@w@@" + thumb + " @@ " + e.localizedMessage)
                            }
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    })
                    .into(holder.imageView!!)
            } else {
                //String thumb0 = channel.getLang();
                val thumb0 = channel.cover

                Glide.with(context)
                    .load(thumb0)
                    .placeholder(R.drawable.placeholder)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            if (e != null) {
                                d("@@b@@" + channel.name + " " + e.localizedMessage)
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
                    .into(holder.imageView!!)
            }
        } else {
            val thumb = channel.cover
            if (TextUtils.isEmpty(thumb)) {
                Glide.with(context)
                    .load(R.drawable.logo)
                    .placeholder(R.drawable.logo)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            if (e != null) {
                                d("@@w@@" + thumb + " @@ " + e.localizedMessage)
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
                    .into(holder.imageView!!)
            } else {
                Glide.with(context)
                    .load(thumb)
                    .placeholder(R.drawable.logo)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            if (e != null) {
                                d("@@w@@" + thumb + " @@ " + e.localizedMessage)
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
                    .into(holder.imageView!!)
            }
        }

        holder.bind(channel)

        //int position = getAdapterPosition();
        holder.itemView.setOnClickListener { v: View ->
            if (mOnItemClickListener != null) {
                mOnItemClickListener!!.onItemClick(v, searchList[position], position)
            }
        }
    }

    override fun getItemCount(): Int {
        return searchList.size
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    searchList = items
                } else {
                    val filteredList = ArrayList<Channel>()
                    for (row in items) {
                        if (row.name.lowercase(Locale.getDefault()).contains(
                                charString.lowercase(
                                    Locale.getDefault()
                                )
                            ) || (row.cat ?: "").lowercase(Locale.getDefault()).contains(
                                charString.lowercase(
                                    Locale.getDefault()
                                )
                            )
                        ) {
                            filteredList.add(row)
                        }
                    }
                    searchList = filteredList
                }

                val filterResults = FilterResults()
                filterResults.values = searchList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                searchList = filterResults.values as ArrayList<Channel>
                notifyDataSetChanged()
            }
        }
    }
}
