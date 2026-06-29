package tv.hdonlinetv.compose.core.databridge.playlist

import com.walhalla.data.model.PlaylistImpl
import tv.hdonlinetv.compose.core.domain.model.PlaylistType
import tv.hdonlinetv.compose.core.domain.model.PlaylistUi

object PlaylistMapper {

    fun toUi(playlist: PlaylistImpl): PlaylistUi = PlaylistUi(
        id = playlist._id,
        title = playlist.title,
        fileName = playlist.fileName,
        count = playlist.count,
        type = playlist.type,
        autoUpdate = playlist.autoUpdate,
        importDate = playlist.importDate,
        updateDate = playlist.updateDate,
    )

    fun toUiList(playlists: List<PlaylistImpl>): List<PlaylistUi> =
        playlists.map { toUi(it) }

    fun toData(ui: PlaylistUi): PlaylistImpl {
        val playlist = PlaylistImpl(
            ui.title,
            ui.fileName,
            ui.importDate,
            ui.count,
            ui.autoUpdate,
            ui.type,
        )
        playlist._id = ui.id
        playlist.updateDate = ui.updateDate
        return playlist
    }

    fun newCloudPlaylist(title: String, url: String): PlaylistImpl = PlaylistImpl(
        title,
        url,
        System.currentTimeMillis(),
        -1,
        true,
        PlaylistType.M3U_CLOUD,
    )
}
