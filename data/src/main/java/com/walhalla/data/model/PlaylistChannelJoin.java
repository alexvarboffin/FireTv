package com.walhalla.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "playlist_channel_join",
        primaryKeys = { "playlistId", "channelId" },
        foreignKeys = {
                @ForeignKey(
                        entity = PlaylistImpl.class,
                        parentColumns = "_id",
                        childColumns = "playlistId",
                        onUpdate = ForeignKey.NO_ACTION,
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(entity = Channel.class,
                        parentColumns = "_id",
                        childColumns = "channelId",
                        onUpdate = ForeignKey.NO_ACTION,
                        onDelete = ForeignKey.CASCADE
                )
        })
public class PlaylistChannelJoin {
    public long playlistId;
    public long channelId;
}