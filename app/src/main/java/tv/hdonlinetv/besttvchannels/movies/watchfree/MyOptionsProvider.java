package tv.hdonlinetv.besttvchannels.movies.watchfree;

import android.content.Context;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.SessionProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// https://cast.google.com/publish/#

@Keep
public class MyOptionsProvider implements OptionsProvider {

//    @Override
//    public Map<String, Object> getOptions() {
//        return new HashMap<>();
//    }

    @Override
    public CastOptions getCastOptions(@NonNull Context context) {
        CastOptions castOptions = new CastOptions.Builder()
                .setReceiverApplicationId(
                        CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID
                        //context.getString(R.string.chromecast_app_id)
                )
                .build();
        return castOptions;
    }

//    @Nullable
//    @Override
//    public List<SessionProvider> getAdditionalSessionProviders(@NonNull Context context) {
//        return Collections.emptyList();
//    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(@NonNull Context context) {
        return null;
    }

}

