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
                LoadType.APPEND -> {
                    val remoteKeys = state.pages
                        .lastOrNull() { it.data.isNotEmpty() } // Find the first page with items
                        ?.data?.lastOrNull() // Get the first item in that page
                        ?.let { remoteKeyDao.remoteKeysItemId(it.id) }
                    remoteKeys?.nextOffset
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                }

                LoadType.PREPEND -> {
                    val remoteKeys = state.pages
                        .firstOrNull { it.data.isNotEmpty() }
                        ?.data?.firstOrNull()
                        ?.let { remoteKeyDao.remoteKeysItemId(it.id) }
                    remoteKeys?.prevOffset
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
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
