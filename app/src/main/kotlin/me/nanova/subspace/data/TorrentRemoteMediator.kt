package me.nanova.subspace.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import me.nanova.subspace.data.api.QTApiService
import me.nanova.subspace.data.db.AppDatabase
import me.nanova.subspace.domain.model.QTListParams
import me.nanova.subspace.domain.model.RemoteKeys
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.model.toEntity

@OptIn(ExperimentalPagingApi::class)
class TorrentRemoteMediator(
    private val currentAccountId: Long,
    private val query: QTListParams,
    private val database: AppDatabase,
    private val networkService: QTApiService
) : RemoteMediator<Int, Torrent>() {

    private val torrentDao = database.torrentDao()
    private val remoteKeyDao = database.remoteKeyDao()

    override suspend fun initialize(): InitializeAction {
//        return InitializeAction.SKIP_INITIAL_REFRESH
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Torrent>
    ): MediatorResult {
        return try {
            val offset = when (loadType) {
                LoadType.REFRESH -> 0
                // No items to prepend in offset-based APIs
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
//                LoadType.PREPEND -> {
//                    val firstItemId = state.firstItemOrNull()?.id
//                    val firstItemRemoteKeys = firstItemId?.let {
//                        remoteKeyDao.remoteKeysItemId(it)
//                    }
//                    firstItemRemoteKeys?.lastOffset?.minus(state.config.pageSize)
//                        ?: return MediatorResult.Success(endOfPaginationReached = true)
//                }
                LoadType.APPEND -> {
                    val lastItemId = state.lastItemOrNull()?.id
                    val lastItemRemoteKeys = lastItemId?.let {
                        remoteKeyDao.remoteKeysItemId(it)
                    }
                    lastItemRemoteKeys?.lastOffset?.plus(state.config.pageSize)
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }

            // fetch api
            val response = networkService.list(
                query.copy(offset = offset, limit = state.config.pageSize).toMap()
            )
            val endOfPaginationReached = response.size < state.config.pageSize

            // update db
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.clearRemoteKeys(currentAccountId)
                    torrentDao.clearAll(currentAccountId)
                }

                val entities = response.map { it.toEntity(currentAccountId) }
                torrentDao.insertAll(entities)
                val keys = entities.map {
                    RemoteKeys(
                        torrentId = it.id,
                        lastOffset = offset,
                        accountId = currentAccountId
                    )
                }
                remoteKeyDao.insertAll(keys)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
