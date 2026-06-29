package tv.hdonlinetv.compose.player

import android.content.Context
import android.net.Uri
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import com.walhalla.ui.DLog.d
import tv.hdonlinetv.compose.core.domain.model.ChannelUi

class LegacyCastController(
    context: Context,
    private val channel: ChannelUi,
    private val setup: JzPlayerSetup,
) {
    private val castContext: CastContext? = try {
        CastContext.getSharedInstance(context)
    } catch (_: Exception) {
        null
    }
    private var castSession: CastSession? = null
    private var remoteMediaClient: RemoteMediaClient? = null

    private val sessionManagerListener: SessionManagerListener<CastSession> =
        object : MySessionManagerListener() {
            override fun onSessionEnded(session: CastSession, error: Int) {
                remoteMediaClient = null
                castSession = null
            }

            override fun onSessionStarted(session: CastSession, s: String) {
                castSession = session
                d("{{SessionStarted}}..... ${session.isConnected}")
                remoteMediaClient = session.remoteMediaClient
                playMedia()
            }

            override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
                castSession = session
                playMedia()
            }
        }

    fun onResume() {
        val ctx = castContext ?: return
        ctx.sessionManager.addSessionManagerListener(sessionManagerListener, CastSession::class.java)
        ctx.sessionManager.currentCastSession?.let { castSession = it }
        playMedia()
    }

    fun onPause() {
        castContext?.sessionManager?.removeSessionManagerListener(sessionManagerListener, CastSession::class.java)
    }

    private fun playMedia() {
        if (!isConnected) {
            d("Not connected to Chromecast.")
            return
        }
        d("Connected to Chromecast, starting media playback...")
        loadCastMedia()
    }

    private fun loadCastMedia() {
        val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
        movieMetadata.putString(MediaMetadata.KEY_TITLE, channel.name)
        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, channel.category ?: "")
        channel.cover?.let { movieMetadata.addImage(WebImage(Uri.parse(it))) }

        val builder = MediaInfo.Builder(channel.link ?: "")
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("application/x-mpegURL")
            .setMetadata(movieMetadata)
        setup.customData?.let { builder.setCustomData(it) }
        val mediaInfo = builder.build()

        remoteMediaClient = castSession?.remoteMediaClient
        remoteMediaClient?.load(MediaLoadRequestData.Builder().setMediaInfo(mediaInfo).build())
    }

    private val isConnected: Boolean
        get() {
            val session = castContext?.sessionManager?.currentCastSession
            return session != null && session.isConnected
        }
}
