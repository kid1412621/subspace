package me.nanova.subspace.domain.repo

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.QTListParams
import me.nanova.subspace.domain.model.Torrent

interface TorrentRepo {
    suspend fun apiVersion(): String

    fun torrents(): Flow<PagingData<Torrent>>
    fun torrents(params: QTListParams): Flow<List<Torrent>>
    suspend fun refresh(params: Map<String, String?>)
    suspend fun fetch(params: QTListParams): List<Torrent>
}

