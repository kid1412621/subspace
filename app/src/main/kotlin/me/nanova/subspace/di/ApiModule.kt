package me.nanova.subspace.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.nanova.subspace.data.api.QBApiService
import me.nanova.subspace.data.api.QBAuthService
import retrofit2.Retrofit
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
//    @Singleton
    fun provideQBApiService(factory: RetrofitFactory): QBApiService {
        return factory.retrofit().create(QBApiService::class.java)
    }

    @Provides
    fun provideQBAuthService(@Named("defaultRetrofit") retrofit: Retrofit): QBAuthService =
        retrofit.create(QBAuthService::class.java)
}