package tv.hdonlinetv.compose.navigation

import tv.hdonlinetv.compose.core.presentation.player.PlayerBrowseScopeWire

/**
 * Maps app-nav [PlayerBrowseScope] → presentation wire used by PlayerViewModel
 * (`:core:presentation` must not depend on `app-compose`).
 */
fun PlayerBrowseScope.toWire(): PlayerBrowseScopeWire = when (this) {
    PlayerBrowseScope.Favorites -> PlayerBrowseScopeWire.Favorites
    is PlayerBrowseScope.Playlist -> PlayerBrowseScopeWire.Playlist(playlistId)
    is PlayerBrowseScope.Category -> PlayerBrowseScopeWire.Category(name)
    PlayerBrowseScope.All -> PlayerBrowseScopeWire.All
    PlayerBrowseScope.None -> PlayerBrowseScopeWire.None
    PlayerBrowseScope.InferPlaylist -> PlayerBrowseScopeWire.InferPlaylist
}
