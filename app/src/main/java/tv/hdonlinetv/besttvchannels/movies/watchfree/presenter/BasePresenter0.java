package tv.hdonlinetv.besttvchannels.movies.watchfree.presenter;

import android.content.Context;
import android.os.Handler;

import com.walhalla.data.repository.BasePresenter;

import okhttp3.OkHttpClient;

public abstract class BasePresenter0 extends BasePresenter {

    protected final PlaylistManagementView view;
    protected OkHttpClient client;

    public BasePresenter0(Handler handler, PlaylistManagementView view, Context context) {
        super(handler, context);
        this.view = view;
    }

}
