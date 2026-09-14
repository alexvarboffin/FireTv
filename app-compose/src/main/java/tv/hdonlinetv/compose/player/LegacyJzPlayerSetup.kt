package tv.hdonlinetv.compose.player

import android.text.TextUtils
import cn.jzvd.JZDataSource
import cn.jzvd.Jzvd
import cn.jzvd.demo.CustomMedia.JZMediaAliyun
import cn.jzvd.demo.CustomMedia.JZMediaExo
import cn.jzvd.demo.CustomMedia.JZMediaIjk
import cn.jzvd.demo.CustomMedia.JZMediaSystemSafe
import cn.jzvd.demo.CustomMedia.JZMediaVlc
import coil3.load
import com.google.common.net.HttpHeaders
import com.walhalla.ui.DLog.d
import com.walhalla.ui.DLog.handleException
import org.json.JSONObject
import tv.hdonlinetv.compose.core.domain.model.ChannelUi

data class JzPlayerSetup(
    val dataSource: JZDataSource,
    val customData: JSONObject?,
)

object LegacyJzPlayerSetup {

    fun prepare(channel: ChannelUi): JzPlayerSetup? {
        val link = channel.link ?: return null
        val tmpUa = if (TextUtils.isEmpty(channel.extUserAgent)) channel.ua else channel.extUserAgent
        val headers = HashMap<String, String>()
        if (!tmpUa.isNullOrEmpty()) {
            headers[HttpHeaders.USER_AGENT] = tmpUa
        }
        if (!channel.extReferer.isNullOrEmpty()) {
            headers["Referer"] = channel.extReferer!!
        }

        val dataSource = JZDataSource(link, channel.name)
        val customData = if (headers.isEmpty()) {
            null
        } else {
            d("@w@$headers")
            dataSource.headerMap = headers
            JSONObject().also { json ->
                for ((key, value) in headers) {
                    try {
                        json.put(key, value)
                    } catch (e: Exception) {
                        handleException(e)
                    }
                }
            }
        }
        return JzPlayerSetup(dataSource, customData)
    }

    fun apply(
        player: JZVideoPlayerNew,
        channel: ChannelUi,
        setup: JzPlayerSetup,
        mediaPlayerOption: Int,
    ) {
        loadPoster(player.posterImageView, channel.cover)

        setUp00(player, setup.dataSource, Jzvd.SCREEN_NORMAL, mediaPlayerOption)
    }

    private fun loadPoster(imageView: android.widget.ImageView, url: String?) {
        val cover = url?.trim()?.takeIf { it.isNotBlank() } ?: return
        imageView.load(cover)
    }

    private fun setUp00(
        player: JZVideoPlayerNew,
        jzDataSource: JZDataSource,
        screen: Int,
        mediaPlayerOption: Int,
    ) {
        when (mediaPlayerOption) {
            0 -> clickChangeToSystem(player, jzDataSource, screen)
            1 -> clickChangeToAliyun(player, jzDataSource, screen)
            2 -> clickChangeToExo(player, jzDataSource, screen)
            3 -> clickChangeToIjkplayer(player, jzDataSource, screen)
            4 -> clickChangeToVlc(player, jzDataSource, screen)
            else -> player.setUp(jzDataSource, screen, JZMediaSystemSafe::class.java)
        }
    }

    private fun clickChangeToIjkplayer(player: JZVideoPlayerNew, jzDataSource: JZDataSource, screen: Int) {
        Jzvd.releaseAllVideos()
        player.setUp(jzDataSource, screen, JZMediaIjk::class.java)
        player.startVideo()
    }

    private fun clickChangeToAliyun(player: JZVideoPlayerNew, jzDataSource: JZDataSource, screen: Int) {
        Jzvd.releaseAllVideos()
        player.setUp(jzDataSource, screen, JZMediaAliyun::class.java)
        player.startVideo()
    }

    private fun clickChangeToSystem(player: JZVideoPlayerNew, jzDataSource: JZDataSource, screen: Int) {
        Jzvd.releaseAllVideos()
        player.setUp(jzDataSource, screen, JZMediaSystemSafe::class.java)
        player.startVideo()
    }

    private fun clickChangeToExo(player: JZVideoPlayerNew, jzDataSource: JZDataSource, screen: Int) {
        Jzvd.releaseAllVideos()
        player.setUp(jzDataSource, screen, JZMediaExo::class.java)
        player.startVideo()
    }

    private fun clickChangeToVlc(player: JZVideoPlayerNew, jzDataSource: JZDataSource, screen: Int) {
        Jzvd.releaseAllVideos()
        player.setUp(jzDataSource, screen, JZMediaVlc::class.java)
        player.startVideo()
    }
}
