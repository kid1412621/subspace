package me.nanova.subspace.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.nanova.subspace.data.api.QBApiService
import me.nanova.subspace.data.api.QBAuthService
import me.nanova.subspace.domain.model.ClientType
import me.nanova.subspace.domain.repo.AccountRepo
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Provider
import okhttp3.OkHttpClient


@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    // Note: QBApiService and QBAuthService are now created on-demand by TorrentClientFactory.
    // If they were needed as globally scoped singletons (which they aren't, as they are account-specific),
    // this module would need a way to get the "active" or "current" account's Retrofit instance.
    // However, with the factory pattern, these direct providers might become obsolete or change role.

    // This provider is for the QBAuthService used for login, which might not have an accountId yet.
    // Or, it could be used for a "default" instance if needed, but login is account-specific.
    // For now, let's assume QBAuthService for a specific account is created in TorrentClientFactory.
    // If there's a global/default QBAuthService needed (e.g. for an initial login screen before an account is selected),
    // it would use the defaultRetrofit.
    @Provides
    fun provideDefaultQBAuthService(@Named("defaultRetrofit") retrofit: Retrofit): QBAuthService =
        retrofit.create(QBAuthService::class.java)

    // QBApiService is definitely account-specific, so it doesn't make sense to provide a single instance here.
    // It will be created within TorrentClientFactory using the account-specific Retrofit instance.
    // Thus, the direct provider for QBApiService is removed from here.
    // fun provideQBApiService(...) - REMOVED

    @Provides
    fun provideTransmissionApiService(@Named("defaultRetrofit") retrofit: Retrofit): me.nanova.subspace.data.api.TransmissionApiService =
        retrofit.create(me.nanova.subspace.data.api.TransmissionApiService::class.java)
}