package me.nanova.subspace.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.nanova.subspace.data.api.QBApiService
import me.nanova.subspace.data.api.QBAuthService
import me.nanova.subspace.data.db.AppDatabase
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.data.db.TorrentDao.Companion.buildQuery
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.QBCategories
import me.nanova.subspace.domain.model.QBListParams
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.model.toModel
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject
import javax.inject.Provider

class TorrentRepoImpl @Inject constructor(
    private val database: AppDatabase,
    private val torrentDao: TorrentDao,
    private val apiService: Provider<QBApiService>,
    private val authService: QBAuthService,
) : TorrentRepo {

    override suspend fun login(url: String, username: String, password: String): String {
        val res = authService.login(url, username, password)
        if (!res.isSuccessful) {
            if (res.code() == 403) {
                throw RuntimeException("Wrong username or password.")
            }
            throw RuntimeException("Cannot connect to qBittorent service.")
        }
        val cookie = res.headers()["Set-Cookie"]
        if (cookie.isNullOrBlank()) {
            throw RuntimeException("Failed to retrieve cookie.")
        }
        return cookie
    }

    override suspend fun appVersion(url: String, cookie: String) =
        authService.appVersion(url, cookie).removePrefix("v")

    override suspend fun apiVersion() = apiService.get().apiVersion()

    companion object {
        const val PAGE_SIZE = 20
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun torrents(account: Account, filter: QBListParams): Flow<PagingData<Torrent>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = 1,
                enablePlaceholders = true,
            ),
            remoteMediator = TorrentRemoteMediator(
                account.id,
                filter,
                database,
                apiService.get()
            ),
            pagingSourceFactory = {
                torrentDao.pagingSource(buildQuery(account.id, filter))
            }
        ).flow.map { pagingData ->
            pagingData.map { entity -> entity.toModel() }
        }
    }

    override fun categories(): Flow<QBCategories> {
        return flow {
            emit(apiService.get().categories() ?: emptyMap())
        }.catch { e -> throw e }
    }

    override fun tags(): Flow<List<String>> {
        return flow {
            emit(apiService.get().tags())
        }.catch { e -> throw e }
    }

}


