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
    private val httpClient: OkHttpClient,
    private val accountRepo: AccountRepo
) {
    private val retrofitMap = mutableMapOf<Long, Retrofit>()

    fun retrofit(): Retrofit {
        return runBlocking { accountRepo.currentAccount.first() }?.let {
            retrofitMap.getOrPut(it.id) {
                return Retrofit.Builder()
                    .client(httpClient)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .baseUrl(it.url)
                    .build()
            }
        } ?: throw RuntimeException("No account found")
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
    fun provideDefaultRetrofit(moshi: Moshi): Retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl("https://placeholder.local")
        .build()

    @Singleton
    @Provides
    fun provideRetrofitFactory(
        moshi: Moshi,
        okHttpClient: OkHttpClient,
        accountRepo: AccountRepo
    ): RetrofitFactory = RetrofitFactory(moshi, okHttpClient, accountRepo)

//    fun httpClientByType(type: AccountType): OkHttpClient {
//        when (type) {
//            AccountType.QBITTORENT -> createOkHttpClient();
//            AccountType.TRANSMISSION -> TODO()
//        }
//        return TODO("Provide the return value")
//    }

    @Provides
    fun provideOkHttpClient(
        cookieInterceptor: QBCookieInterceptor
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




