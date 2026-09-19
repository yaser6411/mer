package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mercury_locale_preferences")

/**
 * Manages locale and language preference persistence using Jetpack DataStore Preferences.
 */
class LocalePreferencesManager(private val context: Context) {

    companion object {
        val KEY_APP_LANGUAGE = stringPreferencesKey("mercury_app_language")
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_ARABIC = "ar"
        const val LANGUAGE_SYSTEM = "SYSTEM"
    }

    /**
     * Flow emitting the currently persisted language code ("en", "ar", or "SYSTEM").
     */
    val appLanguage: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_APP_LANGUAGE] ?: LANGUAGE_SYSTEM
        }

    /**
     * Persists the selected language code into DataStore Preferences.
     */
    suspend fun setAppLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_APP_LANGUAGE] = languageCode
        }
    }
}
