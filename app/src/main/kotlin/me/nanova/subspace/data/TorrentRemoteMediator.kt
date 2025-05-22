package me.nanova.subspace.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import kotlinx.coroutines.flow.first
import me.nanova.subspace.data.client.QBittorrentClientImpl
import me.nanova.subspace.data.db.AppDatabase
import me.nanova.subspace.domain.client.TorrentClient
import me.nanova.subspace.domain.model.GenericTorrentFilter
import me.nanova.subspace.domain.model.RemoteKeys
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.model.TorrentEntity
import me.nanova.subspace.domain.model.toEntity

@OptIn(ExperimentalPagingApi::class)
class TorrentRemoteMediator(
    private val currentAccountId: Long,
    private val filter: GenericTorrentFilter,
    private val database: AppDatabase,
    private val torrentClient: TorrentClient // Changed from QBApiService to TorrentClient
) : RemoteMediator<Int, TorrentEntity>() {

    private val torrentDao = database.torrentDao()
    private val remoteKeyDao = database.remoteKeyDao()

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TorrentEntity>
    ): MediatorResult {
        return try {
            val pageNumber = when (loadType) {
                LoadType.REFRESH -> 1 // Start from page 1 on refresh
                LoadType.PREPEND -> {
                    // Prepending is not typically used with offset/page-based pagination in this manner.
                    // If your API supports it, you'd calculate the previous page number.
                    // For simplicity, returning true for endOfPaginationReached.
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.APPEND -> {
                    // Calculate next page number based on last item's key or total items loaded.
                    // This requires storing page info in RemoteKeys or calculating from offset.
                    val lastItem = state.lastItemOrNull()
                    val remoteKeys = lastItem?.let { remoteKeyDao.remoteKeysItemId(it.id) }
                    // Assuming nextOffset stores the next page number or can be used to calculate it.
                    // If nextOffset stores actual offset, convert to page: (offset / pageSize) + 1
                    remoteKeys?.nextOffset ?: 1 // Default to page 1 if no key (should ideally not happen in APPEND)
                }
            }

            // Fetch data using TorrentClient
            // The torrentClient.fetchTorrents method needs to support pagination (page, pageSize)
            // This is a placeholder for how it might be called.
            // QBittorrentClientImpl has fetchTorrents(filter, page, pageSize)
            val response: List<Torrent> = when (torrentClient) {
                is QBittorrentClientImpl -> { // Handle QB specific client call if needed
                    torrentClient.fetchTorrents(filter, page = pageNumber, pageSize = state.config.pageSize).first()
                }
                // Add other client types here if they have different fetch methods
                else -> {
                    // Fallback or generic client call - this part needs careful design
                    // For now, assuming a common interface method or throwing an error
                    throw IllegalStateException("Unsupported TorrentClient type for remote mediation or fetchTorrents not available.")
                    // Alternatively, if TorrentClient defines a common fetchTorrents with pagination:
                    // torrentClient.fetchTorrents(filter, page = pageNumber, pageSize = state.config.pageSize).first()
                }
            }

            val endOfPaginationReached = response.isEmpty() || response.size < state.config.pageSize

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.clearRemoteKeys(currentAccountId)
                    torrentDao.clearAll(currentAccountId)
                }

                val entities = response.map { it.toEntity(currentAccountId) } // toEntity handles DomainTorrentState
                torrentDao.insertAll(entities)

                // Update RemoteKeys: prevOffset and nextOffset now represent page numbers
                val prevPage = if (pageNumber == 1) null else pageNumber - 1
                val nextPage = if (endOfPaginationReached) null else pageNumber + 1
                val keys = entities.map {
                    RemoteKeys(
                        torrentId = it.id,
                        prevOffset = prevPage, // Store previous page number
                        nextOffset = nextPage,  // Store next page number
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
