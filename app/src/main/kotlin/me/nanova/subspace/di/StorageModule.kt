package me.nanova.subspace.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.nanova.subspace.data.storage.SessionStorageImpl
import me.nanova.subspace.domain.repo.SessionStorage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Singleton
    @Provides
    fun providePreferenceStorage(@ApplicationContext context: Context): SessionStorage {
        return SessionStorageImpl(context)
    }

}