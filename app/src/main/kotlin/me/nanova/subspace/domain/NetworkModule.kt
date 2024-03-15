package me.nanova.subspace.domain

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.nanova.subspace.data.AccountRepoImpl
import me.nanova.subspace.data.QTRepoImpl
import me.nanova.subspace.data.api.QTApiService
import me.nanova.subspace.data.api.QTAuthService
import me.nanova.subspace.data.db.AccountDao
import me.nanova.subspace.data.db.TorrentDao
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.repo.AccountRepo
import me.nanova.subspace.domain.repo.QTRepo
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Singleton
    @Provides
    fun providePreferenceStorage(@ApplicationContext context: Context): Storage {
        return Storage(context)
    }


    @Provides
    fun getRetrofit(
        httpClient: OkHttpClient,
        accountRepo: AccountRepo
    ): Retrofit {
        val account = runBlocking {   accountRepo.currentAccount.first()}

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .client(httpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(account?.url)
            .build()
    }

//    fun httpClientByType(type: AccountType): OkHttpClient {
//        when (type) {
//            AccountType.QT -> createOkHttpClient();
//            AccountType.TRANSMISSION -> TODO()
//        }
//        return TODO("Provide the return value")
//    }

    @Provides
    fun createOkHttpClient(
        addCookiesInterceptor: AddCookiesInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Log HTTP request and response details
        }

        return OkHttpClient.Builder()
//            .authenticator(QtAuthenticator(authApiService))
            // qt returns 403, okhttp authenticator only response to 401/407
            .addInterceptor(loggingInterceptor)
            .addNetworkInterceptor(addCookiesInterceptor)
            .build()
    }

    @Provides
    fun provideAccountRepo(accountDao: AccountDao, storage: Storage): AccountRepo {
        return AccountRepoImpl(accountDao, storage)
    }

    @Provides
    fun provideQTRepo(apiService: QTApiService, torrentDao: TorrentDao): QTRepo {
        return QTRepoImpl(apiService, torrentDao)
    }

    @Provides
    @Singleton
    fun provideQTApiService(retrofit: Retrofit): QTApiService {
        return retrofit.create(QTApiService::class.java)
    }
}

class AddCookiesInterceptor
@Inject constructor(private val storage: Storage) : Interceptor {
    private var cookie: String? = null
    private var timestamp: Long? = null

    // default 1h session
    private val ttl = 50 * 60 * 1000

    init {
        CoroutineScope(Dispatchers.IO).launch {
            storage.qtCookie.collect { newCookie ->
                cookie = newCookie
            }
            storage.qtCookieTime.collect { cookieTime ->
                timestamp = cookieTime
            }
        }
    }

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val builder = chain.request().newBuilder()
        // Add cookie to every request if available

        if (cookie == null || timestamp == null || System.currentTimeMillis() - timestamp!! > ttl) {
            val authApiService =
                Retrofit.Builder().baseUrl(account.url)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build().create(QTAuthService::class.java)
            val call = authApiService.login(account.user, account.pass)
            val newCookie = call.execute().headers()["Set-Cookie"] ?: ""
            runBlocking {
                storage.saveQtCookie(newCookie)
                storage.updateQtCookieTime()
            }
        }
        cookie?.let {
            builder.addHeader("Cookie", it)
        }
        return chain.proceed(builder.build())
    }
}



