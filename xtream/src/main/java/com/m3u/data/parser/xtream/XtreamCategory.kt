package com.m3u.data.parser.xtream

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class XtreamCategory(
    @SerializedName("category_id")
    val categoryId: Int?,
    @SerializedName("category_name")
    val categoryName: String?,
    @SerializedName("parent_id")
    val parentId: Int?
)