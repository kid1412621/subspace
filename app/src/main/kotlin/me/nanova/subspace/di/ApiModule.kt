package me.nanova.subspace.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.nanova.subspace.data.api.QTApiService

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
//    @Singleton
    fun provideQTApiService(factory: RetrofitFactory): QTApiService {
        return factory.retrofit().create(QTApiService::class.java)
    }
}