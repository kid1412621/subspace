package me.nanova.subspace.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.nanova.subspace.domain.model.Account
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class Storage @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val CURRENT_ACCOUNT_ID = longPreferencesKey("account")
        val QB_COOKIE_KEY = stringPreferencesKey("qb-cookie")
        val QB_COOKIE_TIME_KEY = longPreferencesKey("qb-cookie-time")
    }


    val currentAccountId: Flow<Long?> = dataStore.data
        .map { preferences ->
            preferences[CURRENT_ACCOUNT_ID]
        }
    val qbCookie: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[QB_COOKIE_KEY]
        }
    val qbCookieTime: Flow<Long?> = dataStore.data
        .map { preferences ->
            preferences[QB_COOKIE_TIME_KEY]
        }

    suspend fun updateCurrentAccountId(account: Account?) {
        dataStore.edit { preferences ->
            account?.let {
                preferences[CURRENT_ACCOUNT_ID] = it.id
            } ?: run {
                preferences.clear()
            }
        }
    }

    suspend fun updateCurrentAccountId(id: Long) {
        dataStore.edit { preferences ->
            preferences[CURRENT_ACCOUNT_ID] = id
        }
    }

    suspend fun saveQBCookie(cookie: String) {
        dataStore.edit { preferences ->
            preferences[QB_COOKIE_KEY] = cookie
        }
    }

    suspend fun updateQBCookieTime() {
        dataStore.edit { preferences ->
            preferences[QB_COOKIE_TIME_KEY] = System.currentTimeMillis()
        }
    }
}