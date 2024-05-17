package me.nanova.subspace.domain

import androidx.paging.PagingSource
import androidx.paging.PagingState
import me.nanova.subspace.data.api.QTApiService
import me.nanova.subspace.domain.model.QTListParams
import me.nanova.subspace.domain.model.Torrent
import javax.inject.Inject
import javax.inject.Provider

class TorrentPagingSource @Inject constructor(
    private val apiService: Provider<QTApiService>
) : PagingSource<QTListParams, Torrent>() {
    override fun getRefreshKey(state: PagingState<QTListParams, Torrent>): QTListParams? {
        return state.anchorPosition?.let { anchorPosition ->
            // This loads starting from previous page, but since PagingConfig.initialLoadSize spans
            // multiple pages, the initial load will still load items centered around
            // anchorPosition. This also prevents needing to immediately launch prepend due to
            // prefetchDistance.
            state.closestPageToPosition(anchorPosition)?.prevKey
        }
    }

    override suspend fun load(params: LoadParams<QTListParams>): LoadResult<QTListParams, Torrent> {
        return try {
            val query = params.key ?: QTListParams()

//            val response = torrentRepo.torrents(params.key ?: queryParams)
            val response = apiService.get().getTorrents(query.toMap())

            LoadResult.Page(
                data = response,
                prevKey = null, // no paging backward
                nextKey = if (response.size < query.limit) null else query.copy(offset = query.offset + query.limit)
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}