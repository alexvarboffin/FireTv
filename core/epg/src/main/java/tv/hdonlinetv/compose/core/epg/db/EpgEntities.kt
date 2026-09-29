package tv.hdonlinetv.compose.core.epg.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "epg_source",
    indices = [Index(value = ["url"], unique = true)],
)
data class EpgSourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val lastSyncAt: Long = 0,
    val lastStatus: String? = null,
    val programmeCount: Int = 0,
)

@Entity(
    tableName = "programme",
    foreignKeys = [
        ForeignKey(
            entity = EpgSourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["channelKey", "stop"]),
        Index(value = ["sourceId"]),
    ],
)
data class ProgrammeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceId: Long,
    /** [tv.hdonlinetv.compose.core.epg.EpgKey.normalize]d channel id. */
    val channelKey: String,
    val start: Long,
    val stop: Long,
    val title: String,
    val desc: String?,
)
