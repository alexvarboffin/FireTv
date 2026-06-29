package tv.hdonlinetv.compose.phone

import androidx.compose.runtime.staticCompositionLocalOf
import tv.hdonlinetv.compose.core.domain.repository.PlaylistRepository
import tv.hdonlinetv.compose.core.domain.repository.SettingsRepository

val LocalPlaylistRepository = staticCompositionLocalOf<PlaylistRepository> {
    error("PlaylistRepository not provided")
}

val LocalSettingsRepository = staticCompositionLocalOf<SettingsRepository> {
    error("SettingsRepository not provided")
}

val LocalXtreamRepository = staticCompositionLocalOf<tv.hdonlinetv.compose.core.domain.repository.XtreamRepository> {
    error("XtreamRepository not provided")
}

val LocalGridColumns = staticCompositionLocalOf { 3 }
