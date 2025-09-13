package tv.hdonlinetv.besttvchannels.movies.watchfree.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.walhalla.data.model.Channel;

import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig;
import tv.hdonlinetv.besttvchannels.movies.watchfree.R;

public class ChannelViewHolder extends RecyclerView.ViewHolder {


    private final ImageView geolock;
    ImageView imageView;
    TextView textView, mainCategory;

    public ChannelViewHolder(View itemView) {
        super(itemView);

        geolock = itemView.findViewById(R.id.geolock);
        imageView = itemView.findViewById(R.id.movieImage);
        textView = itemView.findViewById(R.id.tvCategory);
        mainCategory = itemView.findViewById(R.id.mainCategory);
    }

    public void bind(Channel channel) {
        textView.setText(channel.getName());
        mainCategory.setText(channel.getCat());

        int m = (channel.getName() != null && channel.getName().contains("Geo-blocked")) ? View.VISIBLE : View.GONE;
        geolock.setVisibility(m);

        geolock.setOnClickListener(v -> {
            Toast.makeText(geolock.getContext(),
                    "Geo-blocked", Toast.LENGTH_SHORT).show();
        });

        if (BuildConfig.DEBUG) {
            if (!TextUtils.isEmpty(channel.extReferer) || !TextUtils.isEmpty(channel.extUserAgent)) {
                itemView.setBackgroundColor(Color.RED);
            }
            if (!TextUtils.isEmpty(channel.getUa())) {
                itemView.setBackgroundColor(Color.YELLOW);
            }

        }
    }
}