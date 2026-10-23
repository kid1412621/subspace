package me.nanova.subspace.data.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import me.nanova.subspace.data.db.entity.RemoteKeys

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
