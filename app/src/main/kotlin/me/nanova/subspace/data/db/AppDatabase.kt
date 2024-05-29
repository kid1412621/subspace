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
                db.execSQL("ALTER TABLE torrent ADD COLUMN id BIGINT PRIMARY KEY AUTO_INCREMENT;")
                db.execSQL("ALTER TABLE torrent RENAME COLUMN addedOn TO added_on")
                db.execSQL("ALTER TABLE torrent RENAME COLUMN accountId TO account_id")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_torrent_account_id ON torrent (account_id);")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_torrent_hash ON torrent (hash);")
                db.execSQL("ALTER TABLE torrent ADD COLUMN tags TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE torrent ADD COLUMN downloaded INTEGER NOT NULL")
                db.execSQL("ALTER TABLE torrent ADD COLUMN uploaded INTEGER NOT NULL")
                db.execSQL("ALTER TABLE torrent ADD COLUMN dlspeed INTEGER NOT NULL")
                db.execSQL("ALTER TABLE torrent ADD COLUMN upspeed INTEGER NOT NULL")
                db.execSQL("ALTER TABLE torrent ADD COLUMN ratio REAL NOT NULL")
                db.execSQL("ALTER TABLE torrent ADD COLUMN leechs INTEGER NOT NULL")
                db.execSQL("ALTER TABLE torrent ADD COLUMN seeds INTEGER NOT NULL")
                db.execSQL("ALTER TABLE torrent ADD COLUMN priority INTEGER NOT NULL")

                db.execSQL("ALTER TABLE Account RENAME TO account;")

                db.execSQL(
                    """
                    CREATE TABLE remote_keys (
                        torrent_id BIGINT PRIMARY KEY NOT NULL,
                        account_id BIGINT
                        last_offset INTEGER
                    )
                    """
                )
            }
        }
    }

}