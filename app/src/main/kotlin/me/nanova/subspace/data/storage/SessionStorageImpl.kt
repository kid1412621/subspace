package me.nanova.subspace.data.storage

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.nanova.subspace.domain.repo.SessionStorage
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

@Singleton
class SessionStorageImpl @Inject constructor(@ApplicationContext private val context: Context) :
    SessionStorage {

    companion object {
        // Key to store the ID of the currently *active* account (e.g., the one the user is currently interacting with)
        val ACTIVE_ACCOUNT_ID_KEY = longPreferencesKey("active_account_id")

        // Helper to generate dynamic preference keys for cookies and their timestamps
        fun qbCookiePreferenceKey(accountId: Long) = stringPreferencesKey("qb_cookie_$accountId")
        fun qbCookieTimePreferenceKey(accountId: Long) =
            longPreferencesKey("qb_cookie_time_$accountId")

        private const val TAG = "Storage"
    }

    /**
     * Flow emitting the ID of the currently active account, or null if no account is active.
     */
    override val activeAccountId: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[ACTIVE_ACCOUNT_ID_KEY]
        }

    /**
     * Flow emitting the QB Cookie for the *currently active* account.
     * Emits null if no account is active or if the active account has no cookie.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override val qbCookie: Flow<String?> = activeAccountId.flatMapLatest { accountId ->
        if (accountId == null) {
            Log.d(TAG, "No active account ID, qbCookie emitting null.")
            flowOf(null) // No active account, so no specific cookie
        } else {
            Log.d(TAG, "Active account ID changed to $accountId, observing its cookie.")
            context.dataStore.data.map { preferences ->
                preferences[qbCookiePreferenceKey(accountId)]
            }
        }
    }

    /**
     * Flow emitting the QB Cookie timestamp for the *currently active* account.
     * Emits null if no account is active or if the active account has no cookie timestamp.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override val qbCookieTime: Flow<Long?> = activeAccountId.flatMapLatest { accountId ->
        if (accountId == null) {
            Log.d(TAG, "No active account ID, qbCookieTime emitting null.")
            flowOf(null) // No active account, so no specific timestamp
        } else {
            Log.d(TAG, "Active account ID changed to $accountId, observing its cookie time.")
            context.dataStore.data.map { preferences ->
                preferences[qbCookieTimePreferenceKey(accountId)]
            }
        }
    }

    /**
     * Sets the currently active account ID. Pass null to clear the active account.
     */
    override suspend fun setActiveAccountId(accountId: Long?) {
        context.dataStore.edit { preferences ->
            if (accountId == null) {
                preferences.remove(ACTIVE_ACCOUNT_ID_KEY)
                Log.i(TAG, "Active account ID cleared.")
            } else {
                preferences[ACTIVE_ACCOUNT_ID_KEY] = accountId
                Log.i(TAG, "Active account ID set to: $accountId")
            }
        }
    }

    /**
     * Saves the QB Cookie for a specific account ID.
     */
    override suspend fun saveQBCookie(accountId: Long, cookie: String) {
        context.dataStore.edit { preferences ->
            preferences[qbCookiePreferenceKey(accountId)] = cookie
        }
        Log.i(TAG, "Saved cookie for account $accountId: ${cookie.take(10)}...")
    }

    /**
     * Updates the QB Cookie timestamp for a specific account ID to the current time.
     */
    override suspend fun updateQBCookieTime(accountId: Long) {
        val currentTime = System.currentTimeMillis()
        context.dataStore.edit { preferences ->
            preferences[qbCookieTimePreferenceKey(accountId)] = currentTime
        }
        Log.i(TAG, "Updated cookie time for account $accountId to $currentTime.")
    }

    /**
     * Clears all stored data for a specific account ID (cookie and timestamp).
     * Useful for logout or account deletion.
     */
    override suspend fun clearAccountData(accountId: Long) {
        context.dataStore.edit { preferences ->
            preferences.remove(qbCookiePreferenceKey(accountId))
            preferences.remove(qbCookieTimePreferenceKey(accountId))
            // If this account was the active one, clear the active account ID as well
            if (preferences[ACTIVE_ACCOUNT_ID_KEY] == accountId) {
                preferences.remove(ACTIVE_ACCOUNT_ID_KEY)
                Log.i(
                    TAG,
                    "Cleared active account ID as it matched the account being cleared: $accountId"
                )
            }
        }
        Log.i(TAG, "Cleared all data for account $accountId.")
    }

    /**
     * Clears all preferences. Use with caution (e.g., for app reset).
     */
    override suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        Log.w(TAG, "All preference data cleared.")
    }
}
