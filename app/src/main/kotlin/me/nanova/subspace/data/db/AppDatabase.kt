package me.nanova.subspace.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import me.nanova.subspace.data.db.AccountDao
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.model.TorrentDB

@Database(entities = [TorrentDB::class, Account::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun torrentDao(): TorrentDao
    abstract fun accountDao(): AccountDao
}