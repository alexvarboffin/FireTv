package tv.hdonlinetv.besttvchannels.movies.watchfree

import android.content.Context
import androidx.annotation.Keep
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider

// https://cast.google.com/publish/#
@Keep
class MyOptionsProvider : OptionsProvider {
    //    @Override
    //    public Map<String, Object> getOptions() {
    //        return new HashMap<>();
    //    }
    override fun getCastOptions(context: Context): CastOptions {
        val castOptions = CastOptions.Builder()
            .setReceiverApplicationId(
                CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID //context.getString(R.string.chromecast_app_id)
            )
            .build()
        return castOptions
    }

    //    @Nullable
    //    @Override
    //    public List<SessionProvider> getAdditionalSessionProviders(@NonNull Context context) {
    //        return Collections.emptyList();
    //    }

    override fun getAdditionalSessionProviders(context: Context): MutableList<SessionProvider>? {
        return null
    }
}

