package me.nanova.subspace.ui.vm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.CategoryInfo
import me.nanova.subspace.domain.model.ClientType
import me.nanova.subspace.domain.model.DomainTorrentState
import me.nanova.subspace.domain.model.GenericTorrentFilter
import me.nanova.subspace.domain.model.Torrent
import me.nanova.subspace.domain.repo.AccountRepo
import me.nanova.subspace.domain.repo.TorrentRepo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    // Rule for LiveData and other Architecture Components
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher for coroutines
    private val testDispatcher = StandardTestDispatcher()

    // Mocks
    private lateinit var torrentRepo: TorrentRepo
    private lateinit var accountRepo: AccountRepo
    private lateinit var viewModel: HomeViewModel

    private val mockAccount = Account(
        id = 1L,
        name = "Test Account",
        clientType = ClientType.QBITTORRENT,
        url = "http://localhost:8080",
        type = me.nanova.subspace.domain.model.AccountType.QBITTORENT // Required due to @Deprecated field
    )
    private val mockAccountFlow = MutableStateFlow<Account?>(mockAccount)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        torrentRepo = mockk()
        accountRepo = mockk()

        // Mock AccountRepo behavior
        every { accountRepo.currentAccount } returns mockAccountFlow
        coEvery { accountRepo.list() } returns flowOf(listOf(mockAccount))
        coEvery { accountRepo.switch(any()) } returns Unit
        coEvery { accountRepo.delete(any()) } returns Unit
        coEvery { accountRepo.getAccount(any()) } returns flowOf(mockAccount)


        // Mock TorrentRepo behavior
        coEvery { torrentRepo.getTorrents(any(), any()) } returns flowOf(PagingData.empty())
        coEvery { torrentRepo.getCategories(any()) } returns flowOf(emptyMap())
        coEvery { torrentRepo.getTags(any()) } returns flowOf(emptyList())
        coEvery { torrentRepo.startTorrents(any(), any()) } returns Unit
        coEvery { torrentRepo.pauseTorrents(any(), any()) } returns Unit
        coEvery { torrentRepo.stopTorrents(any(), any()) } returns Unit // Added for completeness

        viewModel = HomeViewModel(torrentRepo, accountRepo)
        // Ensure currentAccount has a value for tests that rely on it being non-null initially
        // runTest { mockAccountFlow.emit(mockAccount) }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have default filter`() = runTest {
        assertEquals(GenericTorrentFilter(), viewModel.homeUiState.value.filter)
    }

    @Test
    fun `updateFilter should change filter in uiState`() = runTest {
        val newFilter = GenericTorrentFilter(query = "test query")
        viewModel.updateFilter(newFilter)
        assertEquals(newFilter, viewModel.homeUiState.value.filter)
    }

    @Test
    fun `resetFilter should set filter to default`() = runTest {
        val initialFilter = GenericTorrentFilter(query = "initial")
        viewModel.updateFilter(initialFilter) // Set a non-default filter first
        viewModel.resetFilter()
        assertEquals(GenericTorrentFilter(), viewModel.homeUiState.value.filter)
    }

    @Test
    fun `pagingDataFlow should request torrents from repo when account and filter change`() = runTest {
        // Initial collection to trigger the flow
        val job = launch(testDispatcher) {
            viewModel.pagingDataFlow.collect { }
        }

        // Wait for the initial collection to set up
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify initial call
        coVerify { torrentRepo.getTorrents(mockAccount.id, viewModel.homeUiState.value.filter) }

        // Change filter
        val newFilter = GenericTorrentFilter(category = "movies")
        viewModel.updateFilter(newFilter)
        testDispatcher.scheduler.advanceUntilIdle() // Allow flow to process the change
        coVerify { torrentRepo.getTorrents(mockAccount.id, newFilter) }

        job.cancel()
    }


    @Test
    fun `toggleTorrentStatus should start paused torrent`() = runTest {
        val torrent = mockk<Torrent>().apply {
            every { state } returns DomainTorrentState.PAUSED
            every { hash } returns "hash123"
        }
        viewModel.toggleTorrentStatus(torrent)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { torrentRepo.startTorrents(mockAccount.id, listOf("hash123")) }
    }

    @Test
    fun `toggleTorrentStatus should pause running torrent`() = runTest {
        val torrent = mockk<Torrent>().apply {
            every { state } returns DomainTorrentState.DOWNLOADING // Any non-pausable state
            every { hash } returns "hash456"
        }
        viewModel.toggleTorrentStatus(torrent)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { torrentRepo.pauseTorrents(mockAccount.id, listOf("hash456")) }
    }

    @Test
    fun `categories flow should fetch categories from repo`() = runTest {
        val mockCategories = mapOf("cat1" to CategoryInfo("cat1", "/save"))
        coEvery { torrentRepo.getCategories(mockAccount.id) } returns flowOf(mockCategories)

        val job = launch(testDispatcher) {
            viewModel.categories.collect { collectedCategories ->
                assertEquals(mockCategories, collectedCategories)
            }
        }
        testDispatcher.scheduler.advanceUntilIdle()
        job.cancel() // Cancel to prevent further collection if the flow doesn't complete
    }

    @Test
    fun `tags flow should fetch tags from repo`() = runTest {
        val mockTags = listOf("tag1", "tag2")
        coEvery { torrentRepo.getTags(mockAccount.id) } returns flowOf(mockTags)

        val job = launch(testDispatcher) {
            viewModel.tags.collect { collectedTags ->
                assertEquals(mockTags, collectedTags)
            }
        }
        testDispatcher.scheduler.advanceUntilIdle()
        job.cancel()
    }

    @Test
    fun `switchAccount should call accountRepo switch and reset filter`() = runTest {
        val newAccount = Account(id = 2L, name = "New Account", clientType = ClientType.QBITTORRENT, url = "url2", type = me.nanova.subspace.domain.model.AccountType.QBITTORENT)
        viewModel.updateFilter(GenericTorrentFilter(query = "old filter")) // Set a non-default filter

        viewModel.switchAccount(newAccount)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { accountRepo.switch(newAccount.id) }
        assertEquals(GenericTorrentFilter(), viewModel.homeUiState.value.filter) // Verify filter reset
    }

    @Test
    fun `deleteAccount should call accountRepo delete`() = runTest {
        val accountToDelete = Account(id = 3L, name = "DeleteMe", clientType = ClientType.QBITTORRENT, url = "url3", type = me.nanova.subspace.domain.model.AccountType.QBITTORENT)
        viewModel.deleteAccount(accountToDelete)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { accountRepo.delete(accountToDelete.id) }
    }
}
