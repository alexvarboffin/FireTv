package tv.hdonlinetv.compose.core.epg.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EpgDao {

    @Query("SELECT * FROM epg_source WHERE url = :url LIMIT 1")
    fun sourceByUrl(url: String): EpgSourceEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSource(source: EpgSourceEntity): Long

    @Query(
        "UPDATE epg_source SET lastSyncAt = :at, lastStatus = :status, programmeCount = :count WHERE id = :id",
    )
    fun updateSourceStatus(id: Long, at: Long, status: String, count: Int)

    @Query("UPDATE epg_source SET lastSyncAt = :at, lastStatus = :status WHERE id = :id")
    fun markSourceError(id: Long, at: Long, status: String)

    @Query("SELECT * FROM epg_source ORDER BY id")
    fun sources(): List<EpgSourceEntity>

    @Query("DELETE FROM programme WHERE sourceId = :sourceId")
    fun deleteProgrammes(sourceId: Long)

    @Query("DELETE FROM programme WHERE stop < :before")
    fun deleteEndedBefore(before: Long): Int

    @Insert
    fun insertProgrammes(items: List<ProgrammeEntity>)

    /** Current and upcoming programmes for [keys], ordered for per-channel take(2). */
    @Query(
        "SELECT * FROM programme WHERE channelKey IN (:keys) AND stop > :now " +
            "ORDER BY channelKey, start",
    )
    fun upcoming(keys: List<String>, now: Long): List<ProgrammeEntity>

    @Query(
        "SELECT * FROM programme WHERE channelKey IN (:keys) AND stop > :now " +
            "ORDER BY channelKey, start",
    )
    fun observeUpcoming(keys: List<String>, now: Long): Flow<List<ProgrammeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertChannelMap(items: List<ChannelMapEntity>)

    @Query("SELECT * FROM channel_map WHERE appKey IN (:appKeys)")
    fun channelMap(appKeys: List<String>): List<ChannelMapEntity>

    @Query(
        "SELECT * FROM programme WHERE channelKey = :key AND stop > :from AND start < :to ORDER BY start",
    )
    fun schedule(key: String, from: Long, to: Long): List<ProgrammeEntity>
}
