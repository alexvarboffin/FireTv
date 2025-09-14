package tv.hdonlinetv.besttvchannels.movies.watchfree.tutorial;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.PrivacyActivity;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ItemFaqBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ItemHeaderBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ItemPlaylist2Binding;

public class FAQAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_HEADER = 2;
    public static final int VIEW_TYPE_FAQ = 0;
    public static final int VIEW_TYPE_PLAYLIST = 1;

    private final List<FAQItem> faqItems;
    private final Context context;
    private final MyCallback callback;

    public FAQAdapter(Context context, List<FAQItem> faqItems, MyCallback callback) {
        this.faqItems = faqItems;
        this.context = context;
        this.callback = callback;
    }

    @Override
    public int getItemViewType(int position) {
        return faqItems.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FAQ) {
            ItemFaqBinding binding = ItemFaqBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new FAQViewHolder(binding);
        } else if (viewType == VIEW_TYPE_PLAYLIST) {
            ItemPlaylist2Binding binding = ItemPlaylist2Binding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new PlaylistViewHolder(binding);
        } else {  // VIEW_TYPE_HEADER
            ItemHeaderBinding binding = ItemHeaderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new HeaderViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FAQItem item = faqItems.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_FAQ) {
            FAQViewHolder faqHolder = (FAQViewHolder) holder;
            faqHolder.bind((FAQQuestion) item);

            // Set up the click listener
            faqHolder.binding.textViewFAQ.setOnClickListener(v -> {
                Intent intent = new Intent(context, PrivacyActivity.class);
                intent.putExtra(PrivacyActivity.EXTRA_HTML_URL0, ((FAQQuestion) item).getHtmlUrl());
                intent.putExtra(PrivacyActivity.EXTRA_TITLE, ((FAQQuestion) item).getQuestion());
                context.startActivity(intent);
            });
        } else if (holder.getItemViewType() == VIEW_TYPE_PLAYLIST) {
            PlaylistItem playlistItem = (PlaylistItem) item;
            PlaylistViewHolder h0 = ((PlaylistViewHolder) holder);
            h0.bind(playlistItem);

            h0.itemView.setOnClickListener(v -> callback.onItemClisk(playlistItem.getUrl()));
            h0.binding.btnCopy1.setOnClickListener(v -> callback.onItemClisk(playlistItem.getUrl()));
        } else if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
            ((HeaderViewHolder) holder).bind((HeaderItem) item);
        }
    }

    @Override
    public int getItemCount() {
        return faqItems.size();
    }

    static class FAQViewHolder extends RecyclerView.ViewHolder {
        private final ItemFaqBinding binding;

        public FAQViewHolder(ItemFaqBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FAQQuestion question) {
            binding.textViewFAQ.setText(question.getQuestion());
        }
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private final ItemPlaylist2Binding binding;

        public PlaylistViewHolder(ItemPlaylist2Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PlaylistItem playlistItem) {
            String zz = playlistItem.getPlaylistTitle();
            binding.tvPlaylistUK.setText(zz);
            binding.tvPlaylistUKUrl.setText(playlistItem.getSubTitle());
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final ItemHeaderBinding binding;

        public HeaderViewHolder(ItemHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(HeaderItem headerItem) {
            binding.headerTitle.setText(headerItem.getHeaderTitle());
        }
    }



    public interface MyCallback {
        void onItemClisk(String url);
    }
}

