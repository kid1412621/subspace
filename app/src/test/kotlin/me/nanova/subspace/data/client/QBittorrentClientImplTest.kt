package me.nanova.subspace.data.client

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import me.nanova.subspace.data.api.QBApiService
import me.nanova.subspace.data.api.QBAuthService
import me.nanova.subspace.data.model.qb.QBTorrent
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.CategoryInfo
import me.nanova.subspace.domain.model.ClientType
import me.nanova.subspace.domain.model.DomainTorrentState
import me.nanova.subspace.domain.model.GenericTorrentFilter
import me.nanova.subspace.exception.AuthenticationException
import me.nanova.subspace.exception.NetworkException
import me.nanova.subspace.exception.OperationFailedException
import me.nanova.subspace.exception.ResourceNotFoundException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@ExperimentalCoroutinesApi
class QBittorrentClientImplTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var qbApiService: QBApiService
    private lateinit var qbAuthService: QBAuthService
    private lateinit var client: QBittorrentClientImpl

    private val mockAccount = Account(
        id = 1L,
        url = "http://localhost:8080",
        user = "admin",
        pass = "password",
        clientType = ClientType.QBITTORRENT,
        type = me.nanova.subspace.domain.model.AccountType.QBITTORENT // Deprecated
    )

    @Before
    fun setUp() {
        qbApiService = mockk()
        qbAuthService = mockk()
        client = QBittorrentClientImpl(mockAccount, qbApiService, qbAuthService)
    }

    // --- Login Tests ---
    @Test
    fun `login success should emit true`() = runTest {
        coEvery { qbAuthService.login(any(), any(), any()) } returns Response.success(
            "SID=cookievalue; path=/; HttpOnly", // Simulate SID in response body, though it's usually a header
            okhttp3.Headers.headersOf("Set-Cookie", "SID=cookievalue")
        )
        val result = client.login(mockAccount).first()
        assertTrue(result)
    }

    @Test(expected = AuthenticationException::class)
    fun `login with 403 should throw AuthenticationException`() = runTest {
        coEvery { qbAuthService.login(any(), any(), any()) } returns Response.error(
            403,
            "Forbidden".toResponseBody("text/plain".toMediaTypeOrNull())
        )
        client.login(mockAccount).first()
    }

    @Test(expected = NetworkException::class)
    fun `login with other HTTP error should throw NetworkException`() = runTest {
        coEvery { qbAuthService.login(any(), any(), any()) } returns Response.error(
            500,
            "Internal Server Error".toResponseBody("text/plain".toMediaTypeOrNull())
        )
        client.login(mockAccount).first()
    }

    @Test(expected = NetworkException::class)
    fun `login with IOException should throw NetworkException`() = runTest {
        coEvery { qbAuthService.login(any(), any(), any()) }.throws(IOException("Network issue"))
        client.login(mockAccount).first()
    }

    // --- FetchTorrents Tests ---
    @Test
    fun `fetchTorrents success should emit mapped torrents`() = runTest {
        val mockQBTorrents = listOf(
            QBTorrent(hash = "hash1", name = "Torrent 1", state = "downloading", size = 1000, progress = 0.5f, dlspeed = 100, upspeed = 10, eta = 3600, category = "movies", tags = "hd,action", added_on = 123, num_leechs = 1, num_seeds = 10, priority = 1, ratio = 0.0f, downloaded = 0, uploaded = 0),
            QBTorrent(hash = "hash2", name = "Torrent 2", state = "pausedUP", size = 2000, progress = 1.0f, dlspeed = 0, upspeed = 50, eta = 0, category = "tv", tags = "comedy", added_on = 456, num_leechs = 0, num_seeds = 5, priority = 0, ratio = 0.0f, downloaded = 0, uploaded = 0)
        )
        coEvery { qbApiService.torrents(any(), any(), any(), any(), any(), any(), any(), any()) } returns mockQBTorrents

        val filter = GenericTorrentFilter()
        val result = client.fetchTorrents(filter, 1, 10).first()

        assertEquals(2, result.size)
        assertEquals("Torrent 1", result[0].name)
        assertEquals(DomainTorrentState.DOWNLOADING, result[0].state)
        assertEquals("Torrent 2", result[1].name)
        assertEquals(DomainTorrentState.PAUSED, result[1].state) // was pausedUP
    }

    @Test(expected = AuthenticationException::class)
    fun `fetchTorrents with 401 should throw AuthenticationException`() = runTest {
        coEvery { qbApiService.torrents(any(), any(), any(), any(), any(), any(), any(), any()) }.throws(HttpException(Response.error<List<QBTorrent>>(401, "Unauthorized".toResponseBody())))
        client.fetchTorrents(GenericTorrentFilter(), 1, 10).first()
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `fetchTorrents with 404 should throw ResourceNotFoundException`() = runTest {
         coEvery { qbApiService.torrents(any(), any(), any(), any(), any(), any(), any(), any()) }.throws(HttpException(Response.error<List<QBTorrent>>(404, "Not Found".toResponseBody())))
        client.fetchTorrents(GenericTorrentFilter(), 1, 10).first()
    }


    // --- GetTorrentDetails Tests ---
    @Test
    fun `getTorrentDetails success should emit mapped torrent`() = runTest {
        val mockQBTorrent = QBTorrent(hash = "hash1", name = "Torrent 1", state = "downloading", size = 1000, progress = 0.5f, dlspeed = 100, upspeed = 10, eta = 3600, category = "movies", tags = "hd,action", added_on = 0, num_leechs = 0, num_seeds = 0, priority = 0, ratio = 0.0f, downloaded = 0, uploaded = 0)
        coEvery { qbApiService.torrents(hashes = "hash1") } returns listOf(mockQBTorrent)

        val result = client.getTorrentDetails("hash1").first()

        assertEquals("Torrent 1", result.name)
        assertEquals(DomainTorrentState.DOWNLOADING, result.state)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `getTorrentDetails not found should throw ResourceNotFoundException`() = runTest {
        coEvery { qbApiService.torrents(hashes = "nonexistent") } returns emptyList()
        client.getTorrentDetails("nonexistent").first()
    }

    // --- Action Tests (Start, Stop, Pause, Delete) ---
    @Test
    fun `startTorrents success should emit true`() = runTest {
        coEvery { qbApiService.start(any()) } returns Unit // Returns 200 OK
        val result = client.startTorrents(listOf("hash1")).first()
        assertTrue(result)
        coVerify { qbApiService.start("hash1") }
    }

    @Test(expected = OperationFailedException::class)
    fun `startTorrents failure should throw OperationFailedException`() = runTest {
         coEvery { qbApiService.start(any()) }.throws(HttpException(Response.error<Unit>(500, "Server Error".toResponseBody())))
        client.startTorrents(listOf("hash1")).first()
    }

    // Similar tests for stopTorrents, pauseTorrents, deleteTorrents

    @Test
    fun `stopTorrents success should emit true`() = runTest {
        coEvery { qbApiService.stop(any()) } returns Unit
        val result = client.stopTorrents(listOf("hash1")).first()
        assertTrue(result)
        coVerify { qbApiService.stop("hash1") }
    }

    @Test
    fun `pauseTorrents success should emit true`() = runTest {
        coEvery { qbApiService.pause(any()) } returns Unit
        val result = client.pauseTorrents(listOf("hash1")).first()
        assertTrue(result)
        coVerify { qbApiService.pause("hash1") }
    }

    @Test
    fun `deleteTorrents success should emit true`() = runTest {
        coEvery { qbApiService.delete(any(), any()) } returns Unit
        val result = client.deleteTorrents(listOf("hash1"), true).first()
        assertTrue(result)
        coVerify { qbApiService.delete("hash1", true) }
    }


    // --- GetCategories & GetTags Tests ---
    @Test
    fun `getCategories success should emit mapped categories`() = runTest {
        val mockQBCategories = mapOf(
            "movies" to me.nanova.subspace.data.model.qb.QBCategory(name = "movies", savePath = "/dl/movies"),
            "tv" to me.nanova.subspace.data.model.qb.QBCategory(name = "tv", savePath = "/dl/tv")
        )
        coEvery { qbApiService.categories() } returns mockQBCategories

        val result = client.getCategories().first()

        assertEquals(2, result.size)
        assertTrue(result.containsKey("movies"))
        assertEquals("/dl/movies", result["movies"]?.savePath)
    }

    @Test
    fun `getTags success should emit tags`() = runTest {
        val mockTags = listOf("action", "comedy")
        coEvery { qbApiService.tags() } returns mockTags

        val result = client.getTags().first()

        assertEquals(mockTags, result)
    }


    // --- GetAppVersion ---
    @Test
    fun `getAppVersion success should emit version string`() = runTest {
        coEvery { qbAuthService.appVersion(mockAccount.url, "") } returns "v4.3.9"
        val result = client.getAppVersion().first()
        assertEquals("4.3.9", result)
    }

    @Test(expected = NetworkException::class)
    fun `getAppVersion network error should throw NetworkException`() = runTest {
        coEvery { qbAuthService.appVersion(mockAccount.url, "") }.throws(IOException("Network issue"))
        client.getAppVersion().toList() // Use toList to trigger collection and exception
    }

}
