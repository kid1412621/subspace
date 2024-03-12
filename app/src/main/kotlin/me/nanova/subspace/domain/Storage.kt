package me.nanova.subspace.domain

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
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
@Singleton
class PreferenceStorage @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val QT_COOKIE_KEY = stringPreferencesKey("qt-cookie")
        val QT_COOKIE_TIME_KEY = longPreferencesKey("qt-cookie-time")
    }

    val qtCookie: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[QT_COOKIE_KEY]
        }

    val qtCookieTime: Flow<Long?> = dataStore.data
        .map { preferences ->
            preferences[QT_COOKIE_TIME_KEY]
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