package me.nanova.subspace.domain

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.nanova.subspace.data.AccountType
import me.nanova.subspace.data.NetworkRepo
import me.nanova.subspace.data.Repo
import me.nanova.subspace.ui.Account
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private lateinit var cookie: String

    private val account: Account
        get() {
            TODO()
        }

    @Provides
    fun getRetrofit(
        httpClient: OkHttpClient,
    ): Retrofit {
        val authApiService =
            Retrofit.Builder().baseUrl(account.host)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build().create(QtAuthApiService::class.java)
        val call = authApiService.login(account.user, account.password)

        cookie = call.execute().headers().get("Set-Cookie") ?: ""

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .client(httpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(account.host)
            .build()
        return retrofit
    }

    fun httpClientByType(type: AccountType): OkHttpClient {
        when (type) {
            AccountType.QT -> createOkHttpClient();
            AccountType.TRANSMISSION -> TODO()
        }
        return TODO("Provide the return value")
    }

    @Provides
    fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Log HTTP request and response details
        }


        // qt returns 403, okhttp authenticator only response to 401/407
        val cookieInterceptor = Interceptor { chain ->
            val requestBuilder: Request.Builder = chain.request().newBuilder()

            requestBuilder.header("Cookie", cookie)

            chain.proceed(requestBuilder.build())
        }

        return OkHttpClient.Builder()
//            .authenticator(QtAuthenticator(authApiService))
            .addInterceptor(loggingInterceptor)
            .addNetworkInterceptor(cookieInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRepo(retrofit: Retrofit): Repo {
        val apiService = retrofit.create(QtApiService::class.java)
        return NetworkRepo(apiService)
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