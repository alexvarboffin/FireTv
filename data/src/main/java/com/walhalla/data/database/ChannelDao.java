package com.walhalla.data.database;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.walhalla.data.model.Channel;

import java.util.List;

@Dao
public interface ChannelDao {

//    @Insert
//    void addData(Channel favoriteList);

//    @Delete
//    void delete(Channel favoriteList);


    @Query("select * from channel WHERE liked>0")
    List<Channel> getFavoriteData();

    @Query("SELECT channel.* FROM channel " +
            "JOIN playlist_channel_join ON playlist_channel_join.channelId = channel._id " +
            "WHERE playlist_channel_join.playlistId = :playlistId AND channel.liked > 0")
    List<Channel> getFavoriteChannelsForPlaylist(long playlistId);

//    @Query("SELECT EXISTS (SELECT 1 FROM favorite WHERE _id=:id)")
//    int isFavorite(long id);

//    @Query("SELECT EXISTS (SELECT 1 FROM favorite WHERE tvgId=:tvGid)")
//    int isFavorite(String tvGid);


    @Query("SELECT 1 FROM channel WHERE _id=:id AND liked>0")
    int isFavorite(long id);


    //Set favorite
    @Update
    int update(Channel entity);

//    @Query("DELETE FROM channel WHERE tvgId=:tvGid")
//    void delete(String tvGid);


    //=================================================================

    //Channel

    // @@@@@@@@@@@@@@@@@@@   @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    List<Long> addChannel(List<Channel> channels);

    //========================================================================================

    @Query("SELECT * from channel order by name ASC")
    List<Channel> selectAllChannelsByAsc();//low to hegt

    @Query("SELECT * from channel order by name DESC")
    List<Channel> selectAllChannelsByDESC();//heght to low

    @Query("SELECT * from channel order by name ASC")
    List<Channel> selectAllChannelsByIdAsc();//low to hegt

    @Query("SELECT * from channel order by _id DESC")
    List<Channel> selectAllChannelsByIdDESC();//heght to low


    //========================================================================================



    @Query("SELECT * from channel WHERE LOWER(name) LIKE :queryLower OR LOWER(cat) LIKE :queryLower ORDER BY name ASC")
    List<Channel> searchChannelsQuery(String queryLower);

    @Query("SELECT * from channel WHERE LOWER(cat) LIKE LOWER(:query) ORDER BY name ASC")
    List<Channel> getCategory(String query);

//    @Query("SELECT * from channel WHERE tvgId=:tvgId ORDER BY name ASC")
//    Channel getChannelByTvgId(String tvgId);

    @Query("SELECT * from channel WHERE name=:name AND lnk=:lnk ORDER BY name ASC")
    Channel getChannelByUrl(String name, String lnk);

    @Query("SELECT * FROM channel WHERE _id=:id")
    Channel getChannelById(long id);

    @Query("SELECT * FROM channel WHERE name IN (:names) AND lnk IN (:lnks) ORDER BY name ASC")
    List<Channel> getChannelsByNamesAndLinks(List<String> names, List<String> lnks);


}
