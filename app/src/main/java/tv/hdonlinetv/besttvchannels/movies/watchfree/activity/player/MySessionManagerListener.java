package tv.hdonlinetv.besttvchannels.movies.watchfree.activity.player;

import androidx.annotation.NonNull;

import com.google.android.gms.cast.CastStatusCodes;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.walhalla.ui.DLog;

public abstract class MySessionManagerListener implements SessionManagerListener<CastSession> {


    @Override
    public void onSessionStarting(@NonNull CastSession session) {
        DLog.d("@@@@@@" + session);
    }

    @Override
    public void onSessionResuming(@NonNull CastSession castSession, @NonNull String s) {
        DLog.d("@@@@@@" + castSession+" "+s);
    }

    @Override
    public void onSessionStartFailed(@NonNull CastSession castSession, int error) {
        String errorMessage = CastStatusCodes.getStatusCodeString(error);
        DLog.d("Session START failed. Error: " + errorMessage + " | Code: " + error);

    }

    @Override
    public void onSessionSuspended(@NonNull CastSession session, int reason) {
        DLog.d("@@@@@@" + session + " " + reason);
    }

    @Override
    public void onSessionResumeFailed(@NonNull CastSession session, int error) {
        String errorMessage = CastStatusCodes.getStatusCodeString(error);
        DLog.d("Session resume failed. Error: " + errorMessage + " | Code: " + error);

    }

    @Override
    public void onSessionEnding(@NonNull CastSession castSession) {
        DLog.d("@@@@@@" + castSession);
    }

    // Implement other methods as needed

}
