package tv.hdonlinetv.besttvchannels.movies.watchfree.adapter;

import android.content.Context;
import android.graphics.Color;
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

import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ItemCategoryBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.model.CategoryUI;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;

import com.walhalla.ui.DLog;

import java.util.ArrayList;
import java.util.List;


public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>
        implements Filterable {

    private final String[] mColors;

    private final Context context;
    private List<CategoryUI> data;
    private List<CategoryUI> searchList;
    private PrefManager prf;
    private OnItemClickListener mOnItemClickListener;

    public void swapData(List<CategoryUI> newCategories) {
        this.data.clear();
        this.data.addAll(newCategories);
        // Обновляем searchList только при изменении данных
        this.searchList = new ArrayList<>(data);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, CategoryUI obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemOverflowClickListener) {
        this.mOnItemClickListener = mItemOverflowClickListener;
    }

    public CategoryAdapter(Context mCtx, List<CategoryUI> categoryList) {
        this.context = mCtx;
        this.data = new ArrayList<>(categoryList);
        this.searchList = new ArrayList<>(categoryList);
        this.prf = new PrefManager(mCtx);
        this.mColors = mCtx.getResources().getStringArray(R.array.category_colors);
    }


    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        CategoryUI c = searchList.get(position);

        holder.binding.tvCategory.setText(c.name);

        if (TextUtils.isEmpty(c.thumb)) {
            Glide.with(context)
                    .load(R.drawable.ic_tv_icon_white)
                    .placeholder(R.drawable.ic_tv_icon_white)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.binding.ivCategory0);
        } else {
            Glide.with(context)
                    .load(c.thumb)
                    .placeholder(R.drawable.logo)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                            if (e != null) {
                                DLog.d("@@bb@@" + c.thumb + " " + e.getLocalizedMessage());
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(holder.binding.ivCategory0);
        }


        holder.binding.colorBackground.setBackgroundColor(Color.parseColor(mColors[position % 24]));

    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(LayoutInflater.from(context), parent, false);
        return new CategoryViewHolder(binding);
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        private final ItemCategoryBinding binding;

        public CategoryViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnClickListener(this);
        }

//        public void bind(CategoryUI category) {
//            binding.tvCategory.setText(category.getName());
//            binding.ivCategory.setImageResource(category.getImageResId());
//            binding.colorBackground.setBackgroundColor(category.getBackgroundColor());
//        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (mOnItemClickListener != null && position != RecyclerView.NO_POSITION) {
                mOnItemClickListener.onItemClick(view, searchList.get(position), position);
            }
        }

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
                    ArrayList<CategoryUI> filteredList = new ArrayList<>();
                    for (CategoryUI row : data) {
                        if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getDesc().toLowerCase().contains(charString.toLowerCase())) {
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
                searchList = (ArrayList<CategoryUI>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
