package me.nanova.subspace.data.client

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.nanova.subspace.data.api.QBApiService
import me.nanova.subspace.data.api.QBAuthService
import me.nanova.subspace.data.mapper.mapQBStateToDomainState
import me.nanova.subspace.domain.client.TorrentClient
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.CategoryInfo
import me.nanova.subspace.domain.model.GenericTorrentFilter
import me.nanova.subspace.domain.model.ServerState
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.model.toModel
import me.nanova.subspace.exception.AuthenticationException
import me.nanova.subspace.exception.NetworkException
import me.nanova.subspace.exception.OperationFailedException
import me.nanova.subspace.exception.RateLimitException
import me.nanova.subspace.exception.ResourceNotFoundException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class QBittorrentClientImpl @Inject constructor(
    private val account: Account, // Assuming account details are passed upon instantiation
    private val qbApiService: QBApiService,
    private val qbAuthService: QBAuthService
) : TorrentClient {

    override suspend fun login(account: Account): Flow<Boolean> = flow {
        try {
            val response = qbAuthService.login(account.url, account.user, account.pass)
            if (response.isSuccessful && response.headers()["Set-Cookie"] != null) {
                // Cookie is typically handled by an interceptor, but we confirm success here
                emit(true)
            } else {
                if (response.code() == 403) {
                    throw AuthenticationException("Wrong username or password.")
                } else {
                    throw NetworkException("Cannot connect to qBittorrent service. HTTP Status: ${response.code()}")
                }
            }
        } catch (e: HttpException) {
            throw NetworkException("Cannot connect to qBittorrent service. HTTP Status: ${e.code()}", e)
        } catch (e: IOException) {
            throw NetworkException("Network error during login: ${e.message}", e)
        } catch (e: Exception) {
            if (e is AuthenticationException || e is NetworkException) throw e // Re-throw known exceptions
            throw OperationFailedException("Login failed due to an unexpected error: ${e.message}", e)
        }
    }

    override suspend fun logout() {
        // qBittorrent logout might involve clearing cookies, which is handled by CookieManager/Interceptor
        // Or calling a specific logout endpoint if available (not standard for qB)
        // For now, this can be a no-op if cookie clearing is managed elsewhere
    }

    override fun getTorrents(filter: GenericTorrentFilter): Flow<PagingData<Torrent>> {
        // This implementation detail is tricky with Paging3.
        // The actual fetching for paging is done in TorrentRemoteMediator.
        // This function in the client might be more about fetching a single page
        // or perhaps not directly used by TorrentRepoImpl if PagingSource/RemoteMediator
        // directly use the qbApiService.
        // For now, let's assume it fetches a list (non-paged for simplicity here,
        // though the interface returns PagingData, which is a mismatch to resolve)
        // Or, this method is what TorrentRemoteMediator would call.
        // This needs further refinement based on how RemoteMediator is structured.
        // For now, let's assume it's a direct, non-paged call for simplicity of this class.
        // A proper implementation for PagingData would involve creating a Pager here.
        throw UnsupportedOperationException("Paging should be handled by TorrentRemoteMediator or a PagingSource using this client's services")
    }


    fun fetchTorrents(filter: GenericTorrentFilter, page: Int, pageSize: Int): Flow<List<Torrent>> = flow {
        try {
            // This is a more realistic method that RemoteMediator might use.
            val qbFilter = filter.status?.firstOrNull()?.name?.lowercase() ?: "all" // Simplified filter mapping
            val torrentsResponse = qbApiService.torrents(
                filter = qbFilter,
                category = filter.category,
                tags = filter.tags?.joinToString(","),
                sort = "name", // Example sort
                reverse = false,
                limit = pageSize,
                offset = (page - 1) * pageSize,
                hashes = null // Or some query mapping from filter.query
            )
            emit(torrentsResponse.map { it.toModel(mapQBStateToDomainState(it.state)) })
        } catch (e: HttpException) {
            when (e.code()) {
                401, 403 -> throw AuthenticationException(cause = e)
                404 -> throw ResourceNotFoundException("Torrent list not found.", e)
                429 -> throw RateLimitException(cause = e)
                else -> throw NetworkException("API call failed: ${e.code()} ${e.message()}", e)
            }
        } catch (e: IOException) {
            throw NetworkException("Network error fetching torrents: ${e.message}", e)
        } catch (e: Exception) {
            throw OperationFailedException("Failed to fetch torrents: ${e.message}", e)
        }
    }


    override fun getTorrentDetails(id: String): Flow<Torrent> = flow {
        try {
            // qBittorrent API might not have a dedicated "get by ID" if ID is not hash.
            // Assuming 'id' is the torrent hash.
            val torrentsResponse = qbApiService.torrents(hashes = id)
            val torrentInfo = torrentsResponse.firstOrNull()
            if (torrentInfo != null) {
                emit(torrentInfo.toModel(mapQBStateToDomainState(torrentInfo.state)))
            } else {
                throw ResourceNotFoundException("Torrent with hash $id not found")
            }
        } catch (e: HttpException) {
            when (e.code()) {
                401, 403 -> throw AuthenticationException(cause = e)
                404 -> throw ResourceNotFoundException("Torrent details for $id not found.", e)
                429 -> throw RateLimitException(cause = e)
                else -> throw NetworkException("API call failed for getTorrentDetails: ${e.code()} ${e.message()}", e)
            }
        } catch (e: IOException) {
            throw NetworkException("Network error getting torrent details: ${e.message}", e)
        } catch (e: Exception) {
            if (e is ResourceNotFoundException) throw e
            throw OperationFailedException("Failed to get torrent details for $id: ${e.message}", e)
        }
    }

    private suspend fun <T> executeApiCall(
        apiCall: suspend () -> T,
        errorMessagePrefix: String
    ): T {
        try {
            return apiCall()
        } catch (e: HttpException) {
            when (e.code()) {
                401, 403 -> throw AuthenticationException(cause = e)
                404 -> throw ResourceNotFoundException("$errorMessagePrefix: Resource not found.", e)
                429 -> throw RateLimitException(cause = e)
                else -> throw NetworkException("$errorMessagePrefix: API call failed: ${e.code()} ${e.message()}", e)
            }
        } catch (e: IOException) {
            throw NetworkException("$errorMessagePrefix: Network error: ${e.message}", e)
        } catch (e: Exception) {
            throw OperationFailedException("$errorMessagePrefix: An unexpected error occurred: ${e.message}", e)
        }
    }


    override suspend fun startTorrents(ids: List<String>): Flow<Boolean> = flow {
        executeApiCall( { qbApiService.start(ids.joinToString("|")) }, "Failed to start torrents" )
        emit(true) // qBittorrent API usually returns 200 OK on success
    }

    override suspend fun stopTorrents(ids: List<String>): Flow<Boolean> = flow {
        executeApiCall( { qbApiService.stop(ids.joinToString("|")) }, "Failed to stop torrents" )
        emit(true)
    }

    override suspend fun pauseTorrents(ids: List<String>): Flow<Boolean> = flow {
        executeApiCall( { qbApiService.pause(ids.joinToString("|")) }, "Failed to pause torrents" )
        emit(true)
    }

    override suspend fun deleteTorrents(ids: List<String>, deleteData: Boolean): Flow<Boolean> = flow {
        executeApiCall( { qbApiService.delete(ids.joinToString("|"), deleteData) }, "Failed to delete torrents" )
        emit(true)
    }

    override fun getCategories(): Flow<Map<String, CategoryInfo>> = flow {
        val categories = executeApiCall( { qbApiService.categories() }, "Failed to get categories" )
            ?.mapValues { CategoryInfo(name = it.key, savePath = it.value.savePath) } ?: emptyMap()
        emit(categories)
    }

    override fun getTags(): Flow<List<String>> = flow {
        val tags = executeApiCall( { qbApiService.tags() }, "Failed to get tags" )
        emit(tags)
    }

    override fun getServerState(): Flow<ServerState> {
        // This would require mapping qBittorrent's server state (if available)
        // to the generic ServerState model.
        // qbApiService.getServerState() or similar would be needed.
        // For now, wrapping in similar exception handling if it were implemented.
        throw UnsupportedOperationException("getServerState not implemented for qBittorrent yet")
    }

    override fun getAppVersion(): Flow<String> = flow {
        val version = executeApiCall( { qbAuthService.appVersion(account.url, "") }, "Failed to get app version" )
            .removePrefix("v") // Cookie might be needed
        emit(version)
    }
}
