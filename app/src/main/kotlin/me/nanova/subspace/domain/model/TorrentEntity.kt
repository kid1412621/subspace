package me.nanova.subspace.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "torrent")
data class TorrentEntity(
    @PrimaryKey
    val hash: String,
    @ColumnInfo(index = true, name = "account_id")
    val accountId: Long,
    val name: String,
//    @ColumnInfo(name = "added_on")
    val addedOn: Long,
    val size: Long,
    val downloaded: Long,
    val uploaded: Long,
    val progress: Float,
    // seconds
    val eta: Long,
    val state: String,
    val category: String?,
    // comma-concatenated tag list of the torrent
    val tags: String?,
    // bytes/s
    val dlspeed: Long,
    val upspeed: Long,
    val ratio: Float,
    val leechs: Int,
    val seeds: Int,
    val priority: Int,
)

fun TorrentEntity.toModel() = Torrent(
    hash = this.hash,
    name = this.name,
    addedOn = this.addedOn,
    size = this.size,
    progress = this.progress,
    eta = this.eta,
    state = this.state,
    category = this.category,
    dlspeed = this.dlspeed,
    upspeed = this.upspeed,
    ratio = this.ratio,
    tags = this.tags,
    downloaded = this.downloaded,
    uploaded = this.uploaded,
    seeds = this.seeds,
    leechs = this.leechs,
    priority = this.priority
)
