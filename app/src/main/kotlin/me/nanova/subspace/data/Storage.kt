package me.nanova.subspace.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class Storage @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val CURRENT_ACCOUNT_ID = longPreferencesKey("account")
        val QT_COOKIE_KEY = stringPreferencesKey("qt-cookie")
        val QT_COOKIE_TIME_KEY = longPreferencesKey("qt-cookie-time")
    }


    val currentAccountId: Flow<Long?> = dataStore.data
        .map { preferences ->
            preferences[CURRENT_ACCOUNT_ID]
        }
    val qtCookie: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[QT_COOKIE_KEY]
        }
    val qtCookieTime: Flow<Long?> = dataStore.data
        .map { preferences ->
            preferences[QT_COOKIE_TIME_KEY]
        }

    suspend fun saveCurrentAccountId(id: Long) {
        dataStore.edit { preferences ->
            preferences[CURRENT_ACCOUNT_ID] = id
        }
    }
    suspend fun saveQtCookie(cookie: String) {
        dataStore.edit { preferences ->
            preferences[QT_COOKIE_KEY] = cookie
        }
    }
    suspend fun updateQtCookieTime() {
        dataStore.edit { preferences ->
            preferences[QT_COOKIE_TIME_KEY] = System.currentTimeMillis()
        }
    }
}