package tv.hdonlinetv.compose.core.epg.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Separate from the legacy `FavoriteDatabase`: guide data is a disposable cache. */
@Database(
    entities = [
        EpgSourceEntity::class,
        ProgrammeEntity::class,
        ChannelMapEntity::class,
        GuideBindingEntity::class,
    ],
    version = EpgDatabase.VERSION,
    exportSchema = false,
)
abstract class EpgDatabase : RoomDatabase() {
    abstract fun dao(): EpgDao

    companion object {
        /** Destructive upgrades wipe the cache: callers that throttle syncs must key on this. */
        const val VERSION = 3

        @Volatile
        private var instance: EpgDatabase? = null

        fun get(context: Context): EpgDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    EpgDatabase::class.java,
                    "epg.db",
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
