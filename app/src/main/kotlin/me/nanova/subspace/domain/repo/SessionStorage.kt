package me.nanova.subspace.domain.repo

import kotlinx.coroutines.flow.Flow

interface SessionStorage {
    val activeAccountId: Flow<Long?>

    val qbCookie: Flow<String?>

    val qbCookieTime: Flow<Long?>

    /**
     * Sets the currently active account ID. Pass null to clear the active account.
     */
    suspend fun setActiveAccountId(accountId: Long?)

    /**
     * Saves the QB Cookie for a specific account ID.
     */
    suspend fun saveQBCookie(accountId: Long, cookie: String)

    /**
     * Updates the QB Cookie timestamp for a specific account ID to the current time.
     */
    suspend fun updateQBCookieTime(accountId: Long)

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