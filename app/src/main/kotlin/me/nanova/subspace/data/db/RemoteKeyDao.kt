package me.nanova.subspace.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import me.nanova.subspace.domain.model.RemoteKeys

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<RemoteKeys>)

    @Query("SELECT * FROM remote_keys WHERE torrent_id = :id")
    suspend fun remoteKeysItemId(id: Long): RemoteKeys?

    @Query("DELETE FROM remote_keys WHERE account_id = :accountId")
    suspend fun clearRemoteKeys(accountId: Long)
}
