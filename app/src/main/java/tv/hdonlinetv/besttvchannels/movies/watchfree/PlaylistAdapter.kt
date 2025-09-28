package tv.hdonlinetv.besttvchannels.movies.watchfree

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.walhalla.data.model.PlaylistImpl
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.TypeUtils
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ItemEmptyPlaylistBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ItemPlaylistBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.DateFormatUtils
import java.lang.String
import java.util.Locale
import kotlin.CharSequence
import kotlin.Int

class PlaylistAdapter(
    private val context: Context,
    playlists: MutableList<PlaylistImpl>?,
    private val listener: OnPlaylistActionListener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>(), Filterable {
    private val data: MutableList<PlaylistImpl>
    private var filteredPlaylists: MutableList<PlaylistImpl> // Новый список для фильтрации

    init {
        this.data = if (playlists == null) ArrayList<PlaylistImpl>() else playlists
        this.filteredPlaylists =
            ArrayList<PlaylistImpl>(this.data) // Изначально отфильтрованный список = исходный
    }

    override fun getItemViewType(position: Int): Int {
        if (filteredPlaylists.isEmpty()) {
            return ITEM_VIEW_TYPE_EMPTY
        } else {
            return ITEM_VIEW_TYPE_PLAYLIST
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val m = LayoutInflater.from(parent.context)
        if (viewType == ITEM_VIEW_TYPE_PLAYLIST) {
            val binding = ItemPlaylistBinding.inflate(m, parent, false)
            return PlaylistViewHolder(binding)
        } else {
            // Inflate the empty view
            val view = ItemEmptyPlaylistBinding.inflate(m, parent, false)
            return EmptyViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == ITEM_VIEW_TYPE_PLAYLIST) {
            val playlistHolder = holder as PlaylistViewHolder
            val playlist = filteredPlaylists[position]
            playlistHolder.bind(playlist, listener)
            playlistHolder.binding.tvPlaylistInfo.text = generatePlaylistInfo(context, playlist)
        } else {
            // Handle the empty view, no additional setup needed for now
        }
    }

    override fun getItemCount(): Int {
        // Показываем один элемент, если список пуст, для отображения empty view
        return if (filteredPlaylists.isEmpty()) 1 else filteredPlaylists.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onDelete(playlist: PlaylistImpl?, absoluteAdapterPosition: Int) {
        data.remove(playlist)
        filteredPlaylists.remove(playlist)
        notifyItemRemoved(absoluteAdapterPosition)

        // Проверяем пуст ли отфильтрованный список после удаления
        if (filteredPlaylists.isEmpty()) {
            notifyDataSetChanged() // Если пусто, обновляем весь список для отображения пустого состояния
        }
    }

    fun swapData(newValue: List<PlaylistImpl>) {
        this.data.clear()
        this.filteredPlaylists.clear()
        this.data.addAll(newValue)
        this.filteredPlaylists.addAll(this.data)
        notifyDataSetChanged()
    }

    internal class PlaylistViewHolder(val binding: ItemPlaylistBinding) :
        RecyclerView.ViewHolder(
            binding.getRoot()
        ) {
        fun bind(playlist: PlaylistImpl, listener: OnPlaylistActionListener?) {
            binding.icon.setImageResource(TypeUtils.getIconByType(playlist.type))
            binding.icon.setOnClickListener(View.OnClickListener { v: View? ->
                val m = TypeUtils.getMsgByType(playlist.type)
                if (m != null) {
                    Toast.makeText(v!!.getContext(), m, Toast.LENGTH_SHORT).show()
                }
            })
            if (TypeUtils.TYPE_M3U_CLOUD == playlist.type) {
                binding.btnUpdate.setVisibility(View.VISIBLE)
                binding.btnUpdate.setOnClickListener(View.OnClickListener { v: View? ->
                    if (listener != null) {
                        listener.onUpdate(playlist, getAbsoluteAdapterPosition())
                    }
                })
            } else {
                binding.btnUpdate.setVisibility(View.GONE)
            }
            binding.tvPlaylistName.setText(String.valueOf(playlist.title))

            binding.btnEdit.setOnClickListener(View.OnClickListener { v: View? ->
                if (listener != null) {
                    listener.onEdit(playlist)
                }
            })


            binding.btnDelete.setOnClickListener(View.OnClickListener { v: View? ->
                if (listener != null) {
                    listener.onDelete(playlist, getAbsoluteAdapterPosition())
                }
            })

            itemView.setOnClickListener(View.OnClickListener { v: View? ->
                if (listener != null) {
                    listener.onItemClick(playlist, getAbsoluteAdapterPosition())
                }
            })
        }
    }

    internal class EmptyViewHolder(itemView: ItemEmptyPlaylistBinding) :
        RecyclerView.ViewHolder(itemView.getRoot())

    fun generatePlaylistInfo(context: Context, playlist: PlaylistImpl): kotlin.String {
        val info = StringBuilder()
        val importDate = playlist.importDate
        if (TypeUtils.TYPE_XTREAM_URL == playlist.type) {
        } else {
            info.append(context.getString(R.string.channels_format, playlist.count))
        }
        info.append(" | ").append(
            context.getString(
                R.string.added_on,
                DateFormatUtils.formatUpdateTime(importDate)
            )
        )
        if (playlist.updateDate > 0 && playlist.updateDate != (importDate)) {
            info.append(" | ").append(
                context.getString(
                    R.string.updated_on,
                    DateFormatUtils.formatUpdateTime(playlist.updateDate)
                )
            )
        }
        return info.toString()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            @SuppressLint("DefaultLocale")
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val query = constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                var filtered: MutableList<PlaylistImpl> = ArrayList<PlaylistImpl>()

                if (query.isEmpty()) {
                    filtered = data // Если запрос пуст, показываем весь список
                } else {
                    for (playlist in data) {
                        if ((playlist.title?:"").lowercase().contains(query)) {
                            filtered.add(playlist) // Фильтрация по имени
                        }
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = filtered
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                filteredPlaylists = results.values as MutableList<PlaylistImpl>
                notifyDataSetChanged() // Уведомляем адаптер об изменении данных
            }
        }
    }

    interface OnPlaylistActionListener {
        fun onEdit(playlist: PlaylistImpl)

        fun onDelete(playlist: PlaylistImpl, absoluteAdapterPosition: Int)

        fun onItemClick(playlist: PlaylistImpl, absoluteAdapterPosition: Int)

        fun onUpdate(playlist: PlaylistImpl, absoluteAdapterPosition: Int)
    }

    companion object {
        private const val ITEM_VIEW_TYPE_PLAYLIST = 0
        private const val ITEM_VIEW_TYPE_EMPTY = 1
    }
}
