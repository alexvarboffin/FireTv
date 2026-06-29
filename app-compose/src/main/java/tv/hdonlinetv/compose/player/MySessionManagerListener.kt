package tv.hdonlinetv.compose.player

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
        d("Session START failed. Error: ${CastStatusCodes.getStatusCodeString(error)} | Code: $error")
    }

    override fun onSessionSuspended(session: CastSession, reason: Int) {
        d("@@@@@@$session $reason")
    }

    override fun onSessionResumeFailed(session: CastSession, error: Int) {
        d("Session resume failed. Error: ${CastStatusCodes.getStatusCodeString(error)} | Code: $error")
    }

    override fun onSessionEnding(castSession: CastSession) {
        d("@@@@@@$castSession")
    }
}
