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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.nanova.subspace.domain.repo.QBSession
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

    override suspend fun getQBSession(accountId: Long): QBSession? =
        context.dataStore.data.first().sessionFor(accountId)

    private fun Preferences.sessionFor(accountId: Long): QBSession? {
        val cookie = this[qbCookiePreferenceKey(accountId)]?.takeIf { it.isNotBlank() }
            ?: return null
        val timestamp = this[qbCookieTimePreferenceKey(accountId)] ?: return null
        return QBSession(cookie, timestamp)
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

    override suspend fun saveQBSession(accountId: Long, session: QBSession) {
        context.dataStore.edit { preferences ->
            preferences[qbCookiePreferenceKey(accountId)] = session.cookie
            preferences[qbCookieTimePreferenceKey(accountId)] = session.savedAtMillis
        }
    }

    override suspend fun invalidateQBSession(accountId: Long, rejected: QBSession) {
        context.dataStore.edit { preferences ->
            if (preferences.sessionFor(accountId) == rejected) {
                preferences.remove(qbCookiePreferenceKey(accountId))
                preferences.remove(qbCookieTimePreferenceKey(accountId))
            }
        }
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
