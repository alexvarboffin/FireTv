package tv.hdonlinetv.besttvchannels.movies.watchfree.adapter

import android.content.Context
import android.graphics.Color
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
import com.walhalla.ui.DLog.d
import tv.hdonlinetv.besttvchannels.movies.watchfree.R
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.CategoryAdapter.CategoryViewHolder
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ItemCategoryBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.model.CategoryUI
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager
import java.util.Locale

class CategoryAdapter(private val context: Context, categoryList: MutableList<CategoryUI>) :
    RecyclerView.Adapter<CategoryViewHolder?>(), Filterable {
    private val mColors: Array<String?>

    private val data: MutableList<CategoryUI>
    private var searchList: MutableList<CategoryUI>
    private val prf: PrefManager?
    private var mOnItemClickListener: OnItemClickListener? = null

    fun swapData(newCategories: MutableList<CategoryUI>) {
        this.data.clear()
        this.data.addAll(newCategories)
        // Обновляем searchList только при изменении данных
        this.searchList = ArrayList<CategoryUI>(data)
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: CategoryUI?, position: Int)
    }

    fun setOnItemClickListener(mItemOverflowClickListener: OnItemClickListener?) {
        this.mOnItemClickListener = mItemOverflowClickListener
    }

    init {
        this.data = ArrayList<CategoryUI>(categoryList)
        this.searchList = ArrayList<CategoryUI>(categoryList)
        this.prf = PrefManager(context)
        this.mColors = context.getResources().getStringArray(R.array.category_colors)
    }


    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val c = searchList.get(position)

        holder.binding.tvCategory.setText(c.name)

        if (TextUtils.isEmpty(c.thumb)) {
            Glide.with(context)
                .load(R.drawable.ic_tv_icon_white)
                .placeholder(R.drawable.ic_tv_icon_white)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.binding.ivCategory0)
        } else {
            Glide.with(context)
                .load(c.thumb)
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
                            d("@@bb@@" + c.thumb + " " + e.getLocalizedMessage())
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
                .into(holder.binding.ivCategory0)
        }


        holder.binding.colorBackground.setBackgroundColor(Color.parseColor(mColors[position % 24]))
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return CategoryViewHolder(binding)
    }

    inner class CategoryViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(
            binding.getRoot()
        ), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        //        public void bind(CategoryUI category) {
        //            binding.tvCategory.setText(category.getName());
        //            binding.ivCategory.setImageResource(category.getImageResId());
        //            binding.colorBackground.setBackgroundColor(category.getBackgroundColor());
        //        }
        override fun onClick(view: View?) {
            val position = getAdapterPosition()
            if (mOnItemClickListener != null && position != RecyclerView.NO_POSITION) {
                mOnItemClickListener!!.onItemClick(view, searchList.get(position), position)
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    searchList = data
                } else {
                    val filteredList = ArrayList<CategoryUI>()
                    for (row in data) {
                        if (row.getName().lowercase(Locale.getDefault()).contains(
                                charString.lowercase(
                                    Locale.getDefault()
                                )
                            ) || row.getDesc().lowercase(Locale.getDefault()).contains(
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
                searchList = filterResults.values as ArrayList<CategoryUI>
                notifyDataSetChanged()
            }
        }
    }
}
