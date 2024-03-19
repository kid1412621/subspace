package me.nanova.subspace.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.nanova.subspace.data.AccountRepoImpl
import me.nanova.subspace.data.Storage
import me.nanova.subspace.data.TorrentRepoImpl
import me.nanova.subspace.data.api.QTApiService
import me.nanova.subspace.data.db.AccountDao
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.domain.repo.AccountRepo
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Provider

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {


    @Provides
    fun provideAccountRepo(accountDao: AccountDao, storage: Storage): AccountRepo {
        return AccountRepoImpl(accountDao, storage)
    }

    @Provides
    fun provideTorrentRepo(
        torrentDao: TorrentDao,
        storage: Storage,
        apiService: Provider<QTApiService>
    ): TorrentRepo {
        return TorrentRepoImpl(torrentDao, storage, apiService)
    }
}