package com.walhalla.data.model

import androidx.room.ColumnInfo

data class CategoryNameCount(
    @ColumnInfo(name = "cat") val cat: String?,
    @ColumnInfo(name = "cnt") val count: Int,
)
