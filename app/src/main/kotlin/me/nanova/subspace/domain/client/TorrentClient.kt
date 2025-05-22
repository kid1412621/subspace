package me.nanova.subspace.domain.client

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.CategoryInfo
import me.nanova.subspace.domain.model.GenericTorrentFilter
import me.nanova.subspace.domain.model.ServerState
import me.nanova.subspace.domain.model.Torrent

interface TorrentClient {
    suspend fun login(account: Account): Flow<Boolean>
    suspend fun logout()

    fun getTorrents(filter: GenericTorrentFilter): Flow<PagingData<Torrent>>
    fun getTorrentDetails(id: String): Flow<Torrent> // Assuming 'id' is a unique identifier for a torrent

    suspend fun startTorrents(ids: List<String>): Flow<Boolean>
    suspend fun stopTorrents(ids: List<String>): Flow<Boolean>
    suspend fun pauseTorrents(ids: List<String>): Flow<Boolean>
    suspend fun deleteTorrents(ids: List<String>, deleteData: Boolean): Flow<Boolean>

    fun getCategories(): Flow<Map<String, CategoryInfo>>
    fun getTags(): Flow<List<String>>
    fun getServerState(): Flow<ServerState>
    fun getAppVersion(): Flow<String>
}
