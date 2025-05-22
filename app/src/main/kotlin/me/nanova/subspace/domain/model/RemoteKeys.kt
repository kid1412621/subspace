package me.nanova.subspace.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeys(
    @PrimaryKey
    @ColumnInfo(name = "torrent_id")
    val torrentId: String,
    @ColumnInfo(index = true, name = "account_id")
    val accountId: Long,
    @ColumnInfo(name = "prev_offset")
    val prevOffset: Int?,
    @ColumnInfo(name = "next_offset")
    val nextOffset: Int?,
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)
