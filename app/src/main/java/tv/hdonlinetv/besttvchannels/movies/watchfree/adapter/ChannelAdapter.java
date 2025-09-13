package tv.hdonlinetv.besttvchannels.movies.watchfree.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.walhalla.data.model.Channel;

import tv.hdonlinetv.besttvchannels.movies.watchfree.R;

import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;

import com.walhalla.ui.DLog;

import java.util.ArrayList;
import java.util.List;


public class ChannelAdapter
        extends RecyclerView.Adapter<ChannelViewHolder> implements Filterable {

    private final Context context;
    private final List<Channel> data;

    private boolean defaultType;

    public boolean isListType() {
        DLog.d("=====" + defaultType);
        return defaultType;
    }

    //private String type;

    public void setType(String type) {
        //this.type = type;
        this.defaultType = type.equals(PrefManager.TYPE_LIST);

        DLog.d("@@@" + type + "@@@" + defaultType);
    }


    private List<Channel> searchList;
    private PrefManager prf;
    private OnItemClickListener mOnItemClickListener;

    public List<Channel> getItems() {
        return data;
    }

    public void swapData(List<Channel> data) {
        this.data.clear();
        this.data.addAll(data);
        // Обновляем searchList только при изменении данных
        this.searchList = new ArrayList<>(data);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Channel obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemOverflowClickListener) {
        this.mOnItemClickListener = mItemOverflowClickListener;
    }

    public ChannelAdapter(Context mCtx, List<Channel> wallpaperList) {
        this.context = mCtx;
        this.data = new ArrayList<>(wallpaperList);
        this.searchList = new ArrayList<>(wallpaperList);
        this.prf = new PrefManager(mCtx);
        this.setType(prf.getChannelDisplayItemType());
    }

    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (isListType()) {
            View view = inflater.inflate(R.layout.item_channel_list, parent, false);
            return new ChannelViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_channel_grid, parent, false);
            return new ChannelViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder holder, final int position) {

        Channel channel = searchList.get(position);

        if (isListType()) {
            //String thumb = channel.getLang();
            String thumb = channel.getCover();
            if (TextUtils.isEmpty(thumb)) {
                Glide.with(context)
                        .load(R.drawable.logo)
                        .placeholder(R.drawable.logo)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                if (e != null) {
                                    DLog.d("@@w@@" + thumb + " @@ " + e.getLocalizedMessage());
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(holder.imageView);
            } else {
                //String thumb0 = channel.getLang();
                String thumb0 = channel.getCover();

                Glide.with(context)
                        .load(thumb0)
                        .placeholder(R.drawable.placeholder)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                if (e != null) {
                                    DLog.d("@@b@@" + channel.getName() + " " + e.getLocalizedMessage());
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(holder.imageView);
            }

        } else {
            String thumb = channel.getCover();
            if (TextUtils.isEmpty(thumb)) {
                Glide.with(context)
                        .load(R.drawable.logo)
                        .placeholder(R.drawable.logo)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                if (e != null) {
                                    DLog.d("@@w@@" + thumb + " @@ " + e.getLocalizedMessage());
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(holder.imageView);
            } else {
                Glide.with(context)
                        .load(thumb)
                        .placeholder(R.drawable.logo)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                if (e != null) {
                                    DLog.d("@@w@@" + thumb + " @@ " + e.getLocalizedMessage());
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(holder.imageView);
            }
        }

        holder.bind(channel);

        //int position = getAdapterPosition();
        holder.itemView.setOnClickListener(v -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(v, searchList.get(position), position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    searchList = data;
                } else {
                    ArrayList<Channel> filteredList = new ArrayList<>();
                    for (Channel row : data) {
                        if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getCat().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    searchList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = searchList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                searchList = (ArrayList<Channel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

}
