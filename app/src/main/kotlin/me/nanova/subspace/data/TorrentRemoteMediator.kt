package me.nanova.subspace.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room3.withWriteTransaction
import me.nanova.subspace.data.api.QBApiService
import me.nanova.subspace.data.api.dto.toEntity
import me.nanova.subspace.data.db.AppDatabase
import me.nanova.subspace.data.db.entity.RemoteKeys
import me.nanova.subspace.data.db.entity.TorrentEntity
import me.nanova.subspace.domain.model.QBListParams
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class TorrentRemoteMediator(
    private val currentAccountId: Long,
    private val query: QBListParams,
    private val database: AppDatabase,
    private val api: QBApiService,
    // NetworkStatusMonitor is no longer needed here, check is done in ViewModel
    // private val networkStatusMonitor: NetworkStatusMonitor
) : RemoteMediator<Int, TorrentEntity>() {

    private val torrentDao = database.torrentDao()
    private val remoteKeyDao = database.remoteKeyDao()

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, TorrentEntity>
    ): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                remoteKeyDao.remoteKeysItemId(id, currentAccountId)
            }
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TorrentEntity>
    ): MediatorResult {
        val pageSize = state.config.pageSize
        val loadKey = when (loadType) {
            LoadType.REFRESH -> null
            LoadType.PREPEND -> {
                val firstItem = state.firstItemOrNull()
                val remoteKeys =
                    firstItem?.let { remoteKeyDao.remoteKeysItemId(it.id, currentAccountId) }
                remoteKeys?.prevOffset
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                val remoteKeys =
                    lastItem?.let { remoteKeyDao.remoteKeysItemId(it.id, currentAccountId) }
                remoteKeys?.nextOffset
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        try {
            val offset: Int
            val limit: Int
            if (loadType == LoadType.REFRESH) {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                val anchorPageOffset = remoteKeys?.prevOffset?.plus(pageSize)
                    ?: remoteKeys?.nextOffset?.minus(pageSize)?.coerceAtLeast(0)
                    ?: 0
                offset = 0
                limit = anchorPageOffset + pageSize
            } else {
                offset = loadKey ?: 0
                limit = pageSize
            }

            // Prepare query parameters based on filter and pagination
            val params = query.toMap().toMutableMap().apply {
                put("limit", limit.toString())
                put("offset", offset.toString())
            }

            // Fetch data from the network
            val torrents = api.list(params)

            val endOfPaginationReached = torrents.size < limit
            val now = System.currentTimeMillis()

            database.withWriteTransaction {
                if (loadType == LoadType.REFRESH) {
                    // Clear existing data and remote keys on refresh
                    remoteKeyDao.clearRemoteKeys(currentAccountId)
                    torrentDao.clearAll(currentAccountId)
                }

                val keys = torrents.mapIndexed { index, torrent ->
                    val itemPageOffset = offset + (index / pageSize) * pageSize
                    val isLastLoadedPage =
                        (index / pageSize + 1) * pageSize >= limit ||
                                (index / pageSize + 1) * pageSize >= torrents.size
                    val prevOffset = if (itemPageOffset == 0) null else itemPageOffset - pageSize
                    val nextOffset =
                        if (endOfPaginationReached && isLastLoadedPage) null else itemPageOffset + pageSize
                    RemoteKeys(
                        torrentId = torrent.id,
                        prevOffset = prevOffset,
                        nextOffset = nextOffset,
                        accountId = currentAccountId,
                        lastUpdated = now
                    )
                }
                remoteKeyDao.insertAll(keys)
                torrentDao.insertAll(torrents.map { it.toEntity(currentAccountId) })
            }

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: Exception) {
            return MediatorResult.Error(exception)
        }
    }

}