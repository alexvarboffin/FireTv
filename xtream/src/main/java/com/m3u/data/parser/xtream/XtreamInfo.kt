package com.m3u.data.parser.xtream

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class XtreamInfo(
    @SerializedName("server_info")
    val serverInfo: ServerInfo = ServerInfo(),
    @SerializedName("user_info")
    val userInfo: UserInfo = UserInfo()
) {
    @Serializable
    data class ServerInfo(
        @SerializedName("https_port")
        val httpsPort: String? = null,
        @SerializedName("port")
        val port: String? = null,
//        @SerializedName("rtmp_port")
//        val rtmpPort: String?,
        @SerializedName("server_protocol")
        val serverProtocol: String? = null,
//        @SerializedName("time_now")
//        val timeNow: String?,
//        @SerializedName("timestamp_now")
//        val timestampNow: Int?,
//        @SerializedName("timezone")
//        val timezone: String?,
//        @SerializedName("url")
//        val url: String?
    )

    @Serializable
    data class UserInfo(
        @SerializedName("active_cons")
        val activeCons: String? = null,
        @SerializedName("allowed_output_formats")
        val allowedOutputFormats: List<String> = emptyList(),
//        @SerializedName("auth")
//        val auth: Int?,
        @SerializedName("created_at")
        val createdAt: String? = null,
        @SerializedName("is_trial")
        val isTrial: String? = null,
        @SerializedName("max_connections")
        val maxConnections: String? = null,
//        @SerializedName("message")
//        val message: String?,
//        @SerializedName("password")
//        val password: String?,
        @SerializedName("status")
        val status: String? = null,
        @SerializedName("username")
        val username: String? = null
    )
}