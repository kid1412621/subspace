package me.nanova.subspace.data.db

import android.util.Log
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import me.nanova.subspace.data.db.entity.AccountEntity
import me.nanova.subspace.data.db.entity.RemoteKeys
import me.nanova.subspace.data.db.entity.TorrentEntity

@Database(
    entities = [
        AccountEntity::class,
        TorrentEntity::class,
        RemoteKeys::class
    ], version = 4
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun torrentDao(): TorrentDao
    abstract fun remoteKeyDao(): RemoteKeyDao
    abstract fun accountDao(): AccountDao

    companion object {
        const val DATABASE_NAME = "subspace.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override suspend fun migrate(connection: SQLiteConnection) {
                try {
                    connection.execSQL("ALTER TABLE Account ADD COLUMN use_lan_switch INTEGER NOT NULL DEFAULT 0;")
                    connection.execSQL("ALTER TABLE Account ADD COLUMN lan_url TEXT NOT NULL DEFAULT '';")
                    connection.execSQL("ALTER TABLE Account ADD COLUMN lan_ssid TEXT NOT NULL DEFAULT '';")
                    // sqlite table name is case-insensitive
                    connection.execSQL("ALTER TABLE Account RENAME TO tmp_account;")
                    connection.execSQL("ALTER TABLE tmp_account RENAME TO account;")

                    // previous version didn't use this table
                    connection.execSQL("DROP TABLE torrent;")
                    connection.execSQL(
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
                        priority INTEGER NOT NULL,
                        last_updated INTEGER NOT NULL
                    );
                    """
                    )
                    connection.execSQL("CREATE INDEX index_torrent_hash ON torrent (hash);")
                    connection.execSQL("CREATE INDEX index_torrent_account_id ON torrent (account_id);")

                    connection.execSQL(
                        """
                    CREATE TABLE remote_keys (
                        torrent_id TEXT PRIMARY KEY NOT NULL,
                        account_id INTEGER NOT NULL,
                        prev_offset INTEGER,
                        next_offset INTEGER
                    );
                    """
                    )
                    connection.execSQL("CREATE INDEX index_remote_keys_account_id ON remote_keys (account_id);")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "DB migration error: $e")
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override suspend fun migrate(connection: SQLiteConnection) {
                try {
                    connection.execSQL("ALTER TABLE Account ADD COLUMN version TEXT NOT NULL DEFAULT '';")
                    connection.execSQL("UPDATE Account SET type = 'QBITTORENT' WHERE type = 'QT';")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "DB migration error: $e")
                }
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override suspend fun migrate(connection: SQLiteConnection) {
                try {
                    connection.execSQL("ALTER TABLE remote_keys ADD COLUMN last_updated INTEGER NOT NULL DEFAULT 0;")
                    val currentTime = System.currentTimeMillis()
                    connection.execSQL("UPDATE remote_keys SET last_updated = $currentTime WHERE last_updated = 0")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "DB migration error: $e")
                }
            }
        }
    }

}
