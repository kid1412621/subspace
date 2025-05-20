package me.nanova.subspace.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.nanova.subspace.data.AccountRepoImpl
import me.nanova.subspace.data.TorrentRepoImpl
import me.nanova.subspace.data.api.QBApiService
import me.nanova.subspace.data.api.QBAuthService
import me.nanova.subspace.data.db.AccountDao
import me.nanova.subspace.data.db.AppDatabase
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.domain.repo.AccountRepo
import me.nanova.subspace.domain.repo.SessionStorage
import me.nanova.subspace.domain.repo.TorrentRepo
import javax.inject.Provider

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {

    @Provides
    fun provideAccountRepo(
        accountDao: AccountDao,
        torrentDao: TorrentDao,
        storage: SessionStorage
    ): AccountRepo {
        return AccountRepoImpl(accountDao, torrentDao, storage)
    }

    @Provides
    fun provideTorrentRepo(
        appDatabase: AppDatabase,
        torrentDao: TorrentDao,
        apiService: Provider<QBApiService>,
        authService: QBAuthService
    ): TorrentRepo {
        return TorrentRepoImpl(appDatabase, torrentDao, apiService, authService)
    }

}