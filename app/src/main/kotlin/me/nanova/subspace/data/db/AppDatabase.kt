package me.nanova.subspace.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.RemoteKeys
import me.nanova.subspace.domain.model.TorrentEntity

@Database(
    entities = [
        Account::class,
        TorrentEntity::class,
        RemoteKeys::class
    ], version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun torrentDao(): TorrentDao
    abstract fun remoteKeyDao(): RemoteKeyDao
    abstract fun accountDao(): AccountDao

    companion object {
        const val DATABASE_NAME = "subspace.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Account RENAME TO account;")

                // previous version didn't use this table
                db.execSQL("DROP TABLE torrent;")

                db.execSQL(
                    """
                    CREATE TABLE torrent (
                        id TEXT PRIMARY KEY NOT NULL,
                        hash TEXT NOT NULL,
                        account_id INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        added_on INTEGER NOT NULL,
                        size INTEGER NOT NULL,
                        downloaded INTEGER NOT NULL,
                        uploaded INTEGER NOT NULL,
                        progress REAL NOT NULL,
                        eta INTEGER NOT NULL,
                        state TEXT NOT NULL,
                        category TEXT,
                        tags TEXT,
                        dlspeed INTEGER NOT NULL,
                        upspeed INTEGER NOT NULL,
                        ratio REAL NOT NULL,
                        leechs INTEGER NOT NULL,
                        seeds INTEGER NOT NULL,
                        priority INTEGER NOT NULL
                    );
                """
                )
                db.execSQL("CREATE INDEX index_torrent_hash ON torrent (hash);")
                db.execSQL("CREATE INDEX index_torrent_account_id ON torrent (account_id);")
                db.execSQL(
                    """
                    CREATE TABLE remote_keys (
                        torrent_id TEXT PRIMARY KEY NOT NULL,
                        account_id INTEGER NOT NULL,
                        last_offset INTEGER
                    );
                    """
                )
                db.execSQL("CREATE INDEX index_remote_keys_account_id ON remote_keys (account_id);")
            }
        }
    }

}