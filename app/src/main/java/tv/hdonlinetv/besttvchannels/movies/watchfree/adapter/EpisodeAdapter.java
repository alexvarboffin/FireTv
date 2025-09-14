package tv.hdonlinetv.besttvchannels.movies.watchfree.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.m3u.data.database.model.ResponseData;
import com.walhalla.ui.DLog;

import java.util.List;

import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ItemEpisodeBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ItemEpisodeHeaderBinding;


public class EpisodeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_EPISODE = 0;
    public static final int TYPE_HEADER = 1;


    private final List<Object> items; // Используем List<Object> для хранения и заголовков, и эпизодов.
    private final OnEpisodeClickListener listener;

    public interface OnEpisodeClickListener {
        void onEpisodeClick(ResponseData.Episode episode);
    }

    public EpisodeAdapter(List<Object> items, OnEpisodeClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TYPE_HEADER;
        } else {
            return TYPE_EPISODE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            ItemEpisodeHeaderBinding headerBinding = ItemEpisodeHeaderBinding.inflate(inflater, parent, false);
            return new HeaderViewHolder(headerBinding);
        } else {
            ItemEpisodeBinding episodeBinding = ItemEpisodeBinding.inflate(inflater, parent, false);
            return new EpisodeViewHolder(episodeBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) items.get(position));
        } else if (holder instanceof EpisodeViewHolder) {
            ((EpisodeViewHolder) holder).bind((ResponseData.Episode) items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder для эпизодов
    class EpisodeViewHolder extends RecyclerView.ViewHolder {
        private final ItemEpisodeBinding binding;

        public EpisodeViewHolder(ItemEpisodeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ResponseData.Episode episode) {
            binding.tvEpisodeTitle.setText(episode.getTitle());
            binding.tvEpisodeNum.setText(String.valueOf(episode.getEpisodeNum()));
            ResponseData.EpisodeInfo info = episode.info;

            binding.tvEpisodeReleaseDate.setText(info.releasedate);
            //binding.tvEpisodePlot.setText(info.plot);
            binding.tvEpisodeNum.setText("Episode " + episode.getEpisodeNum());

            String m = info.movieImage;
            if (TextUtils.isEmpty(m)) {
                Glide.with(binding.getRoot().getContext())
                        .load(R.drawable.placeholder)
                        //.placeholder(R.drawable.ic_tv_icon_white)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.movieImage);
            } else {
                Glide.with(binding.getRoot().getContext())
                        .load(m)
                        .placeholder(R.drawable.placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                if (e != null) {
                                    DLog.d("@@bb@@" + m + " " + e.getLocalizedMessage());
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(binding.movieImage);
            }
            itemView.setOnClickListener(v -> listener.onEpisodeClick(episode));
        }
    }

    // ViewHolder для заголовков
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final ItemEpisodeHeaderBinding binding;

        public HeaderViewHolder(ItemEpisodeHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String title) {
            binding.tvHeaderTitle.setText(title);
        }
    }
}


