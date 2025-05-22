package me.nanova.subspace.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Upsert
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.GenericTorrentFilter // Changed import
import me.nanova.subspace.domain.model.TorrentEntity

@Dao
interface TorrentDao {
    @Query("SELECT * FROM torrent WHERE account_id = :accountId")
    fun getAll(accountId: Long): Flow<List<TorrentEntity>>

    @RawQuery(observedEntities = [TorrentEntity::class])
    fun pagingSource(query: SupportSQLiteQuery): PagingSource<Int, TorrentEntity>

    @Upsert
    suspend fun insertAll(torrents: List<TorrentEntity>)

    @Query("DELETE FROM torrent WHERE account_id = :accountId")
    suspend fun clearAll(accountId: Long)

    companion object {
        fun buildQuery(
            accountId: Long,
            filter: GenericTorrentFilter, // Changed parameter type
        ): SupportSQLiteQuery {
            val queryBuilder = StringBuilder("SELECT * FROM torrent WHERE account_id = $accountId")

            // Handle status filter
            filter.status?.takeIf { it.isNotEmpty() }?.let { domainStates ->
                // Assuming TorrentEntity.state stores the string representation of DomainTorrentState
                val stateNames = domainStates.joinToString("', '") { it.name }
                queryBuilder.append(" AND state IN ('$stateNames')")
            }

            // Handle category filter
            filter.category?.let {
                queryBuilder.append(" AND category = '${it.replace("'", "''")}'") // Escape single quotes
            }

            // Handle tags filter (assuming tags are stored as comma-separated string in DB)
            filter.tags?.takeIf { it.isNotEmpty() }?.forEach { tag ->
                // Ensure tag is properly escaped for SQL LIKE clause
                val escapedTag = tag.replace("'", "''")
                queryBuilder.append(" AND (',' || tags || ',' LIKE '%,$escapedTag,%')")
            }
            
            // Handle query string (assuming it searches in torrent name)
            filter.query?.takeIf { it.isNotBlank() }?.let {
                 val escapedQuery = it.replace("'", "''")
                queryBuilder.append(" AND name LIKE '%$escapedQuery%'")
            }

            // Sorting is not part of GenericTorrentFilter in this iteration,
            // but if it were, it would be appended here.
            // Example: queryBuilder.append(" ORDER BY name ASC") 

            return SimpleSQLiteQuery(queryBuilder.toString())
        }
    }
}