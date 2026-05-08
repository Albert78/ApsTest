package de.dh.raaps

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.dh.raaps.AppStateRepository.Companion.USER_DECLINED_PERMISSIONS_KEY
import de.dh.raaps.common.ui.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val Preferences?.themeMode: ThemeMode
    get() = this?.get(AppStateRepository.THEME_MODE_KEY)?.let { ThemeMode.fromValue(it) } ?: ThemeMode.SYSTEM

val Preferences?.userDeclinedPermissions: Boolean
    get() = this?.get(USER_DECLINED_PERMISSIONS_KEY) ?: false

class AppStateRepository(private val context: Context, private val scope: CoroutineScope) {
    /**
     * Gets the eagerly loaded state of the preferences as StateFlow, i.e. it can be queried
     * without the use of a suspend function, but the value will initially be {@ null} until
     * the preferences are loaded.
     */
    val cachedPreferences: StateFlow<Preferences?> = context.dataStore.data
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    /**
     * Gets the current preferences as a suspend function.
     */
    suspend fun getPreferences(): Preferences {
        return cachedPreferences.filterNotNull().first()
    }

    suspend fun setThemeMode(value: ThemeMode) {
        context.dataStore.edit { mutablePreferences ->
            mutablePreferences[THEME_MODE_KEY] = value.value
        }
    }

    suspend fun setUserDeclinedPermissions(value: Boolean) {
        context.dataStore.edit { mutablePreferences ->
            mutablePreferences[USER_DECLINED_PERMISSIONS_KEY] = value
        }
    }

    companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val USER_DECLINED_PERMISSIONS_KEY = booleanPreferencesKey("user_declined_permissions")
    }
}