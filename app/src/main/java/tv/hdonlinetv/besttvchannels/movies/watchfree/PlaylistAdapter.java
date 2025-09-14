package tv.hdonlinetv.besttvchannels.movies.watchfree;

import static tv.hdonlinetv.besttvchannels.movies.watchfree.activity.TypeUtils.TYPE_M3U_CLOUD;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.TypeUtils;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ItemEmptyPlaylistBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ItemPlaylistBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.DateFormatUtils;

import com.walhalla.data.model.PlaylistImpl;

public class PlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private static final int ITEM_VIEW_TYPE_PLAYLIST = 0;
    private static final int ITEM_VIEW_TYPE_EMPTY = 1;

    private final List<PlaylistImpl> data;
    private List<PlaylistImpl> filteredPlaylists;  // Новый список для фильтрации
    private final OnPlaylistActionListener listener;
    private final Context context;

    public PlaylistAdapter(Context context, List<PlaylistImpl> playlists, OnPlaylistActionListener listener) {
        this.data = (playlists == null) ? new ArrayList<>() : playlists;
        this.filteredPlaylists = new ArrayList<>(this.data);  // Изначально отфильтрованный список = исходный
        this.listener = listener;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (filteredPlaylists.isEmpty()) {
            return ITEM_VIEW_TYPE_EMPTY;
        } else {
            return ITEM_VIEW_TYPE_PLAYLIST;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater m = LayoutInflater.from(parent.getContext());
        if (viewType == ITEM_VIEW_TYPE_PLAYLIST) {
            ItemPlaylistBinding binding = ItemPlaylistBinding.inflate(m, parent, false);
            return new PlaylistViewHolder(binding);
        } else {
            // Inflate the empty view
            @NonNull ItemEmptyPlaylistBinding view = ItemEmptyPlaylistBinding.inflate(m, parent, false);
            return new EmptyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == ITEM_VIEW_TYPE_PLAYLIST) {
            PlaylistViewHolder playlistHolder = (PlaylistViewHolder) holder;
            PlaylistImpl playlist = filteredPlaylists.get(position);
            playlistHolder.bind(playlist, listener);
            playlistHolder.binding.tvPlaylistInfo.setText(generatePlaylistInfo(context, playlist));
        } else {
            // Handle the empty view, no additional setup needed for now
        }
    }

    @Override
    public int getItemCount() {
        // Показываем один элемент, если список пуст, для отображения empty view
        return filteredPlaylists.isEmpty() ? 1 : filteredPlaylists.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void onDelete(PlaylistImpl playlist, int absoluteAdapterPosition) {
        data.remove(playlist);
        filteredPlaylists.remove(playlist);
        notifyItemRemoved(absoluteAdapterPosition);

        // Проверяем пуст ли отфильтрованный список после удаления
        if (filteredPlaylists.isEmpty()) {
            notifyDataSetChanged(); // Если пусто, обновляем весь список для отображения пустого состояния
        }
    }

    public void swapData(List<PlaylistImpl> newValue) {
        this.data.clear();
        this.filteredPlaylists.clear();
        this.data.addAll(newValue);
        this.filteredPlaylists.addAll(this.data);
        notifyDataSetChanged();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {

        private final ItemPlaylistBinding binding;

        public PlaylistViewHolder(ItemPlaylistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PlaylistImpl playlist, OnPlaylistActionListener listener) {
            binding.icon.setImageResource(TypeUtils.getIconByType(playlist.type));
            binding.icon.setOnClickListener(v -> {
                Integer m = TypeUtils.getMsgByType(playlist.type);
                if (m != null) {
                    Toast.makeText(v.getContext(), m, Toast.LENGTH_SHORT).show();
                }
            });
            if (TYPE_M3U_CLOUD == playlist.type) {
                binding.btnUpdate.setVisibility(View.VISIBLE);
                binding.btnUpdate.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onUpdate(playlist, getAbsoluteAdapterPosition());
                    }
                });
            } else {
                binding.btnUpdate.setVisibility(View.GONE);
            }
            binding.tvPlaylistName.setText(String.valueOf(playlist.getTitle()));

            binding.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(playlist);
                }
            });


            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(playlist, getAbsoluteAdapterPosition());
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(playlist, getAbsoluteAdapterPosition());
                }
            });
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(@NonNull ItemEmptyPlaylistBinding itemView) {
            super(itemView.getRoot());
        }
    }

    public String generatePlaylistInfo(Context context, PlaylistImpl playlist) {

        StringBuilder info = new StringBuilder();
        long importDate = playlist.getImportDate();
        if (TypeUtils.TYPE_XTREAM_URL == playlist.type) {

        } else {
            info.append(context.getString(R.string.channels_format, playlist.getCount()));
        }
        info.append(" | ").append(context.getString(R.string.added_on
                , DateFormatUtils.formatUpdateTime(importDate)));
        if (playlist.updateDate > 0 && playlist.updateDate != (importDate)) {
            info.append(" | ").append(context.getString(R.string.updated_on
                    , DateFormatUtils.formatUpdateTime(playlist.updateDate)));
        }
        return info.toString();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString().toLowerCase().trim();
                List<PlaylistImpl> filtered = new ArrayList<>();

                if (query.isEmpty()) {
                    filtered = data;  // Если запрос пуст, показываем весь список
                } else {
                    for (PlaylistImpl playlist : data) {
                        if (playlist.getTitle().toLowerCase().contains(query)) {
                            filtered.add(playlist);  // Фильтрация по имени
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filtered;
                return filterResults;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredPlaylists = (List<PlaylistImpl>) results.values;
                notifyDataSetChanged();  // Уведомляем адаптер об изменении данных
            }
        };
    }

    public interface OnPlaylistActionListener {
        void onEdit(PlaylistImpl playlist);

        void onDelete(PlaylistImpl playlist, int absoluteAdapterPosition);

        void onItemClick(PlaylistImpl playlist, int absoluteAdapterPosition);

        void onUpdate(PlaylistImpl playlist, int absoluteAdapterPosition);
    }
}
