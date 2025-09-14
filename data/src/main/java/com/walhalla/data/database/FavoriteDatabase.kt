package com.walhalla.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.walhalla.data.model.Category
import com.walhalla.data.model.Channel
import com.walhalla.data.model.PlaylistChannelJoin
import com.walhalla.data.model.PlaylistImpl


@Database(
    entities = [PlaylistImpl::class, Channel::class, Category::class, PlaylistChannelJoin::class],
    version = 1
)
abstract class FavoriteDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun categoryDao(): CategoryDao

    abstract fun playlistDao(): PlaylistDao
}
