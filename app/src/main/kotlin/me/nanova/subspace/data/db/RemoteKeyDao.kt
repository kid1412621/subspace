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

    @Query("SELECT * FROM remote_keys WHERE account_id = :accountId ORDER BY last_updated DESC")
    suspend fun lastUpdatedByAccount( accountId: Long): RemoteKeys?

    @Query("SELECT * FROM remote_keys WHERE torrent_id = :id AND account_id = :accountId")
    suspend fun remoteKeysItemId(id: String, accountId: Long): RemoteKeys?

    @Query("DELETE FROM remote_keys WHERE account_id = :accountId")
    suspend fun clearRemoteKeys(accountId: Long)
}
