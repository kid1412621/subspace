package me.nanova.subspace.data.api

import com.google.common.net.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.repo.QBSession
import me.nanova.subspace.domain.repo.SessionStorage
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QBCookieInterceptor @Inject constructor(
    private val sessionStorage: SessionStorage,
    private val authService: QBAuthService,
) : Interceptor {
    private val refreshMutex = Mutex()
    private val ttlMillis = 50 * 60 * 1000L

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val account = request.tag(Account::class.java) ?: return chain.proceed(request)
        val baseUrl = account.url.toHttpUrl()
        if (request.url.scheme != baseUrl.scheme ||
            request.url.host != baseUrl.host ||
            request.url.port != baseUrl.port
        ) {
            return chain.proceed(request)
        }

        val session = try {
            runBlocking(Dispatchers.IO) {
                sessionStorage.getQBSession(account.id)?.takeIf { it.isFresh() }
                    ?: refreshMutex.withLock {
                        sessionStorage.getQBSession(account.id)?.takeIf { it.isFresh() }
                            ?: refreshSession(account)
                    }
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to connect to server: ${e.message}", e)
        }

        val authenticatedRequest = request.newBuilder()
            .header(HttpHeaders.COOKIE, session.cookie)
            .build()
        val response = chain.proceed(authenticatedRequest)
        if (response.code == 403) {
            try {
                runBlocking(Dispatchers.IO) {
                    sessionStorage.invalidateQBSession(account.id, session)
                }
            } catch (e: Exception) {
                response.close()
                throw IOException("Failed to invalidate the qBittorrent session.", e)
            }
        }
        return response
    }

    private suspend fun refreshSession(account: Account): QBSession {
        val response = authService.login(
            "${account.url.trimEnd('/')}/api/v2/auth/login", account.user, account.pass
        )
        response.body()?.close()
        response.errorBody()?.close()
        if (!response.isSuccessful) {
            throw IOException(
                if (response.code() == 403) "Wrong username or password."
                else "Cannot connect to qBittorrent service (HTTP ${response.code()})."
            )
        }
        val cookie = response.headers()[HttpHeaders.SET_COOKIE]?.takeIf { it.isNotBlank() }
            ?: throw IOException("Failed to retrieve session cookie from server.")
        return QBSession(cookie, System.currentTimeMillis()).also {
            sessionStorage.saveQBSession(account.id, it)
        }
    }

    private fun QBSession.isFresh(): Boolean =
        System.currentTimeMillis() - savedAtMillis in 0 until ttlMillis
}
