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
import java.io.IOException
import java.util.concurrent.TimeUnit

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
        // Check if cached data is older than 30 minutes
        val cacheTimeout = TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES)
        val latest = database.withTransaction {
            remoteKeyDao.lastUpdatedByAccount(currentAccountId)
        }

        return if (latest != null && System.currentTimeMillis() - latest.lastUpdated <= cacheTimeout) {
            // Cached data is valid, no need to refresh
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            // Cached data is stale or missing, trigger a refresh
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TorrentEntity>
    ): MediatorResult {
        // Network status check is now done in the ViewModel before calling this.
        // If this method is called, we assume the ViewModel determined it's appropriate to try fetching.

        val loadKey = when (loadType) {
            LoadType.REFRESH -> null // Start from the beginning on refresh
            LoadType.PREPEND -> {
                // Get the first item's key to determine the prev offset
                val firstItem = state.firstItemOrNull()
                val remoteKeys =
                    firstItem?.let { remoteKeyDao.remoteKeysItemId(it.id, currentAccountId) }
                remoteKeys?.prevOffset
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                // Get the last item's key to determine the next offset
                val lastItem = state.lastItemOrNull()
                val remoteKeys =
                    lastItem?.let { remoteKeyDao.remoteKeysItemId(it.id, currentAccountId) }
                remoteKeys?.nextOffset
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        try {
            // Calculate limit and offset for the API call
            val offset =
                loadKey ?: 0 //Default to 0 if null, means its load type refresh or first time load
            val limit = state.config.pageSize

            // Prepare query parameters based on filter and pagination
            val params = query.toMap().toMutableMap().apply {
                put("limit", limit.toString())
                put("offset", offset.toString())
            }

            // Fetch data from the network
            val torrents = api.list(params)

            val endOfPaginationReached = torrents.isEmpty() || torrents.size < state.config.pageSize

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    // Clear existing data and remote keys on refresh
                    remoteKeyDao.clearRemoteKeys(currentAccountId)
                    torrentDao.clearAll(currentAccountId)
                }

                val prevOffset = if (offset == 0) null else offset - state.config.pageSize
                val nextOffset =
                    if (endOfPaginationReached) null else offset + state.config.pageSize
                val keys = torrents.map {
                    RemoteKeys(
                        torrentId = it.id,
                        prevOffset = prevOffset,
                        nextOffset = nextOffset,
                        accountId = currentAccountId,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
                remoteKeyDao.insertAll(keys)
                torrentDao.insertAll(torrents.map { it.toEntity(currentAccountId) })
            }

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

        } catch (exception: IOException) {
            // Handle network errors (though ViewModel tries to prevent this call when offline)
            // or other IO issues like server unreachable.
            return MediatorResult.Error(exception)
        } catch (exception: Exception) {
            // Handle other errors
            return MediatorResult.Error(exception)
        }
    }

}