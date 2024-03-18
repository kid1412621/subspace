package me.nanova.subspace.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.model.TorrentDB

@Dao
interface TorrentDao {
    @Query("SELECT * FROM torrent")
    fun getAll(): Flow<List<TorrentDB>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(torrents: List<TorrentDB>)
}