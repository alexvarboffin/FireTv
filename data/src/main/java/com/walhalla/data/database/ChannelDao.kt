package com.walhalla.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.walhalla.data.model.Channel


@Dao
interface ChannelDao {
    @get:Query("select * from channel WHERE liked>0")
    val favoriteData: List<Channel>

    @Query(
        ("SELECT channel.* FROM channel " +
                "JOIN playlist_channel_join ON playlist_channel_join.channelId = channel._id " +
                "WHERE playlist_channel_join.playlistId = :playlistId AND channel.liked > 0")
    )
    fun getFavoriteChannelsForPlaylist(playlistId: Long): List<Channel>


    //    @Query("SELECT EXISTS (SELECT 1 FROM favorite WHERE _id=:id)")
    //    int isFavorite(long id);
    //    @Query("SELECT EXISTS (SELECT 1 FROM favorite WHERE tvgId=:tvGid)")
    //    int isFavorite(String tvGid);
    @Query("SELECT 1 FROM channel WHERE _id=:id AND liked>0")
    fun isFavorite(id: Long): Int


    //Set favorite
    @Update
    fun update(entity: Channel): Int


    //    @Query("DELETE FROM channel WHERE tvgId=:tvGid")
    //    void delete(String tvGid);
    //=================================================================
    //Channel
    // @@@@@@@@@@@@@@@@@@@   @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    fun addChannel(channels: List<Channel>): List<Long>

    //========================================================================================
    @Query("SELECT * from channel order by name ASC")
    fun selectAllChannelsByAsc(): List<Channel> //low to hegt

    @Query("SELECT * from channel order by name DESC")
    fun selectAllChannelsByDESC(): List<Channel> //heght to low

    @Query("SELECT * from channel order by name ASC")
    fun selectAllChannelsByIdAsc(): List<Channel> //low to hegt

    @Query("SELECT * from channel order by _id DESC")
    fun selectAllChannelsByIdDESC(): List<Channel> //heght to low


    //========================================================================================
    @Query("SELECT * from channel WHERE LOWER(name) LIKE :queryLower OR LOWER(cat) LIKE :queryLower ORDER BY name ASC")
    fun searchChannelsQuery(queryLower: String): List<Channel>

    @Query("SELECT * from channel WHERE LOWER(cat) LIKE LOWER(:query) ORDER BY name ASC")
    fun getCategory(query: String): List<Channel>

    //    @Query("SELECT * from channel WHERE tvgId=:tvgId ORDER BY name ASC")
    //    Channel getChannelByTvgId(String tvgId);
    @Query("SELECT * from channel WHERE name=:name AND lnk=:lnk ORDER BY name ASC")
    fun getChannelByUrl(name: String, lnk: String): Channel

    @Query("SELECT * FROM channel WHERE _id=:id")
    fun getChannelById(id: Long): Channel

    @Query("SELECT * FROM channel WHERE name IN (:names) AND lnk IN (:lnks) ORDER BY name ASC")
    fun getChannelsByNamesAndLinks(
        names: List<String>,
        lnks: List<String?>
    ): List<Channel>
}
