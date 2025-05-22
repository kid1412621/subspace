package me.nanova.subspace.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.nanova.subspace.data.api.QBCookieInterceptor
import me.nanova.subspace.domain.repo.AccountRepo
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class RetrofitFactory @Inject constructor(
    private val moshi: Moshi,
    // Instance of OkHttpClient will be provided by Hilt based on qualifiers
    private val accountRepo: AccountRepo
) {
    private val retrofitMap = mutableMapOf<Long, Retrofit>()
    private val clientMap = mutableMapOf<Long, OkHttpClient>()

    // This function might need to accept an OkHttpClient instance or a way to get a specific one
    fun getRetrofit(accountId: Long, okHttpClient: OkHttpClient): Retrofit {
        return retrofitMap.getOrPut(accountId) {
            val account = runBlocking { accountRepo.getAccount(accountId).first() }
                ?: throw IllegalStateException("Account not found for ID: $accountId")
            Retrofit.Builder()
                .client(okHttpClient) // Use the provided OkHttpClient
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                // Note: If account.url is HTTP, ensure a Network Security Configuration
                // allows cleartext traffic for this domain, as 'usesCleartextTraffic' is false in AndroidManifest.xml.
                .baseUrl(account.url)
                .build()
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Singleton
    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Singleton
    @Provides
    @Named("defaultRetrofit")
    fun provideDefaultRetrofit(moshi: Moshi, @Named("defaultOkHttpClient") okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .client(okHttpClient) // Use the default OkHttpClient
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl("https://placeholder.local") // Default base URL
        .build()

    @Singleton
    @Provides
    fun provideRetrofitFactory(
        moshi: Moshi,
        accountRepo: AccountRepo
        // OkHttpClient is no longer directly injected here, Retrofit instances get it specifically
    ): RetrofitFactory = RetrofitFactory(moshi, accountRepo)

    private fun createBaseOkHttpClientBuilder(): OkHttpClient.Builder {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
    }

    @Singleton
    @Provides
    @Named("defaultOkHttpClient")
    fun provideDefaultOkHttpClient(): OkHttpClient {
        return createBaseOkHttpClientBuilder().build()
    }

    @Singleton
    @Provides
    @Named("qbOkHttpClient")
    fun provideQBOkHttpClient(
        cookieInterceptor: QBCookieInterceptor // This interceptor is qB specific
    ): OkHttpClient {
        return createBaseOkHttpClientBuilder()
            .addNetworkInterceptor(cookieInterceptor) // Add qB specific interceptor
            .build()
    }
}




