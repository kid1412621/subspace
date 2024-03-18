package me.nanova.subspace.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.nanova.subspace.data.AccountRepoImpl
import me.nanova.subspace.data.QTRepoImpl
import me.nanova.subspace.data.Storage
import me.nanova.subspace.data.api.QTApiService
import me.nanova.subspace.data.db.AccountDao
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.domain.repo.AccountRepo
import me.nanova.subspace.domain.repo.QTRepo

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {


    @Provides
    fun provideAccountRepo(accountDao: AccountDao, storage: Storage): AccountRepo {
        return AccountRepoImpl(accountDao, storage)
    }

    @Provides
    fun provideQTRepo(apiService: QTApiService, torrentDao: TorrentDao, storage: Storage): QTRepo {
        return QTRepoImpl(apiService, torrentDao, storage)
    }
}