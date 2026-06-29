package tv.hdonlinetv.besttvchannels.movies.watchfree.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener
import com.google.android.youtube.player.YouTubePlayerView
import tv.hdonlinetv.besttvchannels.movies.watchfree.R

class ActivityYoutubePlayer : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener {
    private var youTubeView: YouTubePlayerView? = null
    private var playerStateChangeListener: MyPlayerStateChangeListener? = null
    private var id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_youtube)

        val b = intent.extras
        id = b!!.get("id") as String?
        if (id != null) {
            Log.e("id", id!!)
        }

        youTubeView = findViewById(R.id.youtube_view)
        youTubeView!!.initialize(getString(R.string.youtube_api_key0), this)

        playerStateChangeListener = MyPlayerStateChangeListener()
    }

    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider?,
        player: YouTubePlayer,
        wasRestored: Boolean
    ) {
        player.setPlayerStateChangeListener(playerStateChangeListener)

        if (!wasRestored) {
            player.loadVideo(id)
        }
    }

    override fun onInitializationFailure(
        provider: YouTubePlayer.Provider?,
        errorReason: YouTubeInitializationResult
    ) {
        if (errorReason.isUserRecoverableError) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show()
        } else {
            Toast.makeText(this, getResources().getString(R.string.error_player), Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            this.youTubePlayerProvider.initialize(getString(R.string.youtube_api_key0), this)
        }
    }

    protected val youTubePlayerProvider: YouTubePlayer.Provider
        get() = youTubeView!!

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    private class MyPlayerStateChangeListener : PlayerStateChangeListener {
        override fun onLoading() {
            // Called when the player is loading a video
            // At this point, it's not ready to accept commands affecting playback such as play() or pause()
        }

        override fun onLoaded(s: String?) {
            // Called when a video is done loading.
            // Playback methods such as play(), pause() or seekToMillis(int) may be called after this callback.
        }

        override fun onAdStarted() {
            // Called when playback of an advertisement starts.
        }

        override fun onVideoStarted() {
            // Called when playback of the video starts.
        }

        override fun onVideoEnded() {
            // Called when the video reaches its end.
            //showInterstitialAd();
        }

        override fun onError(errorReason: YouTubePlayer.ErrorReason?) {
            // Called when an error occurs.
        }
    }

    companion object {
        private const val RECOVERY_REQUEST = 1
    }
}