package com.example.stock.core.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.stock.core.data.local.TokenStoreKeys.TOKEN
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Extension property that provides a DataStore for saving JWT tokens to the application [Context].
 * The DataStore file name is "token".
 */
private val Context.dataStore by preferencesDataStore(name = "token")

/**
 * Object that groups together the keys stored in DataStore.
 * - [TOKEN]: Key for storing the JWT access token.
 */
object TokenStoreKeys {
    val TOKEN = stringPreferencesKey("jwt_token")
}

/**
 * Class for persisting JWT tokens through DataStore.
 * Using this class enables saving, retrieving, and deleting tokens within the application.
 * @property context Application context. Used to access DataStore.
 */
class TokenStore(private val context: Context) {
    private val appContext = context.applicationContext

    val tokenFlow: Flow<String?> = appContext.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { it[TOKEN] }

    suspend fun save(token: String) {
        appContext.dataStore.edit { it[TOKEN] = token }
    }

    suspend fun clear() {
        appContext.dataStore.edit { it.remove(TOKEN) }
    }
}