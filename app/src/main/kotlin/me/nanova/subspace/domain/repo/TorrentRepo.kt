package me.nanova.subspace.domain.repo

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.QBCategories
import me.nanova.subspace.domain.model.QBListParams
import me.nanova.subspace.domain.model.Torrent

interface TorrentRepo {

    /**
     * @return cookie
     * */
    suspend fun login(url: String, username: String, password: String): String

    /**
     * @return semver without prefix `v`
     */
    suspend fun appVersion(url: String, cookie: String): String

    /**
     * seems the api version is not suitable for compatibility check,
     * cannot find the version mapping between api version and app version
     */
    suspend fun apiVersion(): String

    fun torrents(account: Account, filter: QBListParams): Flow<PagingData<Torrent>>

    fun categories(): Flow<QBCategories>

    fun tags(): Flow<List<String>>

    suspend fun stop(torrents: List<String>)
    suspend fun start(torrents: List<String>)

}

