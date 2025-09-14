package com.walhalla.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.walhalla.data.model.Category;
import com.walhalla.data.model.Channel;
import com.walhalla.data.model.PlaylistImpl;
import com.walhalla.data.model.PlaylistChannelJoin;

import java.util.List;

@Dao
public interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPlaylist(PlaylistImpl playlist);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertChannel(Channel channel);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlaylistChannelJoin(PlaylistChannelJoin join);


    //SQLiteConstraintException @@@ FOREIGN KEY constraint failed (code 787 SQLITE_CONSTRAINT_FOREIGNKEY)

    @Query("DELETE FROM playlists WHERE _id = :playListId")
    int deletePlaylistById(long playListId);

    @Query("SELECT * FROM playlists")
    List<PlaylistImpl> selectAll();

    @Query("SELECT * FROM playlists WHERE _id = :playlistId LIMIT 1")
    PlaylistImpl getPlaylistById(long playlistId);


    @Query("DELETE FROM channel WHERE _id = :channelId")
    void deleteChannelById(long channelId);


    @Query("DELETE FROM channel WHERE _id IN (:channelIds)")
    int deleteChannelsByIds(List<Long> channelIds);

    //@@ playlist_channel_join @@




//    @Query("SELECT * FROM channel WHERE _id IN (SELECT channelId FROM playlist_channel_join WHERE playlistId != :playlistId)")
//    List<Channel> getChannelsNotInPlaylist(long playlistId);


    //the number of deleted rows
    @Query("DELETE FROM playlist_channel_join WHERE playlistId = :playlistId")
    int deletePlaylistChannelJoinByPlaylistId(long playlistId);

    // Удалить все каналы, которые находятся только в удаляемом плейлисте
    @Query("DELETE FROM channel WHERE _id IN (SELECT channelId FROM playlist_channel_join WHERE playlistId = :playlistId AND channelId NOT IN (SELECT channelId FROM playlist_channel_join WHERE playlistId != :playlistId))")
    int deleteChannelsInPlaylistOnly(long playlistId);

    // Удалить все каналы, которые не находятся в других плейлистах
    @Query("DELETE FROM channel WHERE _id NOT IN (SELECT channelId FROM playlist_channel_join)")
    void deleteChannelsNotInAnyPlaylist();


    //============================================================================================

//    @Query("SELECT * FROM channel WHERE _id IN (SELECT channelId FROM playlist_channel_join WHERE playlistId == :playlistId)")
//    List<Channel> getChannelsInPlaylist(long playlistId);

    // По имени в порядке возрастания
    @Query("SELECT * FROM channel WHERE _id IN (SELECT channelId FROM playlist_channel_join WHERE playlistId == :playlistId) ORDER BY name ASC")
    List<Channel> getChannelsInPlaylistByNameAsc(long playlistId);

    // По имени в порядке убывания
    @Query("SELECT * FROM channel WHERE _id IN (SELECT channelId FROM playlist_channel_join WHERE playlistId == :playlistId) ORDER BY name DESC")
    List<Channel> getChannelsInPlaylistByNameDesc(long playlistId);

    // По ID в порядке возрастания
    @Query("SELECT * FROM channel WHERE _id IN (SELECT channelId FROM playlist_channel_join WHERE playlistId == :playlistId) ORDER BY _id ASC")
    List<Channel> getChannelsInPlaylistByIdAsc(long playlistId);

    // По ID в порядке убывания
    @Query("SELECT * FROM channel WHERE _id IN (SELECT channelId FROM playlist_channel_join WHERE playlistId == :playlistId) ORDER BY _id DESC")
    List<Channel> getChannelsInPlaylistByIdDesc(long playlistId);


    //============================================================================================

    @Query("SELECT * FROM channel WHERE _id = :channelId AND _id NOT IN (SELECT channelId FROM playlist_channel_join WHERE playlistId != :playlistId)")
    List<Channel> getUnusedChannels(long channelId, long playlistId);

//    @Query("SELECT DISTINCT category.* FROM category " +
//            "JOIN channel ON category._id = channel.categoryId " +
//            "JOIN playlist_channel_join ON playlist_channel_join.channelId = channel._id " +
//            "WHERE playlist_channel_join.playlistId = :playlistId " +
//            "ORDER BY category.name ASC")
//    List<Category> getCategoriesForPlaylist(long playlistId);

    @Query("SELECT DISTINCT category.* FROM category " +
            "JOIN channel ON category.name = channel.cat " +
            "JOIN playlist_channel_join ON playlist_channel_join.channelId = channel._id " +
            "WHERE playlist_channel_join.playlistId = :playlistId " +
            "ORDER BY category.name ASC")
    List<Category> getCategoriesForPlaylist(long playlistId);
}