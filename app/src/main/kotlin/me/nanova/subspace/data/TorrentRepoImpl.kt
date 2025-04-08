package me.nanova.subspace.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.data.api.QTApiService
import me.nanova.subspace.data.db.AppDatabase
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.data.db.TorrentDao.Companion.buildQuery
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.QTListParams
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.model.TorrentEntity
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
                val pagingSource = torrentDao.pagingSource(buildQuery(account.id, filter))
                object : PagingSource<Int, Torrent>() {
                    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Torrent> {
                        val result = pagingSource.load(params)
                        return when (result) {
                            is LoadResult.Page -> {
                                LoadResult.Page(
                                    data = result.data.map { it.toModel() },
                                    prevKey = result.prevKey,
                                    nextKey = result.nextKey
                                )
                            }

                            is LoadResult.Error -> LoadResult.Error(result.throwable)
                            is LoadResult.Invalid -> LoadResult.Invalid()
                        }
                    }

                    override fun getRefreshKey(state: PagingState<Int, Torrent>): Int? {
                        val stateEntity = state as PagingState<Int, TorrentEntity> //fixme
                        return pagingSource.getRefreshKey(stateEntity)
                    }
                }
            }
        ).flow
    }

    companion object {
        const val PAGE_SIZE = 20
    }

}


