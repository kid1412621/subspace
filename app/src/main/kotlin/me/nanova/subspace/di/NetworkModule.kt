package me.nanova.subspace.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.nanova.subspace.domain.QtCookieInterceptor
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.repo.AccountRepo
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitFactory @Inject constructor(
    private val httpClient: OkHttpClient,
    private val accountRepo: AccountRepo
) {
    private val retrofitMap = mutableMapOf<Account, Retrofit>()

    fun create(): Retrofit {
        return runBlocking { accountRepo.currentAccount.first() }?.let {
            retrofitMap.getOrPut(it) {
                // Create and return new Retrofit instance
                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()

                return Retrofit.Builder()
                    .client(httpClient)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .baseUrl(it.url)
                    .build()
            }
        } ?: Retrofit.Builder().baseUrl("https://xxx.local").build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Singleton
    @Provides
    fun provideRetrofitFactory(
        okHttpClient: OkHttpClient,
        accountRepo: AccountRepo
    ): RetrofitFactory = RetrofitFactory(okHttpClient, accountRepo)

    //    @Provides
    fun provideRetrofit(
        httpClient: OkHttpClient,
        accountRepo: AccountRepo
    ): Retrofit {
        return runBlocking { accountRepo.currentAccount.first() }?.let {

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            Retrofit.Builder()
                .client(httpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .baseUrl(it.url)
                .build()
        } ?: Retrofit.Builder().baseUrl("http://xx.local").build()

    }

//    fun httpClientByType(type: AccountType): OkHttpClient {
//        when (type) {
//            AccountType.QT -> createOkHttpClient();
//            AccountType.TRANSMISSION -> TODO()
//        }
//        return TODO("Provide the return value")
//    }

    @Provides
    fun provideOkHttpClient(
        cookieInterceptor: QtCookieInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Log HTTP request and response details
        }

        return OkHttpClient.Builder()
//            .authenticator(QtAuthenticator(authApiService))
            // qt returns 403, okhttp authenticator only response to 401/407
            .addInterceptor(loggingInterceptor)
            .addNetworkInterceptor(cookieInterceptor)
            .build()
    }

}




