package tv.hdonlinetv.compose.core.epg

data class EpgProgramme(
    val title: String,
    val desc: String?,
    val start: Long,
    val stop: Long,
) {
    fun progress(now: Long): Float =
        if (stop <= start) 0f else ((now - start).toFloat() / (stop - start)).coerceIn(0f, 1f)
}

data class EpgNowNext(
    val now: EpgProgramme?,
    val next: EpgProgramme?,
)

sealed interface EpgSyncResult {
    data class Success(
        val channels: Int,
        val programmesSeen: Int,
        val programmesKept: Int,
    ) : EpgSyncResult

    data class Failed(val reason: String) : EpgSyncResult
}
