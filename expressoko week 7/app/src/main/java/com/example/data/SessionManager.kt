package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStorePrefs: DataStore<Preferences> by preferencesDataStore(name = "expressoko_prefs")

class SessionManager(private val context: Context) {

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val PHONE_NUMBER = stringPreferencesKey("phone_number")
        private val USER_TYPE = stringPreferencesKey("user_type")
        private val LANGUAGE_TAG = stringPreferencesKey("language_tag")
        private val SELECTED_ROLE = stringPreferencesKey("selected_role")
        private val APP_COLOR = stringPreferencesKey("app_color")
    }

    val appColor: Flow<String> = context.dataStorePrefs.data.map { preferences ->
        preferences[APP_COLOR] ?: "Blue"
    }

    val selectedRole: Flow<String?> = context.dataStorePrefs.data.map { preferences ->
        preferences[SELECTED_ROLE]
    }

    val isLoggedIn: Flow<Boolean> = context.dataStorePrefs.data.map { preferences ->
        preferences[IS_LOGGED_IN] == true
    }

    val userId: Flow<String?> = context.dataStorePrefs.data.map { preferences ->
        preferences[USER_ID]
    }

    val userName: Flow<String?> = context.dataStorePrefs.data.map { preferences ->
        preferences[USER_NAME]
    }

    val phoneNumber: Flow<String?> = context.dataStorePrefs.data.map { preferences ->
        preferences[PHONE_NUMBER]
    }

    val userType: Flow<String?> = context.dataStorePrefs.data.map { preferences ->
        preferences[USER_TYPE]
    }

    val languageTag: Flow<String> = context.dataStorePrefs.data.map { preferences ->
        preferences[LANGUAGE_TAG] ?: "en"
    }

    suspend fun saveSession(id: String, name: String, phone: String, type: String) {
        context.dataStorePrefs.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_ID] = id
            preferences[USER_NAME] = name
            preferences[PHONE_NUMBER] = phone
            preferences[USER_TYPE] = type
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStorePrefs.edit { preferences ->
            preferences[LANGUAGE_TAG] = lang
        }
    }

    suspend fun setAppColor(color: String) {
        context.dataStorePrefs.edit { preferences ->
            preferences[APP_COLOR] = color
        }
    }

    suspend fun saveSelectedRole(role: String) {
        context.dataStorePrefs.edit { preferences ->
            preferences[SELECTED_ROLE] = role
        }
    }

    suspend fun clearSession() {
        context.dataStorePrefs.edit { preferences ->
            preferences[IS_LOGGED_IN] = false
            preferences[USER_ID] = ""
            preferences[SELECTED_ROLE] = ""
            // We intentionally do not clear PHONE_NUMBER and USER_TYPE and USER_NAME
            // so they can be shown on the login screen after logout.
        }
    }
}
