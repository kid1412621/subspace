package me.nanova.subspace.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.nanova.subspace.data.AccountRepoImpl
import me.nanova.subspace.data.NetworkStatusMonitor
import me.nanova.subspace.data.TorrentRepoImpl
import me.nanova.subspace.domain.repo.AccountRepo
import me.nanova.subspace.domain.repo.NetworkMonitor
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {

    @Binds
    @Singleton
    abstract fun bindAccountRepo(
        impl: AccountRepoImpl
    ): AccountRepo

    @Binds
    @Singleton
    abstract fun bindTorrentRepo(
        impl: TorrentRepoImpl
    ): TorrentRepo

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        impl: NetworkStatusMonitor
    ): NetworkMonitor

}
