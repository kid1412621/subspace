package me.nanova.subspace.domain.repo

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.CategoryInfo
import me.nanova.subspace.domain.model.GenericTorrentFilter
import me.nanova.subspace.domain.model.Torrent

interface TorrentRepo {

    fun getTorrents(accountId: Long, filter: GenericTorrentFilter): Flow<PagingData<Torrent>>

    fun getCategories(accountId: Long): Flow<Map<String, CategoryInfo>>

    fun getTags(accountId: Long): Flow<List<String>>

    suspend fun stopTorrents(accountId: Long, torrentHashes: List<String>)
    suspend fun startTorrents(accountId: Long, torrentHashes: List<String>)
    suspend fun pauseTorrents(accountId: Long, torrentHashes: List<String>)
    suspend fun deleteTorrents(accountId: Long, torrentHashes: List<String>, deleteData: Boolean)

}

