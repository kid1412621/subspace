package me.nanova.subspace.data.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.nanova.subspace.data.Storage
import me.nanova.subspace.domain.repo.AccountRepo
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Inject

class QBCookieInterceptor
@Inject
constructor(
    private val storage: Storage,
    private val accountRepo: AccountRepo
) : Interceptor {
    private var cookie: String? = null
    private var timestamp: Long? = null

    // default 1h session
    private val ttl = 50 * 60 * 1000

    init {
        CoroutineScope(Dispatchers.IO).launch {
            storage.qbCookie.collect { newCookie ->
                cookie = newCookie
            }
            storage.qbCookieTime.collect { cookieTime ->
                timestamp = cookieTime
            }
        }
    }

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val builder = chain.request().newBuilder()
        // Add cookie to every request if available

        if (cookie == null || timestamp == null || System.currentTimeMillis() - timestamp!! > ttl) {
            val account =
                runBlocking { accountRepo.currentAccount.first() } ?: throw RuntimeException()

            val authApiService = Retrofit.Builder()
                .baseUrl(account.url)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(QBAuthService::class.java)
            val call = authApiService.login(account.user, account.pass)
            val newCookie = call.execute().headers()["Set-Cookie"] ?: ""
            runBlocking {
                storage.saveQBCookie(newCookie)
                storage.updateQBCookieTime()
            }
        }
        cookie?.let {
            builder.addHeader("Cookie", it)
        }
        return chain.proceed(builder.build())
    }
}

