package me.nanova.subspace.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.nanova.subspace.data.db.AccountDao
import me.nanova.subspace.data.db.AppDatabase
import me.nanova.subspace.data.db.RemoteKeyDao
import me.nanova.subspace.data.db.TorrentDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DB_NAME = "subspace.db"
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE torrent RENAME COLUMN accountId TO account_id")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_torrent_account_id ON torrent (account_id);")
            db.execSQL("ALTER TABLE torrent ADD COLUMN tags TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE torrent ADD COLUMN downloaded INTEGER NOT NULL")
            db.execSQL("ALTER TABLE torrent ADD COLUMN uploaded INTEGER NOT NULL")
            db.execSQL("ALTER TABLE torrent ADD COLUMN dlspeed INTEGER NOT NULL")
            db.execSQL("ALTER TABLE torrent ADD COLUMN upspeed INTEGER NOT NULL")
            db.execSQL("ALTER TABLE torrent ADD COLUMN ratio REAL NOT NULL")
            db.execSQL("ALTER TABLE torrent ADD COLUMN leechs INTEGER NOT NULL")
            db.execSQL("ALTER TABLE torrent ADD COLUMN seeds INTEGER NOT NULL")
            db.execSQL("ALTER TABLE torrent ADD COLUMN priority INTEGER NOT NULL")

            db.execSQL(
                """
                    CREATE TABLE remote_keys (
                        last_offset INTEGER,
                        hash TEXT PRIMARY KEY NOT NULL
                    )
                    """
            )
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            DB_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideAccountDao(database: AppDatabase): AccountDao {
        return database.accountDao()
    }

    @Provides
    fun provideRemoteKeyDao(database: AppDatabase): RemoteKeyDao {
        return database.remoteKeyDao()
    }

    @Provides
    fun provideTorrentDao(database: AppDatabase): TorrentDao {
        return database.torrentDao()
    }
}
