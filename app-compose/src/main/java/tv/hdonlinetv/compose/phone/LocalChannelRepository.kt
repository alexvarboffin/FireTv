package tv.hdonlinetv.compose.phone

import androidx.compose.runtime.staticCompositionLocalOf
import tv.hdonlinetv.compose.core.domain.repository.ChannelRepository

val LocalChannelRepository = staticCompositionLocalOf<ChannelRepository> {
    error("ChannelRepository not provided")
}
