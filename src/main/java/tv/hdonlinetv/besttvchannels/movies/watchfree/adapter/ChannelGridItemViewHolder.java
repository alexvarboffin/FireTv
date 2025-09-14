package tv.hdonlinetv.besttvchannels.movies.watchfree.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.walhalla.data.model.Channel;

import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ItemChannelGridBinding;

public class ChannelGridItemViewHolder extends RecyclerView.ViewHolder {

    private final ItemChannelGridBinding binding;

    public ChannelGridItemViewHolder(ItemChannelGridBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Channel channel) {
        binding.tvCategory.setText(channel.getName());
        binding.mainCategory.setText(channel.getCat());

        int visibility = channel.getName().contains("Geo-blocked") ? View.VISIBLE : View.GONE;
        binding.geolock.setVisibility(visibility);

        binding.geolock.setOnClickListener(v -> {
            Toast.makeText(binding.geolock.getContext(),
                    "Geo-blocked", Toast.LENGTH_SHORT).show();
        });

        if (BuildConfig.DEBUG) {
            if (!TextUtils.isEmpty(channel.extReferer) || !TextUtils.isEmpty(channel.extUserAgent)) {
                binding.getRoot().setBackgroundColor(Color.RED);
            }
            if (!TextUtils.isEmpty(channel.ua)) {
                binding.getRoot().setBackgroundColor(Color.YELLOW);
            }
        }
    }
}
