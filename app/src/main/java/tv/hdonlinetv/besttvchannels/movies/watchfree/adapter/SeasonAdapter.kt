package tv.hdonlinetv.besttvchannels.movies.watchfree.adapter;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

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
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ItemSeasonBinding;

public class SeasonAdapter extends RecyclerView.Adapter<SeasonAdapter.SeasonViewHolder> {

    private List<ResponseData.Season> seasons;
    private OnSeasonClickListener listener;

    public interface OnSeasonClickListener {
        void onSeasonClick(ResponseData.Season season);
    }

    public SeasonAdapter(List<ResponseData.Season> seasons, OnSeasonClickListener listener) {
        this.seasons = seasons;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SeasonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSeasonBinding binding = ItemSeasonBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SeasonViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SeasonViewHolder holder, int position) {
        ResponseData.Season season = seasons.get(position);
        holder.bind(season);
    }

    @Override
    public int getItemCount() {
        return seasons != null ? seasons.size() : 0;
    }

    class SeasonViewHolder extends RecyclerView.ViewHolder {
        private final ItemSeasonBinding binding;

        SeasonViewHolder(ItemSeasonBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ResponseData.Season season) {
            binding.tvName.setText(season.getName());
            binding.tvAirDate.setText(season.getAirDate());
            binding.tvEpisodeCount.setText("Episodes: " + season.getEpisodeCount());
            binding.tvOverview.setText(season.getOverview());
            String m = season.getCover();
            if (TextUtils.isEmpty(m)) {
                Glide.with(binding.getRoot().getContext())
                        .load(R.drawable.placeholder)
                        //.placeholder(R.drawable.ic_tv_icon_white)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.cover);
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
                        .into(binding.cover);
            }
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSeasonClick(season);
                }
            });
        }
    }
}

