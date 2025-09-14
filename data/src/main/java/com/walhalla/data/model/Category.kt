package com.walhalla.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "category", indices = [Index(value = ["name"], unique = true)])
class Category {
    constructor()

    constructor(name: String?, desc: String?, thumb: String?) {
        this.name = name
        this.desc = desc
        this.thumb = thumb
    }

    @JvmField
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var _id: Long = 0

    @JvmField
    var name: String? = null
    @JvmField
    var desc: String? = null
    @JvmField
    var thumb: String? = null
}
