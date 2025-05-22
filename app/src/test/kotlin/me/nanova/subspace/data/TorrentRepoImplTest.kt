package me.nanova.subspace.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import androidx.sqlite.db.SimpleSQLiteQuery
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.nanova.subspace.data.db.AppDatabase
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.di.TorrentClientFactory
import me.nanova.subspace.domain.client.TorrentClient
import me.nanova.subspace.domain.model.CategoryInfo
import me.nanova.subspace.domain.model.GenericTorrentFilter
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.repo.TorrentRepo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class TorrentRepoImplTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var torrentRepo: TorrentRepo
    private lateinit var torrentClientFactory: TorrentClientFactory
    private lateinit var mockClient: TorrentClient
    private lateinit var database: AppDatabase
    private lateinit var torrentDao: TorrentDao

    private val accountId = 1L

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        torrentClientFactory = mockk()
        mockClient = mockk()
        database = mockk()
        torrentDao = mockk(relaxed = true) // relaxed to allow Pager/PagingSource creation

        every { torrentClientFactory.getClient(accountId) } returns mockClient
        every { database.torrentDao() } returns torrentDao

        // Mock TorrentDao.buildQuery to return a basic query
        // This is important because Pager will call it.
        mockkStatic(TorrentDao.Companion::class)
        every { TorrentDao.buildQuery(any(), any()) } returns SimpleSQLiteQuery("SELECT * FROM torrent")


        // Mock PagingSource if necessary, or ensure DAO returns a valid one
        // For simplicity, we are not testing the Pager content here, just interactions.
        every { torrentDao.pagingSource(any()) } returns mockk(relaxed = true)


        torrentRepo = TorrentRepoImpl(database, torrentDao, torrentClientFactory)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getTorrents should request client and return PagingData`() = runTest {
        val filter = GenericTorrentFilter()
        // The actual PagingData content is not tested here, just that Pager is set up
        // and the client is obtained.
        every { mockClient.getTorrents(filter) } returns flowOf(PagingData.empty<Torrent>())

        val resultFlow = torrentRepo.getTorrents(accountId, filter)
        assertNotNull(resultFlow) // Check that a flow is returned

        // To verify interactions within Pager/RemoteMediator is more complex and
        // typically done in RemoteMediatorTest. Here we mainly test TorrentRepoImpl's direct logic.
        verify { torrentClientFactory.getClient(accountId) }
        // Verification of mockClient.getTorrents might not be direct if Pager/RemoteMediator calls it.
        // For this test, we assume TorrentRemoteMediator will use the client.
    }

    @Test
    fun `getCategories should request client and call getCategories`() = runTest {
        val mockCategories = mapOf("cat1" to CategoryInfo("cat1", "/save"))
        every { mockClient.getCategories() } returns flowOf(mockCategories)

        val result = torrentRepo.getCategories(accountId).first()

        verify { torrentClientFactory.getClient(accountId) }
        verify { mockClient.getCategories() }
        assertEquals(mockCategories, result)
    }

    @Test
    fun `getTags should request client and call getTags`() = runTest {
        val mockTags = listOf("tag1", "tag2")
        every { mockClient.getTags() } returns flowOf(mockTags)

        val result = torrentRepo.getTags(accountId).first()

        verify { torrentClientFactory.getClient(accountId) }
        verify { mockClient.getTags() }
        assertEquals(mockTags, result)
    }

    @Test
    fun `startTorrents should request client and call startTorrents`() = runTest {
        val torrentHashes = listOf("hash1")
        coEvery { mockClient.startTorrents(torrentHashes) } returns flowOf(true) // Ensure flow is returned

        torrentRepo.startTorrents(accountId, torrentHashes)

        verify { torrentClientFactory.getClient(accountId) }
        coVerify { mockClient.startTorrents(torrentHashes) }
    }

    @Test
    fun `stopTorrents should request client and call stopTorrents`() = runTest {
        val torrentHashes = listOf("hash1")
        coEvery { mockClient.stopTorrents(torrentHashes) } returns flowOf(true)

        torrentRepo.stopTorrents(accountId, torrentHashes)

        verify { torrentClientFactory.getClient(accountId) }
        coVerify { mockClient.stopTorrents(torrentHashes) }
    }

    @Test
    fun `pauseTorrents should request client and call pauseTorrents`() = runTest {
        val torrentHashes = listOf("hash1")
        coEvery { mockClient.pauseTorrents(torrentHashes) } returns flowOf(true)

        torrentRepo.pauseTorrents(accountId, torrentHashes)

        verify { torrentClientFactory.getClient(accountId) }
        coVerify { mockClient.pauseTorrents(torrentHashes) }
    }

    @Test
    fun `deleteTorrents should request client and call deleteTorrents`() = runTest {
        val torrentHashes = listOf("hash1")
        val deleteData = true
        coEvery { mockClient.deleteTorrents(torrentHashes, deleteData) } returns flowOf(true)

        torrentRepo.deleteTorrents(accountId, torrentHashes, deleteData)

        verify { torrentClientFactory.getClient(accountId) }
        coVerify { mockClient.deleteTorrents(torrentHashes, deleteData) }
    }
}
