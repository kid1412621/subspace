package me.nanova.subspace.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Upsert
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow
import me.nanova.subspace.domain.model.QTFilterState
import me.nanova.subspace.domain.model.QTListParams
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.model.TorrentEntity

@Dao
interface TorrentDao {
    @Query("SELECT * FROM torrent WHERE account_id = :accountId")
    fun getAll(accountId: Long): Flow<List<TorrentEntity>>

    @RawQuery(observedEntities = [TorrentEntity::class])
    fun pagingSource(query: SupportSQLiteQuery): PagingSource<Int, Torrent>

    @Upsert
    suspend fun insertAll(torrents: List<TorrentEntity>)

    @Query("DELETE FROM torrent WHERE account_id = :accountId")
    suspend fun clearAll(accountId: Long)

    companion object {
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