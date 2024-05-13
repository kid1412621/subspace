package me.nanova.subspace.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "torrent")
data class TorrentDB(
    @PrimaryKey
    val hash: String,
    @ColumnInfo(index = true)
    val accountId: Long,
    val name: String,
    val addedOn: Long,
    val size: Long,
    val progress: Float,
    val eta: Long,
    val state: String,
    val category: String?,
)

fun TorrentDB.toModel() = Torrent(
    hash = this.hash,
    name = this.name,
    addedOn = this.addedOn,
    size = this.size,
    progress = this.progress,
    eta = this.eta,
    state = this.state,
    category = this.category,
    dlspeed = 0,
    upspeed = 0,
    ratio = 0F,
    tags = "",
    downloaded = 0L,
    uploaded = 0L
)
