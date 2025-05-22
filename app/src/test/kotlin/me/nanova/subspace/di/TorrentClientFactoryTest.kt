package me.nanova.subspace.di

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.nanova.subspace.data.api.QBApiService
import me.nanova.subspace.data.api.QBAuthService
import me.nanova.subspace.data.api.TransmissionApiService
import me.nanova.subspace.data.client.QBittorrentClientImpl
import me.nanova.subspace.data.client.TransmissionClientImpl
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.ClientType
import me.nanova.subspace.domain.repo.AccountRepo
import okhttp3.OkHttpClient
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import javax.inject.Provider

@ExperimentalCoroutinesApi
class TorrentClientFactoryTest {

    private lateinit var accountRepo: AccountRepo
    private lateinit var retrofitFactory: RetrofitFactory
    private lateinit var qbOkHttpClientProvider: Provider<OkHttpClient>
    private lateinit var defaultOkHttpClientProvider: Provider<OkHttpClient>
    private lateinit var transmissionApiServiceProvider: Provider<TransmissionApiService>

    // Mocks for services created by RetrofitFactory (not directly injected into TorrentClientFactory, but needed for its operation)
    private lateinit var mockQbApiService: QBApiService
    private lateinit var mockQbAuthService: QBAuthService
    private lateinit var mockRetrofit: retrofit2.Retrofit


    private lateinit var factory: TorrentClientFactory

    private val accountId = 1L
    private val qbAccount = Account(
        id = accountId,
        name = "QB Account",
        clientType = ClientType.QBITTORRENT,
        url = "http://qb.local",
        type = me.nanova.subspace.domain.model.AccountType.QBITTORENT // Deprecated
    )
    private val trAccount = Account(
        id = accountId + 1,
        name = "TR Account",
        clientType = ClientType.TRANSMISSION,
        url = "http://tr.local",
        type = me.nanova.subspace.domain.model.AccountType.TRANSMISSION // Deprecated
    )

    @Before
    fun setUp() {
        accountRepo = mockk()
        retrofitFactory = mockk()
        qbOkHttpClientProvider = mockk()
        defaultOkHttpClientProvider = mockk()
        transmissionApiServiceProvider = mockk()

        mockQbApiService = mockk()
        mockQbAuthService = mockk()
        mockRetrofit = mockk()


        // Mock OkHttpClient providers to return a mock OkHttpClient
        val mockOkHttpClient = mockk<OkHttpClient>()
        every { qbOkHttpClientProvider.get() } returns mockOkHttpClient
        every { defaultOkHttpClientProvider.get() } returns mockOkHttpClient

        // Mock RetrofitFactory to return the mockRetrofit instance
        every { retrofitFactory.getRetrofit(any(), any()) } returns mockRetrofit

        // Mock the creation of qBittorrent specific services from the mockRetrofit
        every { mockRetrofit.create(QBApiService::class.java) } returns mockQbApiService
        every { mockRetrofit.create(QBAuthService::class.java) } returns mockQbAuthService

        // Mock TransmissionApiService provider
        val mockTransmissionApiService = mockk<TransmissionApiService>()
        every { transmissionApiServiceProvider.get() } returns mockTransmissionApiService


        factory = TorrentClientFactory(
            accountRepo,
            retrofitFactory,
            qbOkHttpClientProvider,
            defaultOkHttpClientProvider,
            transmissionApiServiceProvider
        )
    }

    @Test
    fun `getClient for QBITTORRENT should return QBittorrentClientImpl`() = runTest {
        coEvery { accountRepo.getAccount(qbAccount.id) } returns flowOf(qbAccount)

        val client = factory.getClient(qbAccount.id)

        assertTrue(client is QBittorrentClientImpl)
        verify { accountRepo.getAccount(qbAccount.id) }
        verify { qbOkHttpClientProvider.get() }
        verify { retrofitFactory.getRetrofit(qbAccount.id, any()) }
    }

    @Test
    fun `getClient for TRANSMISSION should return TransmissionClientImpl`() = runTest {
        coEvery { accountRepo.getAccount(trAccount.id) } returns flowOf(trAccount)

        val client = factory.getClient(trAccount.id)

        assertTrue(client is TransmissionClientImpl)
        verify { accountRepo.getAccount(trAccount.id) }
        verify { transmissionApiServiceProvider.get() }
        // Verify that qb-specific OkHttpClient or Retrofit was NOT requested for Transmission if not needed
        // verify(exactly = 0) { qbOkHttpClientProvider.get() }
        // verify(exactly = 0) { retrofitFactory.getRetrofit(trAccount.id, any()) } // This depends on how TransmissionApiService is set up
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getClient with non-existent accountId should throw IllegalArgumentException`() = runTest {
        coEvery { accountRepo.getAccount(999L) } returns flowOf(null)
        factory.getClient(999L)
    }
}
