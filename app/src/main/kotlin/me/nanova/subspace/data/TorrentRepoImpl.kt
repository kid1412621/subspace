package me.nanova.subspace.data

import android.content.Context
import android.net.Uri
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.nanova.subspace.data.api.QBApiService
import me.nanova.subspace.data.api.QBAuthService
import me.nanova.subspace.data.api.dto.toDomain
import me.nanova.subspace.data.db.AppDatabase
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.data.db.TorrentDao.Companion.buildQuery
import me.nanova.subspace.data.db.entity.toDomain
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.AddTorrentParams
import me.nanova.subspace.domain.model.FilePriority
import me.nanova.subspace.domain.model.QBCategories
import me.nanova.subspace.domain.model.QBListParams
import me.nanova.subspace.domain.model.ServerTransferInfo
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.model.TorrentFile
import me.nanova.subspace.domain.model.TorrentPeer
import me.nanova.subspace.domain.model.TorrentProperties
import me.nanova.subspace.domain.model.TorrentTracker
import me.nanova.subspace.domain.repo.TorrentRepo
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Provider

class TorrentRepoImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val torrentDao: TorrentDao,
    private val apiService: Provider<QBApiService>,
    private val authService: QBAuthService,
    // NetworkStatusMonitor is no longer needed directly here,
    // it's used in the ViewModel to control when torrents() is called.
    // private val networkStatusMonitor: NetworkStatusMonitor
) : TorrentRepo {

    override suspend fun login(url: String, username: String, password: String): String {
        val res = authService.login(url, username, password)
        if (!res.isSuccessful) {
            if (res.code() == 403) {
                throw RuntimeException("Wrong username or password.")
            }
            throw RuntimeException("Cannot connect to qBittorent service.")
        }
        val cookie = res.headers()["Set-Cookie"]
        if (cookie.isNullOrBlank()) {
            throw RuntimeException("Failed to retrieve cookie.")
        }
        return cookie
    }

    override suspend fun appVersion(url: String, cookie: String) =
        authService.appVersion(url, cookie).removePrefix("v")

    override suspend fun apiVersion() = apiService.get().apiVersion()

    companion object {
        const val PAGE_SIZE = 20
    }

    @OptIn(ExperimentalPagingApi::class)
    // Removed NetworkStatusMonitor parameter
    override fun torrents(account: Account, filter: QBListParams): Flow<PagingData<Torrent>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = 1,
                enablePlaceholders = true,
            ),
            remoteMediator = TorrentRemoteMediator(
                account.id,
                filter,
                database,
                apiService.get(),
                // NetworkStatusMonitor is no longer passed to the RemoteMediator
            ),
            pagingSourceFactory = {
                torrentDao.pagingSource(buildQuery(account.id, filter))
            }
        ).flow.map { pagingData ->
            pagingData.map { entity -> entity.toDomain(account.type) }
        }
    }

    override fun categories(): Flow<QBCategories> {
        return flow {
            emit(apiService.get().categories() ?: emptyMap())
        }
    }

    override fun tags(): Flow<List<String>> {
        return flow {
            emit(apiService.get().tags())
        }
    }

    override suspend fun stop(torrents: List<String>) {
        apiService.get().stop(torrents.joinToString("|"))
    }

    override suspend fun start(torrents: List<String>) {
        apiService.get().start(torrents.joinToString("|"))
    }

    override suspend fun addTorrent(params: AddTorrentParams) {
        val uri = params.fileUri?.let(Uri::parse)
        if (uri == null) {
            submitTorrent(params)
            return
        }
        require(uri.scheme == "content")
        withContext(Dispatchers.IO) {
            val file = File.createTempFile("import-", ".torrent", context.cacheDir)
            try {
                val input = try {
                    context.contentResolver.openInputStream(uri)
                } catch (e: SecurityException) {
                    throw IOException("Unable to read the torrent file.", e)
                } ?: throw IOException("Unable to open the torrent file.")
                input.use { source -> file.outputStream().use { source.copyTo(it) } }
                val upload = object : RequestBody() {
                    override fun contentType() = "application/x-bittorrent".toMediaType()
                    override fun contentLength() = file.length()
                    override fun isOneShot() = true

                    override fun writeTo(sink: BufferedSink) {
                        file.source().use { sink.writeAll(it) }
                    }
                }
                submitTorrent(params, upload)
            } finally {
                file.delete()
            }
        }
    }

    private suspend fun submitTorrent(params: AddTorrentParams, upload: RequestBody? = null) {
        val body = MultipartBody.Builder().setType(MultipartBody.FORM).apply {
            if (upload == null) {
                addFormDataPart("urls", params.urls)
            } else {
                addFormDataPart("torrents", "import.torrent", upload)
            }
            params.savepath?.let { addFormDataPart("savepath", it) }
            params.category?.let { addFormDataPart("category", it) }
            params.tags?.let { addFormDataPart("tags", it) }
            addFormDataPart("stopped", params.paused.toString())
            addFormDataPart("paused", params.paused.toString())
            addFormDataPart("sequentialDownload", params.sequentialDownload.toString())
            addFormDataPart("firstLastPiecePrio", params.firstLastPiecePriority.toString())
        }.build()
        val response = apiService.get().addTorrent(body).use { it.string() }
        if (response.trim().startsWith("Fails", ignoreCase = true)) {
            throw IOException("Server rejected the torrent.")
        }
    }

    override suspend fun delete(torrents: List<String>, deleteFiles: Boolean) {
        apiService.get().delete(torrents.joinToString("|"), deleteFiles)
    }

    override suspend fun recheck(torrents: List<String>) {
        apiService.get().recheck(torrents.joinToString("|"))
    }

    override suspend fun getTorrentProperties(hash: String): TorrentProperties {
        val service = apiService.get()
        val properties = service.properties(hash).toDomain()
        val torrent = service.list(mapOf("hashes" to hash)).firstOrNull()
            ?: throw IOException("Torrent not found")
        return properties.copy(progress = torrent.progress)
    }

    override suspend fun getTorrentFiles(hash: String): List<TorrentFile> {
        return apiService.get().files(hash).mapIndexed { index, file -> file.toDomain(index) }
    }

    override suspend fun setFilePriority(hash: String, fileIds: List<Int>, priority: FilePriority) {
        apiService.get().setFilePriority(hash, fileIds.joinToString("|"), priority.value)
    }

    override suspend fun getTorrentTrackers(hash: String): List<TorrentTracker> {
        return apiService.get().trackers(hash).map { it.toDomain() }
    }

    override suspend fun getTorrentPeers(hash: String): List<TorrentPeer> {
        val response = apiService.get().peers(hash)
        return response.peers?.map { (key, dto) ->
            val fallbackIp = key.substringBeforeLast(":", key).removeSurrounding("[", "]")
            val fallbackPort = key.substringAfterLast(":", "").toIntOrNull() ?: 0
            dto.toDomain(fallbackIp, fallbackPort)
        } ?: emptyList()
    }

    override suspend fun reannounce(torrents: List<String>) {
        apiService.get().reannounce(torrents.joinToString("|"))
    }

    override suspend fun getTransferInfo(): ServerTransferInfo {
        val service = apiService.get()
        val info = service.transferInfo()
        val altMode = try {
            service.speedLimitsMode() == 1
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }
        return info.toDomain(altMode)
    }

    override suspend fun toggleSpeedLimitsMode() {
        apiService.get().toggleSpeedLimitsMode()
    }
}