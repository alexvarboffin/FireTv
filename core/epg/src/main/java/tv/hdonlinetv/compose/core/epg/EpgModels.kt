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

data class EpgIndexSyncResult(
    val channels: Int,
    val matchedById: Int,
    val matchedByName: Int,
    /** Guide file URL → its download/parse result. */
    val files: Map<String, EpgSyncResult>,
    /** Index in the input list → guide icon, for channels without their own logo. */
    val icons: Map<Int, String>,
)
