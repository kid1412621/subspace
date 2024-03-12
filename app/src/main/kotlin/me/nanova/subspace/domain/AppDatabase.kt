package me.nanova.subspace.domain

import androidx.room.Database
import androidx.room.RoomDatabase
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.domain.model.Torrent

@Database(entities = [Torrent::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun torrentRepo(): TorrentDao
}