package tv.hdonlinetv.compose.core.databridge.playlist

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal object PlaylistUrlHelper {

    fun formatServerUrl(url: String): String =
        if (url.startsWith("http://") || url.startsWith("https://")) url else "http://$url"

    fun buildPlayerApiUrl(serverUrl: String, username: String, password: String): String {
        val formatted = formatServerUrl(serverUrl)
        val baseUrl = formatted.toHttpUrlOrNull() ?: return formatted
        return HttpUrl.Builder()
            .scheme(baseUrl.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .addPathSegment("player_api.php")
            .addQueryParameter("username", username)
            .addQueryParameter("password", password)
            .build()
            .toString()
    }

    fun deriveTitleFromUrl(url: String, fallbackHost: String? = null): String {
        val lastSlashIndex = url.lastIndexOf('/')
        if (lastSlashIndex != -1 && lastSlashIndex < url.length - 1) {
            val name = url.substring(lastSlashIndex + 1).trim()
            if (name.isNotEmpty()) return name
        }
        return fallbackHost?.takeIf { it.isNotEmpty() } ?: url
    }
}
