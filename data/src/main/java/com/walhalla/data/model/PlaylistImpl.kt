package com.walhalla.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "playlists")
class PlaylistImpl(
    //            , String serverUrl//            , String serverUrl
    //            , long importDate
    //            , int count
    //            , boolean autoUpdate
    //            , int type) {
    //        this.title = title;
    //        this.fileName = serverUrl;
    //        this.importDate = importDate;
    //        this.count = count;
    //        this.autoUpdate = autoUpdate;
    //        this.type = type;
    //    }
    //    public Playlist(String name) {
    //        this.name = name;
    //    }
    @JvmField @field:ColumnInfo(name = "title") var title: String, //private final String info;
    @field:ColumnInfo(name = "file_name") private var fileName: String?, @field:ColumnInfo(
        name = "import_date"
    ) private var importDate: Long,
    @field:ColumnInfo(name = "count") var count: Int,
    @field:ColumnInfo(name = "auto_update") private var autoUpdate: Boolean,
    @JvmField @field:ColumnInfo(name = "type") var type: Int
) {
    @JvmField
    @PrimaryKey(autoGenerate = true)
    var _id: Long = 0

    @ColumnInfo(name = "update_date")
    var updateDate: Long = 0


    //    	        "_id": "61c40fdd-a31b-46f2-8190-26fd5cee178f",
    //                "title": "aaaaaa",
    //                "password": "12",
    //                "username": "12",
    //                "serverUrl": "http://iptv.icsnleb.com:25461/",
    //                "importDate": "2024-10-07T09:49:21.468Z"
    //    public PlaylistImpl(@NonNull String title
    /*            , String password
    * /            , String username */
    //            , String serverUrl
    //            , long importDate
    //            , int count
    //            , boolean autoUpdate
    //            , int type) {
    //        this.title = title;
    //        this.fileName = serverUrl;
    //        this.importDate = importDate;
    //        this.count = count;
    //        this.autoUpdate = autoUpdate;
    //        this.type = type;
    //    }
    //    public Playlist(String name) {
    //        this.name = name;
    //    }
//    fun getTitle(): String {
//        return field
//    }
//
//
//    fun setTitle(title: String) {
//        field = title
//    }
//
//    fun getFileName(): String? {
//        return fileName
//    }
//
//    fun setFileName(fileName: String?) {
//        this.fileName = fileName
//    }
//
//    fun getImportDate(): Long {
//        return importDate
//    }
//
//    fun setImportDate(importDate: Long) {
//        this.importDate = importDate
//    }
//
//    fun getCount(): Int {
//        return count
//    }
//
//    fun setCount(count: Int) {
//        this.count = count
//    }
//
//    fun isAutoUpdate(): Boolean {
//        return autoUpdate
//    }
//
//    fun setAutoUpdate(autoUpdate: Boolean) {
//        this.autoUpdate = autoUpdate
//    }
}