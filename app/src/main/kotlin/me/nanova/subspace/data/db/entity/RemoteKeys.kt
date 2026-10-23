package me.nanova.subspace.data.db.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

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
