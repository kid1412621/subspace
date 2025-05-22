package me.nanova.subspace.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.nanova.subspace.data.client.QBittorrentClientImpl
import me.nanova.subspace.data.db.AppDatabase
import me.nanova.subspace.data.db.RemoteKeyDao
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.domain.model.DomainTorrentState
import me.nanova.subspace.domain.model.GenericTorrentFilter
import me.nanova.subspace.domain.model.RemoteKeys
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.model.TorrentEntity
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@ExperimentalPagingApi
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TorrentRemoteMediatorTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var torrentDao: TorrentDao
    private lateinit var remoteKeyDao: RemoteKeyDao
    private lateinit var mockClient: QBittorrentClientImpl // Using QB directly for its fetchTorrents

    private val accountId = 1L
    private val mockTorrents = listOf(
        Torrent(id = "hash1", hash = "hash1", name = "Torrent 1", state = DomainTorrentState.DOWNLOADING, addedOn = 1L, size = 1000, progress = 0.5f, eta = 3600, category = "movies", tags = "action", dlspeed = 100, upspeed = 10, ratio = 0f, leechs = 0, seeds = 0, priority = 0, downloaded = 0L, uploaded = 0L),
        Torrent(id = "hash2", hash = "hash2", name = "Torrent 2", state = DomainTorrentState.SEEDING, addedOn = 2L, size = 2000, progress = 1.0f, eta = 0, category = "tv", tags = "comedy", dlspeed = 0, upspeed = 50, ratio = 0f, leechs = 0, seeds = 0, priority = 0, downloaded = 0L, uploaded = 0L)
    )

    @Before
    fun setUp() {
        // Using an in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build() // Allow main thread queries for Robolectric tests
        torrentDao = database.torrentDao()
        remoteKeyDao = database.remoteKeyDao()
        mockClient = mockk()

        // Mock static methods if any are used internally by the class under test, e.g. Mappers if not tested directly
        // For now, assuming toModel/toEntity are simple enough not to need static mocking here.
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createMediator(client: QBittorrentClientImpl = mockClient, filter: GenericTorrentFilter = GenericTorrentFilter()): TorrentRemoteMediator {
        return TorrentRemoteMediator(
            currentAccountId = accountId,
            filter = filter,
            database = database,
            torrentClient = client
        )
    }

    @Test
    fun `initialize returns LAUNCH_INITIAL_REFRESH`() = runTest {
        val mediator = createMediator()
        val result = mediator.initialize()
        assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, result)
    }

    @Test
    fun `load REFRESH success should insert data and remote keys`() = runTest {
        coEvery { mockClient.fetchTorrents(any(), eq(1), any()) } returns flowOf(mockTorrents)

        val mediator = createMediator()
        val pagingState = PagingState<Int, TorrentEntity>(
            pages = emptyList(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 10),
            leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        coVerify { mockClient.fetchTorrents(any(), eq(1), eq(10)) } // page=1, pageSize=10
        assertEquals(2, torrentDao.getAll(accountId).first().size)
        assertEquals(2, remoteKeyDao.getAll(accountId).size)
        assertEquals(null, remoteKeyDao.remoteKeysItemId(mockTorrents[0].toEntity(accountId).id)?.prevOffset) // prevPage for first page is null
        assertEquals(2, remoteKeyDao.remoteKeysItemId(mockTorrents[0].toEntity(accountId).id)?.nextOffset) // nextPage is 2
    }

    @Test
    fun `load REFRESH success with empty data should signal endOfPagination`() = runTest {
        coEvery { mockClient.fetchTorrents(any(), eq(1), any()) } returns flowOf(emptyList())

        val mediator = createMediator()
        val pagingState = PagingState<Int, TorrentEntity>(listOf(), null, PagingConfig(10), 0)
        val result = mediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }


    @Test
    fun `load APPEND success should fetch next page and insert data`() = runTest {
         // Populate DB with initial data and remote keys for APPEND to work
        val initialTorrents = listOf(
            Torrent(id = "hash0", hash = "hash0", name = "Torrent 0", state = DomainTorrentState.DOWNLOADING, addedOn = 0L, size = 100, progress = 0.1f, eta = 100, category = "cat", tags = "tag", dlspeed = 10, upspeed = 1, ratio = 0f, leechs = 0, seeds = 0, priority = 0, downloaded = 0L, uploaded = 0L)
        )
        val initialEntities = initialTorrents.map { it.toEntity(accountId) }
        torrentDao.insertAll(initialEntities)
        remoteKeyDao.insertAll(initialEntities.map { RemoteKeys(it.id, null, 2, accountId) }) // nextOffset is page 2

        coEvery { mockClient.fetchTorrents(any(), eq(2), any()) } returns flowOf(mockTorrents) // mockTorrents are page 2

        val mediator = createMediator()
        val pagingState = PagingState(
            pages = listOf(mockk { every { data } returns initialEntities }), // Simulate that page 1 was loaded
            anchorPosition = null,
            config = PagingConfig(pageSize = 1), // pageSize = 1 for easier testing of append
            leadingPlaceholderCount = 0
        )
        val result = mediator.load(LoadType.APPEND, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        coVerify { mockClient.fetchTorrents(any(), eq(2), eq(1)) }
        assertEquals(1 + mockTorrents.size, torrentDao.getAll(accountId).first().size) // Initial + new
        // Check remote keys for newly added items
        val newTorrentEntity = mockTorrents[0].toEntity(accountId)
        val newRemoteKey = remoteKeyDao.remoteKeysItemId(newTorrentEntity.id)
        assertEquals(1, newRemoteKey?.prevOffset) // prevPage for page 2 items is 1
        assertEquals(3, newRemoteKey?.nextOffset) // nextPage for page 2 items is 3
    }


    @Test
    fun `load APPEND at end of data should return endOfPaginationReached true`() = runTest {
        val initialEntities = mockTorrents.map { it.toEntity(accountId) }
        torrentDao.insertAll(initialEntities)
        // Simulate that these were page 1, and next page (2) is the end
        remoteKeyDao.insertAll(initialEntities.map { RemoteKeys(it.id, null, 2, accountId) })

        coEvery { mockClient.fetchTorrents(any(), eq(2), any()) } returns flowOf(emptyList()) // Page 2 is empty

        val mediator = createMediator()
        val pagingState = PagingState(
            pages = listOf(mockk { every { data } returns initialEntities }),
            anchorPosition = null,
            config = PagingConfig(10),
            leadingPlaceholderCount = 0
        )
        val result = mediator.load(LoadType.APPEND, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `load PREPEND should return success and endOfPagination true`() = runTest {
        // Prepend is not really used with page-number based pagination typically
        val mediator = createMediator()
        val pagingState = PagingState<Int, TorrentEntity>(emptyList(), null, PagingConfig(10), 0)
        val result = mediator.load(LoadType.PREPEND, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `load with network error should return MediatorResult Error`() = runTest {
        coEvery { mockClient.fetchTorrents(any(), any(), any()) }.throws(IOException("Network failed"))

        val mediator = createMediator()
        val pagingState = PagingState<Int, TorrentEntity>(emptyList(), null, PagingConfig(10), 0)
        val result = mediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        assertTrue((result as RemoteMediator.MediatorResult.Error).throwable is IOException)
    }
}
