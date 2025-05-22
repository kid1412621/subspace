package me.nanova.subspace.data.api

// Placeholder for Transmission RPC methods
// Example:
// suspend fun getTorrents(...): TransmissionTorrentListResponse
// suspend fun addTorrent(...): TransmissionAddTorrentResponse
// suspend fun startTorrent(ids: List<String>): Response<Unit>
// suspend fun stopTorrent(ids: List<String>): Response<Unit>
// suspend fun removeTorrent(ids: List<String>, deleteLocalData: Boolean): Response<Unit>
// suspend fun getSessionStats(): TransmissionSessionStatsResponse
// suspend fun getSession(): TransmissionSessionResponse
// suspend fun setSession(params: TransmissionSetSessionParams): Response<Unit>

/**
 * Interface for Transmission RPC API.
 *
 * This interface will define methods for interacting with a Transmission daemon's RPC interface.
 * Each method will correspond to a specific RPC call supported by Transmission, such as
 * retrieving torrents, adding new torrents, modifying torrent states (start, stop, remove),
 * and managing session settings or statistics.
 *
 * Implementations of this interface will handle the actual network requests and response parsing,
 * likely using Retrofit with a suitable converter (e.g., Moshi or Gson) for JSON-RPC.
 *
 * Authentication with Transmission typically involves an `X-Transmission-Session-Id` header,
 * which needs to be obtained and resent with subsequent requests. This is often handled by an
 * OkHttp Interceptor.
 *
 * @see <a href="https://github.com/transmission/transmission/blob/main/docs/rpc-spec.md">Transmission RPC Specification</a>
 */
interface TransmissionApiService {
    // Methods will be added here as Transmission client functionality is developed.
    // For example:
    // @POST("/transmission/rpc")
    // suspend fun getTorrents(@Body request: TransmissionRpcRequest<GetTorrentParams>): Response<TransmissionRpcResponse<TorrentListInfo>>
}

// Define placeholder data classes for request/response if needed for method examples, e.g.:
// data class GetTorrentParams(val fields: List<String>, val ids: List<Int>? = null)
// data class TorrentListInfo(val torrents: List<TransmissionTorrent>)
// data class TransmissionTorrent(...) // Define fields according to RPC spec
// data class TransmissionRpcRequest<T>(val method: String, val arguments: T? = null, val tag: Int? = null)
// data class TransmissionRpcResponse<T>(val result: String, val arguments: T? = null, val tag: Int? = null)
