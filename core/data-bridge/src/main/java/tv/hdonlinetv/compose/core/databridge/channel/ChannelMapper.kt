package tv.hdonlinetv.compose.core.databridge.channel

import com.walhalla.data.model.Channel
import tv.hdonlinetv.compose.core.domain.model.ChannelUi

object ChannelMapper {

    fun toUi(channel: Channel, isFavorite: Boolean = channel.liked == 1): ChannelUi = ChannelUi(
        id = channel._id,
        name = channel.name,
        cover = channel.cover,
        category = channel.cat,
        link = channel.lnk,
        desc = channel.desc,
        isFavorite = isFavorite,
        extUserAgent = channel.extUserAgent,
        extReferer = channel.extReferer,
        ua = channel.ua,
    )

    fun toUiList(channels: List<Channel>): List<ChannelUi> =
        channels.map { toUi(it) }

    fun toData(ui: ChannelUi): Channel {
        val channel = Channel()
        channel._id = ui.id
        channel.name = ui.name
        channel.cover = ui.cover
        channel.cat = ui.category
        channel.lnk = ui.link
        channel.desc = ui.desc
        channel.liked = if (ui.isFavorite) 1 else 0
        channel.extUserAgent = ui.extUserAgent
        channel.extReferer = ui.extReferer
        channel.ua = ui.ua
        return channel
    }
}
