package me.nanova.subspace.domain

import android.content.Context
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.nanova.subspace.data.AccountType
import me.nanova.subspace.data.NetworkRepo
import me.nanova.subspace.data.Repo
import me.nanova.subspace.ui.Account
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap
import javax.inject.Inject
import javax.inject.Singleton

private val account: Account
    get() {
        TODO()
    }

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Singleton
    @Provides
    fun providePreferenceStorage(@ApplicationContext context: Context): PreferenceStorage {
        return PreferenceStorage(context)
    }


    @Provides
    fun getRetrofit(
        httpClient: OkHttpClient,
    ): Retrofit {

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .client(httpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(account.host)
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
    @Singleton
    fun provideRepo(retrofit: Retrofit): Repo {
        val apiService = retrofit.create(QtApiService::class.java)
        return NetworkRepo(apiService)
    }
}

class AddCookiesInterceptor @Inject constructor(private val preferenceStorage: PreferenceStorage) :
    Interceptor {
    private var cookie: String? = null

    init {
        CoroutineScope(Dispatchers.IO).launch {
            preferenceStorage.qtCookie.collect { newCookie ->
                cookie = newCookie
            }
        }
    }

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val builder = chain.request().newBuilder()
        // Add cookie to every request if available

        if (cookie == null) {
            val authApiService =
                Retrofit.Builder().baseUrl(account.host)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build().create(QtAuthApiService::class.java)
            val call = authApiService.login(account.user, account.password)
            val newCookie = call.execute().headers()["Set-Cookie"] ?: ""
            runBlocking {
                preferenceStorage.saveQtCookie(newCookie)
            }
        }
        cookie?.let {
            builder.addHeader("Cookie", it)
        }
        return chain.proceed(builder.build())
    }
}

interface QtAuthApiService {

    @FormUrlEncoded
    @POST("api/v2/auth/login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<String>

}

interface QtApiService {

    @GET("api/v2/app/version")
    suspend fun version(): Response<String>

    @GET("api/v2/torrents/info")
    suspend fun getTorrents(@QueryMap params: Map<String, String?>): List<Torrent>
}

data class Torrent(
    val hash: String,
    val name: String,
    @Json(name = "added_on")
    val addedOn: Long,
    val size: Long,
    val state: String
)