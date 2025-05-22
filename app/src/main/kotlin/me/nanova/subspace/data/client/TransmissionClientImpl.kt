package me.nanova.subspace.data.client

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import me.nanova.subspace.data.api.TransmissionApiService
import me.nanova.subspace.domain.client.TorrentClient
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.CategoryInfo
import me.nanova.subspace.domain.model.GenericTorrentFilter
import me.nanova.subspace.domain.model.ServerState
import me.nanova.subspace.domain.model.Torrent
import javax.inject.Inject

class TransmissionClientImpl @Inject constructor(
    private val account: Account,
    private val transmissionApiService: TransmissionApiService
    // Potentially an OkHttpClient if different from qBittorrent's
) : TorrentClient {

    override suspend fun login(account: Account): Flow<Boolean> {
        // Transmission authentication is typically handled by an X-Transmission-Session-Id header,
        // which is obtained on a 409 response and then resent.
        // This logic is often in an OkHttp Interceptor.
        // For now, assume login success if service is reachable or defer to interceptor.
        return flow {
            // Placeholder: In a real scenario, might try a simple call to ensure connectivity
            // and that the interceptor handles the session ID.
            // For now, just emit true as a placeholder.
            emit(true)
            // throw NotImplementedError("TransmissionClient: login not yet implemented, relies on interceptor for session ID")
        }
    }

    override suspend fun logout() {
        // Transmission doesn't have a formal logout RPC call.
        // Clearing session ID might be handled by clearing cookies/storage if applicable,
        // or simply doing nothing if the session ID is managed per-request by an interceptor.
        // No specific action required here for placeholder.
    }

    override fun getTorrents(filter: GenericTorrentFilter): Flow<PagingData<Torrent>> {
        return flowOf(PagingData.empty())
        // Or:
        // flow {
        //     emit(PagingData.empty<Torrent>())
        //     throw NotImplementedError("TransmissionClient: getTorrents not yet implemented")
        // }
    }

    override fun getTorrentDetails(id: String): Flow<Torrent> {
        return flow { throw NotImplementedError("TransmissionClient: getTorrentDetails not yet implemented") }
    }

    override suspend fun startTorrents(ids: List<String>): Flow<Boolean> {
        return flow { throw NotImplementedError("TransmissionClient: startTorrents not yet implemented") }
    }

    override suspend fun stopTorrents(ids: List<String>): Flow<Boolean> {
        return flow { throw NotImplementedError("TransmissionClient: stopTorrents not yet implemented") }
    }

    override suspend fun pauseTorrents(ids: List<String>): Flow<Boolean> {
        // Transmission uses "torrent-stop" for pausing.
        return flow { throw NotImplementedError("TransmissionClient: pauseTorrents (uses stop) not yet implemented") }
    }

    override suspend fun deleteTorrents(ids: List<String>, deleteData: Boolean): Flow<Boolean> {
        return flow { throw NotImplementedError("TransmissionClient: deleteTorrents not yet implemented") }
    }

    override fun getCategories(): Flow<Map<String, CategoryInfo>> {
        // Transmission doesn't have categories in the same way qBittorrent does.
        // It uses download directories. This might need to be adapted or return empty.
        return flow {
            emit(emptyMap<String, CategoryInfo>())
            // throw NotImplementedError("TransmissionClient: getCategories not yet implemented (Transmission uses download directories)")
        }
    }

    override fun getTags(): Flow<List<String>> {
        // Transmission doesn't natively support tags like qBittorrent.
        // Labels can be used, but they are part of a more complex setup.
        return flow {
            emit(emptyList<String>())
            // throw NotImplementedError("TransmissionClient: getTags not yet implemented (Transmission uses labels, not directly tags)")
        }
    }

    override fun getServerState(): Flow<ServerState> {
        return flow { throw NotImplementedError("TransmissionClient: getServerState not yet implemented") }
    }

    override fun getAppVersion(): Flow<String> {
        return flow { throw NotImplementedError("TransmissionClient: getAppVersion not yet implemented") }
    }
}
