package me.nanova.subspace.di

import me.nanova.subspace.data.api.QBApiService
import me.nanova.subspace.data.api.QBAuthService
import me.nanova.subspace.data.api.TransmissionApiService
import me.nanova.subspace.data.client.QBittorrentClientImpl
import me.nanova.subspace.data.client.TransmissionClientImpl
import me.nanova.subspace.domain.client.TorrentClient
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.ClientType
import me.nanova.subspace.domain.repo.AccountRepo
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient

@Singleton
class TorrentClientFactory @Inject constructor(
    private val accountRepo: AccountRepo,
    private val retrofitFactory: RetrofitFactory,
    @Named("qbOkHttpClient") private val qbOkHttpClientProvider: Provider<OkHttpClient>,
    // Assuming a default OkHttpClient can be used for Transmission for now, or a specific one like @Named("trOkHttpClient")
    @Named("defaultOkHttpClient") private val defaultOkHttpClientProvider: Provider<OkHttpClient>,
    private val transmissionApiServiceProvider: Provider<TransmissionApiService> // Added provider
) {

    // Cache for client-specific services
    private val qbApiServiceCache = mutableMapOf<Long, QBApiService>()
    private val qbAuthServiceCache = mutableMapOf<Long, QBAuthService>()
    // Cache for TransmissionApiService if it becomes stateful or costly to create
    // private val transmissionApiServiceCache = mutableMapOf<Long, TransmissionApiService>()


    fun getClient(accountId: Long): TorrentClient {
        val account = runBlocking { accountRepo.getAccount(accountId).first() }
            ?: throw IllegalArgumentException("Account not found with ID: $accountId")

        return when (account.clientType) {
            ClientType.QBITTORRENT -> {
                val qbOkHttpClient = qbOkHttpClientProvider.get()
                val retrofit = retrofitFactory.getRetrofit(accountId, qbOkHttpClient)
                val qbApiService = qbApiServiceCache.getOrPut(accountId) {
                    retrofit.create(QBApiService::class.java)
                }
                val qbAuthService = qbAuthServiceCache.getOrPut(accountId) {
                    retrofit.create(QBAuthService::class.java)
                }
                QBittorrentClientImpl(account, qbApiService, qbAuthService)
            }
            ClientType.TRANSMISSION -> {
                // Use the injected provider for TransmissionApiService
                val transmissionApiService = transmissionApiServiceProvider.get()
                // Note: The Retrofit instance used by transmissionApiServiceProvider might be the default one
                // from ApiModule, which uses placeholder.local. This is fine for a placeholder client.
                // If TransmissionApiService needed account-specific URL, RetrofitFactory would be used here too.
                TransmissionClientImpl(account, transmissionApiService)
            }
            // Add other client types as needed
        }
    }
}
