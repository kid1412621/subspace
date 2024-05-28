package me.nanova.subspace.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
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
}