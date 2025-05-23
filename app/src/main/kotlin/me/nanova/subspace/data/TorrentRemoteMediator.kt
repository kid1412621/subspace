package me.nanova.subspace.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import me.nanova.subspace.data.api.QBApiService
import me.nanova.subspace.data.db.AppDatabase
import me.nanova.subspace.domain.model.QBListParams
import me.nanova.subspace.domain.model.RemoteKeys
import me.nanova.subspace.domain.model.TorrentEntity
import me.nanova.subspace.domain.model.toEntity

@OptIn(ExperimentalPagingApi::class)
class TorrentRemoteMediator(
    private val currentAccountId: Long,
    private val query: QBListParams,
    private val database: AppDatabase,
    private val api: QBApiService
) : RemoteMediator<Int, TorrentEntity>() {

    private val torrentDao = database.torrentDao()
    private val remoteKeyDao = database.remoteKeyDao()

    override suspend fun initialize(): InitializeAction {
//        return InitializeAction.SKIP_INITIAL_REFRESH
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TorrentEntity>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null // Start from the beginning on refresh
                LoadType.PREPEND -> {
                    // Get the first item's key to determine the prev offset
                    val firstItem = state.firstItemOrNull()
                    val remoteKeys = firstItem?.let { remoteKeyDao.remoteKeysItemId(it.id) }
                    remoteKeys?.prevOffset
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                }

                LoadType.APPEND -> {
                    // Get the last item's key to determine the next offset
                    val lastItem = state.lastItemOrNull()
                    val remoteKeys = lastItem?.let { remoteKeyDao.remoteKeysItemId(it.id) }
                    remoteKeys?.nextOffset
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }

            // fetch api
            val offset =
                loadKey ?: 0 //Default to 0 if null, means its load type refresh or first time load
            val response = api.list(
                query.copy(offset = offset, limit = state.config.pageSize).toMap()
            )
            val endOfPaginationReached = response.isEmpty() || response.size < state.config.pageSize

            // update db
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.clearRemoteKeys(currentAccountId)
                    torrentDao.clearAll(currentAccountId)
                }

                val entities = response.map { it.toEntity(currentAccountId) }
                torrentDao.insertAll(entities)
                val prevOffset = if (offset == 0) null else offset - state.config.pageSize
                val nextOffset =
                    if (endOfPaginationReached) null else offset + state.config.pageSize
                val keys = entities.map {
                    RemoteKeys(
                        torrentId = it.id,
                        prevOffset = prevOffset,
                        nextOffset = nextOffset,
                        accountId = currentAccountId
                    )
                }
                remoteKeyDao.insertAll(keys)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            // might display db data when network is unavailable, but not sure the user case, let decide in future
            MediatorResult.Error(e)
        }
    }
}
