package me.nanova.subspace.domain

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import me.nanova.subspace.data.AccountType
import me.nanova.subspace.ui.Account
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Route
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

class HttpClient(account: Account){
    private val authApiService =
        Retrofit.Builder().baseUrl(account.host)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build().create(QtAuthApiService::class.java)
    var call = authApiService.login(account.user, account.password)

    var cookie = call.execute().headers().get("Set-Cookie") ?: ""

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val retrofit = Retrofit.Builder()
        .client(httpClientByType(account.type))
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(account.host)
        .build()

    fun getRetrofit(): Retrofit{
        return retrofit
    }

    private fun httpClientByType(type: AccountType): OkHttpClient {
        when (type) {
            AccountType.QT -> createOkHttpClient();
            AccountType.TRANSMISSION -> TODO()
        }
        return TODO("Provide the return value")
    }

    private fun createOkHttpClient(): OkHttpClient {
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

//    class QtAuthenticator(val api: QtAuthApiService) : Authenticator {
//
//        override fun authenticate(route: Route?, response: okhttp3.Response): Request? {
//            if (response.request.header("Cookie") != null) {
//                return null
//            }
//
//            var call = api.login("", "")
//            return response.request.newBuilder().header("Cookie", cookie).build()
//        }
//    }

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