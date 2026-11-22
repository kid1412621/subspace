package me.nanova.subspace.domain.repo

import kotlinx.coroutines.flow.Flow

data class QBSession(val cookie: String, val savedAtMillis: Long)

interface SessionStorage {
    val activeAccountId: Flow<Long?>

    suspend fun getQBSession(accountId: Long): QBSession?

    /**
     * Sets the currently active account ID. Pass null to clear the active account.
     */
    suspend fun setActiveAccountId(accountId: Long?)

    suspend fun saveQBSession(accountId: Long, session: QBSession)

    suspend fun invalidateQBSession(accountId: Long, rejected: QBSession)

    /**
     * Clears all stored data for a specific account ID (cookie and timestamp).
     * Useful for logout or account deletion.
     */
    suspend fun clearAccountData(accountId: Long)

    /**
     * Clears all preferences. Use with caution (e.g., for app reset).
     */
    suspend fun clearAllData()

}
