package com.walhalla.data.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "playlist_channel_join",
    primaryKeys = ["playlistId", "channelId"],
    foreignKeys = [ForeignKey(
        entity = PlaylistImpl::class,
        parentColumns = arrayOf("_id"),
        childColumns = arrayOf("playlistId"),
        onUpdate = ForeignKey.Companion.NO_ACTION,
        onDelete = ForeignKey.Companion.CASCADE
    ), ForeignKey(
        entity = Channel::class,
        parentColumns = arrayOf("_id"),
        childColumns = arrayOf("channelId"),
        onUpdate = ForeignKey.Companion.NO_ACTION,
        onDelete = ForeignKey.Companion.CASCADE
    )]
)
class PlaylistChannelJoin {
    var playlistId: Long = 0
    var channelId: Long = 0
}