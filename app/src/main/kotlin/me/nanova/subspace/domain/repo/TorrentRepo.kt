package me.nanova.subspace.domain.repo

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.QBCategories
import me.nanova.subspace.domain.model.QBListParams
import me.nanova.subspace.domain.model.Torrent

interface TorrentRepo {
//    suspend fun apiVersion(): String

    fun torrents(account: Account, filter: QBListParams): Flow<PagingData<Torrent>>

    fun categories(): Flow<QBCategories>

    fun tags(): Flow<List<String>>

}

