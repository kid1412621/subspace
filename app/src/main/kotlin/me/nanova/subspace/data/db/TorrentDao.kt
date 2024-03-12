package me.nanova.subspace.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import me.nanova.subspace.domain.model.Torrent

@Dao
interface TorrentDao {
    @Query("SELECT * FROM torrent")
    fun getAll(): List<Torrent>

    @Insert
    fun insertAll(vararg users: Torrent)
}