package me.nanova.subspace.data.api

import android.util.Log
import com.google.common.net.HttpHeaders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.nanova.subspace.domain.repo.AccountRepo
import me.nanova.subspace.domain.repo.SessionStorage
import okhttp3.Interceptor
import javax.inject.Inject

class QBCookieInterceptor
@Inject
constructor(
    private val sessionStorage: SessionStorage,
    private val accountRepo: AccountRepo,
    private val authService: QBAuthService
) : Interceptor {
    private var cookie: String? = null
    private var timestamp: Long? = null

    // default 1h session
    private val ttl = 50 * 60 * 1000
    private val refreshMutex = Mutex()

    companion object {
        private const val TAG = "QBCookieInterceptor"
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            launch { sessionStorage.qbCookie.collect { cookie = it } }
            launch { sessionStorage.qbCookieTime.collect { timestamp = it } }
        }
    }

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()

        // Check and refresh cookie if needed (with double-checked locking)
        if (isCookieStale()) {
            runBlocking(Dispatchers.IO) {
                refreshMutex.withLock {
                    // Second check after acquiring the lock
                    if (isCookieStale()) {
                        Log.d(TAG, "Cookie stale/missing for ${originalRequest.url}. Refreshing.")
                        refreshCookie()
                    }
                }
            }
        }

        // Add cookie to request header
        val requestWithCookie = cookie?.takeIf { it.isNotBlank() }?.let {
            originalRequest.newBuilder()
                .addHeader(HttpHeaders.COOKIE, it)
                .also { Log.d(TAG, "Attaching cookie to request for ${originalRequest.url}") }
                .build()
        } ?: originalRequest

        return chain.proceed(requestWithCookie)
    }

    private suspend fun refreshCookie() {
        val account = accountRepo.currentAccount.first()
            ?: throw IllegalStateException("No current account found")

        val response =
            authService.login("${account.url}/api/v2/auth/login", account.user, account.pass)
        val newCookie = response.headers()[HttpHeaders.SET_COOKIE] ?: ""
        cookie = newCookie
        timestamp = System.currentTimeMillis()

        // Update storage
        sessionStorage.saveQBCookie(account.id, newCookie)
        sessionStorage.updateQBCookieTime(account.id)
    }

    private fun isCookieStale(): Boolean =
        cookie.isNullOrBlank() || timestamp == null || (System.currentTimeMillis() - timestamp!! > ttl)

}

