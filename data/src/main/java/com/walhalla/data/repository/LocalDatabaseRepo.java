package com.walhalla.data.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.room.Room;

import com.walhalla.data.database.ChannelDao;
import com.walhalla.data.database.FavoriteDatabase;
import com.walhalla.data.database.PlaylistDao;
import com.walhalla.data.model.Category;
import com.walhalla.data.model.Channel;
import com.walhalla.data.model.PlaylistImpl;
import com.walhalla.data.model.PlaylistChannelJoin;
import com.walhalla.ui.BuildConfig;
import com.walhalla.ui.DLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

class LocalDatabaseRepo {

    private static final String TAG = "@@@";
    private static final int SQLITE_MAX_VARIABLES = 999;


    @SuppressLint("StaticFieldLeak")
    private static LocalDatabaseRepo instance;

    private static final Object LOCK = new Object();

    private final FavoriteDatabase db;
    private final Context context;

    private LocalDatabaseRepo(Context context) {
        this.context = context;
        this.db = Room.databaseBuilder(context.getApplicationContext(), FavoriteDatabase.class,
                        "datadba")
                //.allowMainThreadQueries()
                .build();
    }

    public synchronized static LocalDatabaseRepo getStoreInfoDatabase(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new LocalDatabaseRepo(context);
                }
            }
        }
        return instance;
    }


    public List<Channel> getFavorite(long playlistId) {
        ChannelDao dao = db.channelDao();
        List<Channel> list;
        if (playlistId > 0) {
            list = dao.getFavoriteChannelsForPlaylist(playlistId);
        } else {
            list = dao.getFavoriteData();
        }
        return list;
    }


//    private List<Channel> getFavoriteChannelsForPlaylist(long playlistId) {
//        return db.channelDao().getFavoriteChannelsForPlaylist(playlistId);
//    }

    public int isFavorite(long _id) {
        return db.channelDao().isFavorite(_id);
    }

    public int addFavorite(Channel channel) {
        try {
            channel.liked = 1;
            int m = db.channelDao().update(channel);
            //db.channelDao().addData(channel);
            return m;
        } catch (Exception e) {
            DLog.handleException(e);
        }
        return 0;
    }

    public void deleteFavorite(Channel channel) {
        channel.liked = 0;
        db.channelDao().update(channel);
        //db.channelDao().delete(tvgId);
    }

    private List<Long> addChannel(List<Channel> result) {
        List<Long> tmp = new ArrayList<>();
        try {
            tmp = db.channelDao().addChannel(result);
        } catch (Exception e) {
            Log.d(TAG, "addChannel: " + e.getMessage());
        }
        return tmp;
    }

    static List<Long> newIds1 = new ArrayList<>();


    String putIfAbsent(Map<String, String> map, String key, String value) {
        String v = map.get(key);
        if (v == null) {
            v = map.put(key, value);
        }

        return v;
    }

    public List<Channel> getChannelsByNamesAndLinks(ChannelDao dao, List<String> names, List<String> lnks) {
        List<Channel> result = new ArrayList<>();

        int count = names.size();
        for (int i = 0; i < count; i += SQLITE_MAX_VARIABLES / 2) {
            int end = Math.min(count, i + SQLITE_MAX_VARIABLES / 2);
            List<String> subNames = names.subList(i, end);
            List<String> subLnks = lnks.subList(i, end);

            result.addAll(dao.getChannelsByNamesAndLinks(subNames, subLnks));
        }

        return result;
    }

    public long addXtreamPlaylist(PlaylistImpl playlist) {
        long playListId = -1;
        try {
            playListId = insertPlaylist(playlist);
        } catch (Exception e) {
            DLog.handleException(e);
        }
        return playListId;
    }

    public List<Long> addChannelAndPlaylist(List<Channel> channels, PlaylistImpl playlist) {
        List<Long> tmp = new ArrayList<>();
        try {
            ChannelDao channelDao = db.channelDao();
            PlaylistDao dao1 = db.playlistDao();

            final List<Long> newIds0 = channelDao.addChannel(channels);
            newIds1 = new ArrayList<>(newIds0);


            if (BuildConfig.DEBUG) {
                //Log.d("@@@->", + oldIds.size() + " - " + oldIds);
                Log.d("@@@0@@@->", newIds0.size() + " - " + newIds0);
            }


            // Создаем Map для хранения уникальных пар name и lnk
            Map<String, String> nameLnkMap = new HashMap<>();

            // Проходим по списку каналов и заполняем Map только для тех каналов, которые были проигнорированы при вставке
            for (int i = 0; i < channels.size(); i++) {
                if (newIds0.get(i) == -1) {
                    Channel channel = channels.get(i);
                    String key = channel.name;
                    String value = channel.lnk;

                    // Если ключ уже существует, то пропускаем добавление
                    putIfAbsent(nameLnkMap, key, value);
                }
            }

            List<String> names = new ArrayList<>(nameLnkMap.keySet());
            List<String> lnks = new ArrayList<>(nameLnkMap.values());

            List<Channel> existingChannels = getChannelsByNamesAndLinks(channelDao, names, lnks);
            // Создаем Map для быстрого поиска по комбинации name + lnk
            Map<String, Long> existingChannelsMap = new HashMap<>();
            for (Channel channel : existingChannels) {
                String key = channel.name + "|" + channel.lnk;
                existingChannelsMap.put(key, channel._id);
            }

            // Обновляем newIds1, заменяя -1 на реальные ID из базы данных
            for (int i = 0; i < channels.size(); i++) {
                if (newIds0.get(i) == -1) {
                    Channel channel = channels.get(i);
                    String key = channel.name + "|" + channel.lnk;

                    if (existingChannelsMap.containsKey(key)) {
                        newIds1.set(i, existingChannelsMap.get(key));
                    }
                }
            }

            Log.d("@@@1@@@->", newIds0.size() + " - " + newIds0);
            Log.d("@@@1 NEW_IDS @@@->", newIds1.size() + " - " + newIds1);

            int channelCount = channels.size();
            playlist.setCount(channelCount);
            long playListId = insertPlaylist(playlist);

            final int size = newIds1.size();
            for (int i = 0; i < size; i++) {
                //final Long channel_id = newIds1.get(i);
                if (newIds1.get(i) > 0) {
                    try {
                        PlaylistChannelJoin join = new PlaylistChannelJoin();
                        join.playlistId = playListId;
                        join.channelId = newIds1.get(i);
                        dao1.insertPlaylistChannelJoin(join);
                    } catch (Exception e) {

                        Log.d(TAG, "getChannelsInPlaylist: " + i + ") "
                                + ", " + newIds1.get(i));
                        Log.d(TAG, "getChannelsInPlaylist: " + e.getMessage() + " " + newIds1.get(i));
                    }
                }
            }

            if (BuildConfig.DEBUG) {
                //11692
                DLog.d("@@" + channelCount + "@@@ Channel inserted->" + newIds0.size() + " " + newIds1.size());
            }

        } catch (Exception e) {
            Log.d(TAG, "addChannel: " + e.getClass().getSimpleName() + " " + e.getMessage());
        }
        return tmp;
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
////                        if (mm._id == -1) {
////                            Log.d(TAG, "==> " + tvGid + "@@@@"
////                                    + mm.toString());
////                        }
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

    public List<Channel> getAllChannels(int sortOption) {
        List<Channel> tmp;
        try {
            if (sortOption == 0) {
                tmp = db.channelDao().selectAllChannelsByAsc();
            } else if (sortOption == 1) {
                tmp = db.channelDao().selectAllChannelsByDESC();
            } else if (sortOption == 2) {
                tmp = db.channelDao().selectAllChannelsByIdAsc();
            } else if (sortOption == 3) {
                tmp = db.channelDao().selectAllChannelsByIdDESC();
            } else {
                // По умолчанию сортировка по имени по возрастанию
                tmp = db.channelDao().selectAllChannelsByAsc();
            }
        } catch (Exception e) {
            DLog.d("@@@ getAllChannels: " + e.getMessage());
            tmp = new ArrayList<>();
        }

        if (BuildConfig.DEBUG) {
            DLog.d("@@@ Items count=>" + tmp.size());
        }
        return tmp;
    }

    public List<Category> getAllCategories() {
        try {
            return db.categoryDao().selectAllCategories();
        } catch (Exception e) {
            Log.d(TAG, "getAllChannels: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public List<Channel> searchChannel(String raw) {

        //query% - start with
        //%query

        try {
            String query = "%" + raw.toLowerCase() + "%";
            //String query = "" + raw + "";

            List<Channel> tmp = db.channelDao().searchChannelsQuery(query);
//            for (Channel channel : tmp) {
//                Log.d(TAG, "searchChannel: "+channel.getName());
//            }
            Log.d(TAG, "searchChannel: " + query);
            return tmp;
        } catch (Exception e) {
            Log.d(TAG, "getAllChannels: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public void addCategory(List<Category> categories) {
        try {
            List<Long> res = db.categoryDao().addData(categories);
            Log.d(TAG, "addCategory: " + categories.size() + "::->" + res.size() + "::" + res);
        } catch (Exception e) {
            Log.d(TAG, "addChannel: " + e.getMessage());
        }
    }

    public List<Channel> getCategory(String categoryName) {
        try {
            String query = "%" + categoryName + "%";
            List<Channel> res = db.channelDao().getCategory(query);
            return res;
        } catch (Exception e) {
            Log.d(TAG, "addChannel: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public int deletePlaylistAndRelatedChannels(PlaylistImpl playlist) {
        long playlistId = playlist._id;
        PlaylistDao playlistDao = db.playlistDao();
        int res2 = -1;
        try {       // Удалите связи плейлиста с каналами
            playlistDao.deletePlaylistChannelJoinByPlaylistId(playlistId);
            // Удалите сам плейлист
            res2 = playlistDao.deletePlaylistById(playlistId);

            // Удалите каналы, которые не используются в других плейлистах
            playlistDao.deleteChannelsNotInAnyPlaylist();
            return res2;
        } catch (Exception e) {
            DLog.handleException(e);
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return 0;
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
////            for (Channel channel : channelsToDelete0) {
////                DLog.d("@@@// Удалить неиспользуемые каналы" + channel.getName());
////                playlistDao.deleteChannelById(channel._id);
////            }
//            List<Long> channelIdsToDelete = new ArrayList<>();
//            for (Channel channel : channelsToDelete0) {
//                channelIdsToDelete.add(channel._id);
//                DLog.d("@@@// Удалить неиспользуемые каналы" + channel.getName());
//            }
//            // Удалите все каналы по их идентификаторам за один запрос
////            if (!channelIdsToDelete.isEmpty()) {
////                int m = playlistDao.deleteChannelsByIds(channelIdsToDelete);
////                DLog.d("@@@// Удалено неиспользуемые каналы" + m);
////            }
//            res2 = playlistDao.deletePlaylistById(playListId);
//            DLog.d("@@Теперь удалить сам плейлист == the number of deleted rows@" + res2);
//            return res2;
//        } catch (Exception e) {
//            DLog.handleException(e);
//            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//        return 0;
//    }

    private void clearChannelJoin(long playListId, PlaylistDao playlistDao) {
//        List<Channel> mm = playlistDao.getChannelsInPlaylist(playListId);
//        DLog.d("@@@@" + mm.size());//10978
        // Удалить все записи из playlist_channel_join, связанные с данным плейлистом
        int res0 = playlistDao.deletePlaylistChannelJoinByPlaylistId(playListId);
        DLog.d("@@@@the number of deleted rows" + res0);//10978
    }

    public long insertPlaylist(PlaylistImpl playlist) {
        PlaylistDao playlistDao = db.playlistDao();
        return playlistDao.insertPlaylist(playlist);
    }

    public List<PlaylistImpl> selectAllPlaylist() {
        try {
            PlaylistDao playlistDao = db.playlistDao();
            return playlistDao.selectAll();
        } catch (Exception e) {
            Log.d(TAG, "selectAllPlaylist: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public List<Channel> getChannelsInPlaylist(long id, int sortOption) {
        try {
            PlaylistDao playlistDao = db.playlistDao();
            switch (sortOption) {
                case 0:
                    return playlistDao.getChannelsInPlaylistByNameAsc(id);
                case 1:
                    return playlistDao.getChannelsInPlaylistByNameDesc(id);
                case 2:
                    return playlistDao.getChannelsInPlaylistByIdAsc(id);
                case 3:
                    return playlistDao.getChannelsInPlaylistByIdDesc(id);
                default:
                    // По умолчанию сортировка по имени по возрастанию
                    return playlistDao.getChannelsInPlaylistByNameAsc(id);
            }
        } catch (Exception e) {
            DLog.handleException(e);
        }
        return new ArrayList<>();
    }

    //11692
    //10939

    public List<Category> getCategoriesForPlaylist(long playlistId) {
        List<Category> tmp = new ArrayList<>();
        try {
            PlaylistDao playlistDao = db.playlistDao();
            tmp = playlistDao.getCategoriesForPlaylist(playlistId);
            Log.d(TAG, "CategoriesForPlaylist: " + tmp.size());
        } catch (Exception e) {
            Log.d(TAG, "CategoriesForPlaylist: " + e.getMessage());
        }
        return tmp;
    }

    public void addCategory(TreeSet<String> categorySet) {
        List<Category> categories = new ArrayList<>();
        for (String catName : categorySet) {
            Category c = new Category();
            c.name = TextUtils.isEmpty(catName) ? "Undefined" : catName;
            //c.desc = desc;
            //c.thumb = thumb;
            categories.add(c);
            DLog.d("[category] -> " + c.name);
        }
        addCategory(categories);
    }

    public Channel getChannelById(long id) {
        Channel list;
        try {
            ChannelDao dao = db.channelDao();
            list = dao.getChannelById(id);
            return list;
        } catch (Exception e) {
            DLog.handleException(e);
            list = new Channel();
        }
        return list;
    }


    //FOREIGN KEY constraint failed (code 787 SQLITE_CONSTRAINT_FOREIGNKEY)

//    public void insertPlaylistChannelJoin(long playlistId, List<Channel> channels) {
//        PlaylistDao playlistDao = db.playlistDao();
//
//        // Проверка существования плейлиста
////        Playlist playlist = playlistDao.getPlaylistById(playlistId);
////        if (playlist == null) {
////            Log.d(TAG, "@@@@insertPlaylistChannelJoin: Playlist with ID " + playlistId + " does not exist.");
////
////        } else {
////            Log.d(TAG, "insertPlaylistChannelJoin: " + playlist._id + "@@@" + playlistId);
////        }
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


}