package me.nanova.subspace.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.Torrent

@Dao
interface TorrentDao {
    @Query("SELECT * FROM torrent")
    fun getAll(): Flow<List<Torrent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(torrents: List<Torrent>)
}