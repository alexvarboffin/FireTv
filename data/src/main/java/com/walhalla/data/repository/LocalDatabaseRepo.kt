package com.walhalla.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.room.Room.databaseBuilder
import com.walhalla.data.database.ChannelDao
import com.walhalla.data.database.FavoriteDatabase
import com.walhalla.data.database.PlaylistDao
import com.walhalla.data.model.Category
import com.walhalla.data.model.Channel
import com.walhalla.data.model.PlaylistChannelJoin
import com.walhalla.data.model.PlaylistImpl
import com.walhalla.ui.BuildConfig
import com.walhalla.ui.DLog.d
import com.walhalla.ui.DLog.handleException
import java.util.Locale
import java.util.TreeSet
import kotlin.math.min

class LocalDatabaseRepo private constructor(context: Context) {
    private val db: FavoriteDatabase
    private val context: Context?

    fun getFavorite(playlistId: Long): MutableList<Channel?>? {
        val dao = db.channelDao()
        val list: MutableList<Channel?>?
        if (playlistId > 0) {
            list = dao.getFavoriteChannelsForPlaylist(playlistId)
        } else {
            list = dao.getFavoriteData()
        }
        return list
    }


    //    private List<Channel> getFavoriteChannelsForPlaylist(long playlistId) {
    //        return db.channelDao().getFavoriteChannelsForPlaylist(playlistId);
    //    }
    fun isFavorite(_id: Long): Int {
        return db.channelDao().isFavorite(_id)
    }

    fun addFavorite(channel: Channel): Int {
        try {
            channel.liked = 1
            val m = db.channelDao().update(channel)
            //db.channelDao().addData(channel);
            return m
        } catch (e: Exception) {
            handleException(e)
        }
        return 0
    }

    fun deleteFavorite(channel: Channel) {
        channel.liked = 0
        db.channelDao().update(channel)
        //db.channelDao().delete(tvgId);
    }

    private fun addChannel(result: MutableList<Channel>?): MutableList<Long?> {
        var tmp: MutableList<Long?> = ArrayList<Long?>()
        try {
            tmp = db.channelDao().addChannel(result)
        } catch (e: Exception) {
            Log.d(TAG, "addChannel: " + e.message)
        }
        return tmp
    }

    init {
        this.context = context
        this.db = databaseBuilder<FavoriteDatabase>(
            context.getApplicationContext(), FavoriteDatabase::class.java,
            "datadba"
        ) //.allowMainThreadQueries()
            .build()
    }

    fun putIfAbsent(map: MutableMap<String?, String?>, key: String?, value: String?): String? {
        var v = map.get(key)
        if (v == null) {
            v = map.put(key, value)
        }

        return v
    }

    fun getChannelsByNamesAndLinks(
        dao: ChannelDao,
        names: MutableList<String?>,
        lnks: MutableList<String?>
    ): MutableList<Channel> {
        val result: MutableList<Channel> = ArrayList<Channel>()

        val count = names.size
        var i = 0
        while (i < count) {
            val end = min(count, i + SQLITE_MAX_VARIABLES / 2)
            val subNames = names.subList(i, end)
            val subLnks = lnks.subList(i, end)

            result.addAll(dao.getChannelsByNamesAndLinks(subNames, subLnks))
            i += SQLITE_MAX_VARIABLES / 2
        }

        return result
    }

    fun addXtreamPlaylist(playlist: PlaylistImpl?): Long {
        var playListId: Long = -1
        try {
            playListId = insertPlaylist(playlist)
        } catch (e: Exception) {
            handleException(e)
        }
        return playListId
    }

    fun addChannelAndPlaylist(
        channels: MutableList<Channel>,
        playlist: PlaylistImpl
    ): MutableList<Long?> {
        val tmp: MutableList<Long?> = ArrayList<Long?>()
        try {
            val channelDao = db.channelDao()
            val dao1 = db.playlistDao()

            val newIds0 = channelDao.addChannel(channels)
            newIds1 = ArrayList<Long?>(newIds0)


            if (BuildConfig.DEBUG) {
                //Log.d("@@@->", + oldIds.size() + " - " + oldIds);
                Log.d("@@@0@@@->", newIds0.size.toString() + " - " + newIds0)
            }


            // Создаем Map для хранения уникальных пар name и lnk
            val nameLnkMap: MutableMap<String?, String?> = HashMap<String?, String?>()

            // Проходим по списку каналов и заполняем Map только для тех каналов, которые были проигнорированы при вставке
            for (i in channels.indices) {
                if (newIds0.get(i) == -1L) {
                    val channel = channels.get(i)
                    val key = channel.name
                    val value = channel.lnk

                    // Если ключ уже существует, то пропускаем добавление
                    putIfAbsent(nameLnkMap, key, value)
                }
            }

            val names: MutableList<String?> = ArrayList<String?>(nameLnkMap.keys)
            val lnks: MutableList<String?> = ArrayList<String?>(nameLnkMap.values)

            val existingChannels = getChannelsByNamesAndLinks(channelDao, names, lnks)
            // Создаем Map для быстрого поиска по комбинации name + lnk
            val existingChannelsMap: MutableMap<String?, Long?> = HashMap<String?, Long?>()
            for (channel in existingChannels) {
                val key = channel.name + "|" + channel.lnk
                existingChannelsMap.put(key, channel._id)
            }

            // Обновляем newIds1, заменяя -1 на реальные ID из базы данных
            for (i in channels.indices) {
                if (newIds0.get(i) == -1L) {
                    val channel = channels.get(i)
                    val key = channel.name + "|" + channel.lnk

                    if (existingChannelsMap.containsKey(key)) {
                        newIds1.set(i, existingChannelsMap.get(key))
                    }
                }
            }

            Log.d("@@@1@@@->", newIds0.size.toString() + " - " + newIds0)
            Log.d("@@@1 NEW_IDS @@@->", newIds1.size.toString() + " - " + newIds1)

            val channelCount = channels.size
            playlist.count = channelCount
            val playListId = insertPlaylist(playlist)

            val size: Int = newIds1.size
            for (i in 0..<size) {
                //final Long channel_id = newIds1.get(i);
                if (newIds1.get(i)!! > 0) {
                    try {
                        val join = PlaylistChannelJoin()
                        join.playlistId = playListId
                        join.channelId = newIds1.get(i)!!
                        dao1.insertPlaylistChannelJoin(join)
                    } catch (e: Exception) {
                        Log.d(
                            TAG, ("getChannelsInPlaylist: " + i + ") "
                                    + ", " + newIds1[i])
                        )
                        Log.d(TAG, "getChannelsInPlaylist: " + e.message + " " + newIds1[i])
                    }
                }
            }

            if (BuildConfig.DEBUG) {
                //11692
                d("@@" + channelCount + "@@@ Channel inserted->" + newIds0.size + " " + newIds1.size)
            }
        } catch (e: Exception) {
            Log.d(TAG, "addChannel: " + e.javaClass.simpleName + " " + e.message)
        }
        return tmp
    }

    //    public List<Long> addChannelAndPlaylist(List<Channel> channels, Playlist playlist) {
    //        List<Long> tmp = new ArrayList<>();
    //        try {
    //            ChannelDao dao = db.channelDao();
    //            PlaylistDao dao1 = db.playlistDao();
    //
    //            final List<Long> newIds0 = dao.addData(channels);
    //            newIds1 = new ArrayList<>(newIds0);
    //
    //
    //            //Log.d("@@@->", + oldIds.size() + " - " + oldIds);
    //            Log.d("=============@@@0@@@->", newIds0.size() + " - " + newIds0);
    //
    //            for (int i = 0; i < newIds0.size(); i++) {
    //                Channel channel = channels.get(i);
    //                Long newId = newIds0.get(i);
    //
    //                //-1 - если вернулся -1 вставка в RoomDb проигнорирована так как запись с таким именем уже в базе
    //
    //                if (newId == -1) {
    //                    String tvGid = channel.getTvgId();
    //                    // Если вставка была проигнорирована, получаем ID канала по уникальному атрибуту
    //                    Channel mm = dao.getChannelByUrl(channel.getName(), channel.getLnk());
    //
    //                    if (mm != null) {
    //                        Log.d(TAG, "==> [" + tvGid + "] @@@@" + channel._id + "@@@" + mm.getLnk());
    /*                       if (mm._id == -1)
    {
        * /                            Log.d(TAG, "==> "+tvGid+"@@@@"
        * /+mm.toString());
        * /
    } */
    //                        newIds1.set(i, mm._id);
    //                    }
    //                } else {
    //                    newIds1.set(i, newId);
    //                }
    //            }
    //
    //            Log.d("@@@1@@@->", newIds0.size() + " - " + newIds0);
    //            Log.d("@@@1@@@->", newIds1.size() + " - " + newIds1);
    //
    //            int channelCount = channels.size();
    //            playlist.setCount(channelCount);
    //            long playListId = insertPlaylist(playlist);
    //
    //            final int size = newIds1.size();
    //            for (int i = 0; i < size; i++) {
    //                //final Long channel_id = newIds1.get(i);
    //                if (newIds1.get(i) > 0) {
    //                    try {
    //                        PlaylistChannelJoin join = new PlaylistChannelJoin();
    //                        join.playlistId = playListId;
    //                        join.channelId = newIds1.get(i);
    //                        dao1.insertPlaylistChannelJoin(join);
    //                    } catch (Exception e) {
    //
    //                        Log.d(TAG, "getChannelsInPlaylist: " + i + ") "
    //                                + ", " + newIds1.get(i));
    //                        Log.d(TAG, "getChannelsInPlaylist: " + e.getMessage() + " " + newIds1.get(i));
    //                    }
    //                }
    //            }
    //
    //            Log.d("@@@", "@@" + channelCount);
    //        } catch (Exception e) {
    //            Log.d(TAG, "addChannel: " + e.getClass().getSimpleName());
    //        }
    //        return tmp;
    //    }
    fun getAllChannels(sortOption: Int): MutableList<Channel?> {
        var tmp: MutableList<Channel?>
        try {
            if (sortOption == 0) {
                tmp = db.channelDao().selectAllChannelsByAsc()
            } else if (sortOption == 1) {
                tmp = db.channelDao().selectAllChannelsByDESC()
            } else if (sortOption == 2) {
                tmp = db.channelDao().selectAllChannelsByIdAsc()
            } else if (sortOption == 3) {
                tmp = db.channelDao().selectAllChannelsByIdDESC()
            } else {
                // По умолчанию сортировка по имени по возрастанию
                tmp = db.channelDao().selectAllChannelsByAsc()
            }
        } catch (e: Exception) {
            d("@@@ getAllChannels: " + e.message)
            tmp = ArrayList<Channel?>()
        }

        if (BuildConfig.DEBUG) {
            d("@@@ Items count=>" + tmp.size)
        }
        return tmp
    }

    val allCategories: MutableList<Category>?
        get() {
            try {
                return db.categoryDao().selectAllCategories()
            } catch (e: Exception) {
                Log.d(TAG, "getAllChannels: " + e.message)
            }
            return ArrayList<Category>()
        }

    fun searchChannel(raw: String): MutableList<Channel?>? {
        //query% - start with
        //%query

        try {
            val query = "%" + raw.lowercase(Locale.getDefault()) + "%"

            //String query = "" + raw + "";
            val tmp = db.channelDao().searchChannelsQuery(query)
            //            for (Channel channel : tmp) {
//                Log.d(TAG, "searchChannel: "+channel.getName());
//            }
            Log.d(TAG, "searchChannel: $query")
            return tmp
        } catch (e: Exception) {
            Log.d(TAG, "getAllChannels: " + e.message)
        }
        return ArrayList<Channel?>()
    }

    fun addCategory(categories: MutableList<Category>) {
        try {
            val res: List<Long> = db.categoryDao().addData(categories)
            Log.d(TAG, "addCategory: " + categories.size + "::->" + res.size + "::" + res)
        } catch (e: Exception) {
            Log.d(TAG, "addChannel: " + e.message)
        }
    }

    fun getCategory(categoryName: String): MutableList<Channel?>? {
        try {
            val query = "%$categoryName%"
            val res = db.channelDao().getCategory(query)
            return res
        } catch (e: Exception) {
            Log.d(TAG, "addChannel: " + e.message)
        }
        return ArrayList<Channel?>()
    }

    fun deletePlaylistAndRelatedChannels(playlist: PlaylistImpl): Int {
        val playlistId = playlist._id
        val playlistDao = db.playlistDao()
        var res2 = -1
        try {       // Удалите связи плейлиста с каналами
            playlistDao.deletePlaylistChannelJoinByPlaylistId(playlistId)
            // Удалите сам плейлист
            res2 = playlistDao.deletePlaylistById(playlistId)

            // Удалите каналы, которые не используются в других плейлистах
            playlistDao.deleteChannelsNotInAnyPlaylist()
            return res2
        } catch (e: Exception) {
            handleException(e)
            Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
        }
        return 0
    }

    //    public int deletePlaylist(Playlist playlist) {
    //        long playListId = playlist._id;
    //        PlaylistDao playlistDao = db.playlistDao();
    //        int res2 = -1;
    //        try {
    //
    //            // Удалите каналы, которые использовались только в этом плейлисте
    //            int m = playlistDao.deleteChannelsInPlaylistOnly(playListId);
    //
    //            DLog.d("@@@// Удалено неиспользуемые каналы" + m);
    //            clearChannelJoin(playListId, playlistDao);
    //
    //            // Получить все каналы, которые не используются в других плейлистах
    //            List<Channel> channelsToDelete0 = playlistDao.getChannelsNotInPlaylist(playListId);
    /*            for (Channel channel : channelsToDelete0)
    {
        * /                DLog.d("@@@// Удалить неиспользуемые каналы"+channel.getName());
        * /                playlistDao.deleteChannelById(channel._id);
        * /
    } */
    //            List<Long> channelIdsToDelete = new ArrayList<>();
    //            for (Channel channel : channelsToDelete0) {
    //                channelIdsToDelete.add(channel._id);
    //                DLog.d("@@@// Удалить неиспользуемые каналы" + channel.getName());
    //            }
    //            // Удалите все каналы по их идентификаторам за один запрос
    /*            if (!channelIdsToDelete.isEmpty())
    {
        * /                int m = playlistDao.deleteChannelsByIds(channelIdsToDelete);
        * /                DLog.d("@@@// Удалено неиспользуемые каналы"+m);
        * /
    } */
    //            res2 = playlistDao.deletePlaylistById(playListId);
    //            DLog.d("@@Теперь удалить сам плейлист == the number of deleted rows@" + res2);
    //            return res2;
    //        } catch (Exception e) {
    //            DLog.handleException(e);
    //            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
    //        }
    //        return 0;
    //    }
    private fun clearChannelJoin(playListId: Long, playlistDao: PlaylistDao) {
//        List<Channel> mm = playlistDao.getChannelsInPlaylist(playListId);
//        DLog.d("@@@@" + mm.size());//10978
        // Удалить все записи из playlist_channel_join, связанные с данным плейлистом
        val res0 = playlistDao.deletePlaylistChannelJoinByPlaylistId(playListId)
        d("@@@@the number of deleted rows$res0") //10978
    }

    fun insertPlaylist(playlist: PlaylistImpl?): Long {
        val playlistDao = db.playlistDao()
        return playlistDao.insertPlaylist(playlist)
    }

    fun selectAllPlaylist(): MutableList<PlaylistImpl?>? {
        try {
            val playlistDao = db.playlistDao()
            return playlistDao.selectAll()
        } catch (e: Exception) {
            Log.d(TAG, "selectAllPlaylist: " + e.message)
        }
        return ArrayList<PlaylistImpl?>()
    }

    fun getChannelsInPlaylist(id: Long, sortOption: Int): MutableList<Channel?>? {
        try {
            val playlistDao = db.playlistDao()
            when (sortOption) {
                0 -> return playlistDao.getChannelsInPlaylistByNameAsc(id)
                1 -> return playlistDao.getChannelsInPlaylistByNameDesc(id)
                2 -> return playlistDao.getChannelsInPlaylistByIdAsc(id)
                3 -> return playlistDao.getChannelsInPlaylistByIdDesc(id)
                else ->                     // По умолчанию сортировка по имени по возрастанию
                    return playlistDao.getChannelsInPlaylistByNameAsc(id)
            }
        } catch (e: Exception) {
            handleException(e)
        }
        return ArrayList<Channel?>()
    }

    //11692
    //10939
    fun getCategoriesForPlaylist(playlistId: Long): MutableList<Category?>? {
        var tmp: MutableList<Category?> = ArrayList<Category?>()
        try {
            val playlistDao = db.playlistDao()
            tmp = playlistDao.getCategoriesForPlaylist(playlistId)
            Log.d(TAG, "CategoriesForPlaylist: " + tmp.size)
        } catch (e: Exception) {
            Log.d(TAG, "CategoriesForPlaylist: " + e.message)
        }
        return tmp
    }

    fun addCategory(categorySet: TreeSet<String>) {
        val categories: MutableList<Category> = ArrayList<Category>()
        for (catName in categorySet) {
            val c = Category()
            c.name = if (TextUtils.isEmpty(catName)) "Undefined" else catName
            //c.desc = desc;
            //c.thumb = thumb;
            categories.add(c)
            d("[category] -> " + c.name)
        }
        addCategory(categories)
    }

    fun getChannelById(id: Long): Channel? {
        var list: Channel?
        try {
            val dao = db.channelDao()
            list = dao.getChannelById(id)
            return list
        } catch (e: Exception) {
            handleException(e)
            list = Channel()
        }
        return list
    }
    //FOREIGN KEY constraint failed (code 787 SQLITE_CONSTRAINT_FOREIGNKEY)
    //    public void insertPlaylistChannelJoin(long playlistId, List<Channel> channels) {
    //        PlaylistDao playlistDao = db.playlistDao();
    //
    //        // Проверка существования плейлиста
    /*        Playlist playlist = playlistDao.getPlaylistById(playlistId);
    * /        if (playlist == null)
    {
        * /            Log.d(TAG, "@@@@insertPlaylistChannelJoin: Playlist with ID "+playlistId+" does not exist.");
        * /
        * /
    } else
    {
        * /            Log.d(TAG, "insertPlaylistChannelJoin: "+playlist._id+"@@@"+playlistId);
        * /
    } */
    //
    //        for (Channel channel : channels) {
    //            if (channel._id > 0) {
    //                try {
    //                    PlaylistChannelJoin join = new PlaylistChannelJoin();
    //                    join.playlistId = playlistId;
    //                    join.channelId = channel._id;
    //                    playlistDao.insertPlaylistChannelJoin(join);
    //                } catch (Exception e) {
    //                    Log.d(TAG, "getChannelsInPlaylist: " + e.getMessage());
    //                }
    //            }
    //        }
    //    }


    companion object {
        private const val TAG = "@@@"
        private const val SQLITE_MAX_VARIABLES = 999


        @SuppressLint("StaticFieldLeak")
        private var instance: LocalDatabaseRepo? = null

        private val LOCK = Any()

        @JvmStatic
        @Synchronized
        fun getStoreInfoDatabase(context: Context): LocalDatabaseRepo {
            if (instance == null) {
                synchronized(LOCK) {
                    if (instance == null) {
                        instance = LocalDatabaseRepo(context)
                    }
                }
            }
            return instance!!
        }


        var newIds1: MutableList<Long?> = ArrayList<Long?>()
    }
}