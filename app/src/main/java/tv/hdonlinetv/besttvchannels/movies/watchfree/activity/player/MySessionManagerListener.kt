package tv.hdonlinetv.besttvchannels.movies.watchfree.activity.player

import com.google.android.gms.cast.CastStatusCodes
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.walhalla.ui.DLog.d

abstract class MySessionManagerListener : SessionManagerListener<CastSession> {
    override fun onSessionStarting(session: CastSession) {
        d("@@@@@@$session")
    }

    override fun onSessionResuming(castSession: CastSession, s: String) {
        d("@@@@@@$castSession $s")
    }

    override fun onSessionStartFailed(castSession: CastSession, error: Int) {
        val errorMessage = CastStatusCodes.getStatusCodeString(error)
        d("Session START failed. Error: $errorMessage | Code: $error")
    }

    override fun onSessionSuspended(session: CastSession, reason: Int) {
        d("@@@@@@$session $reason")
    }

    override fun onSessionResumeFailed(session: CastSession, error: Int) {
        val errorMessage = CastStatusCodes.getStatusCodeString(error)
        d("Session resume failed. Error: $errorMessage | Code: $error")
    }

    override fun onSessionEnding(castSession: CastSession) {
        d("@@@@@@$castSession")
    } // Implement other methods as needed
}
