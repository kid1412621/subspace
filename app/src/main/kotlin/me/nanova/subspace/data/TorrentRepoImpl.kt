package me.nanova.subspace.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.nanova.subspace.data.api.QTApiService
import me.nanova.subspace.data.db.AppDatabase
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.data.db.TorrentDao.Companion.buildQuery
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.QTCategories
import me.nanova.subspace.domain.model.QTListParams
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.model.toModel
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Inject
import javax.inject.Provider

class TorrentRepoImpl @Inject constructor(
    private val database: AppDatabase,
    private val torrentDao: TorrentDao,
    private val apiService: Provider<QTApiService>
) : TorrentRepo {
//    override suspend fun apiVersion() = apiService.get().version()

    companion object {
        const val PAGE_SIZE = 20
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun torrents(account: Account, filter: QTListParams): Flow<PagingData<Torrent>> {
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

    override fun categories(): Flow<QTCategories> {
        return flow {
            emit(apiService.get().categories())
        }
    }

    override fun tags(): Flow<List<String>> {
        return flow {
            emit(apiService.get().tags())
        }
    }

}


