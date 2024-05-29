package me.nanova.subspace.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.data.api.QTApiService
import me.nanova.subspace.data.db.AppDatabase
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.QTFilterState
import me.nanova.subspace.domain.model.QTListParams
import me.nanova.subspace.domain.model.Torrent
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
                enablePlaceholders = true,
                prefetchDistance = 1
            ),
            remoteMediator = TorrentRemoteMediator(
                account.id,
                filter,
                database,
                apiService.get()
            ),
            pagingSourceFactory = {
                torrentDao.pagingSource(
                    buildQuery(account.id, filter)
                )
            }
        ).flow
    }

    companion object {
        const val PAGE_SIZE = 20

        fun buildQuery(
            accountId: Long,
            filter: QTListParams,
        ): SupportSQLiteQuery {
            var query = "SELECT * FROM torrent WHERE account_id = $accountId"

            if (filter.category != null) {
                query += " AND category = '${filter.category}'"
            }

            if (filter.filter.isNotBlank()) {
                val state = QTFilterState.valueOf(filter.filter).toQTStates()
                if (state.isNotEmpty()) {
                    query += " AND state IN ('${state.joinToString("', '")}')"
                }
            }

            if (!filter.sort.isNullOrBlank()) {
                query += " ORDER BY ${filter.sort} ${if (filter.reverse) "DESC" else "ASC"}"
            }
            return SimpleSQLiteQuery(query)
        }

    }

}


