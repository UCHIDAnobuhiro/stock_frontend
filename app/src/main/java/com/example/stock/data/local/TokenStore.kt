package com.example.stock.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.stock.data.local.TokenStoreKeys.TOKEN
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * アプリケーションの [Context] に対して、JWT トークンを保存するための DataStore を提供する拡張プロパティ。
 * DataStore のファイル名は "token"。
 */
private val Context.dataStore by preferencesDataStore(name = "token")

/**
 * DataStore に保存するキーをまとめたオブジェクト。
 * - [TOKEN]: JWT アクセストークンを保存するためのキー。
 */
object TokenStoreKeys {
    val TOKEN = stringPreferencesKey("jwt_token")
}

/**
 * JWT トークンを DataStore を通じて永続化するためのクラス。
 * このクラスを利用することで、アプリ内でトークンの保存・取得・削除が可能になる。
 * @property context アプリケーションコンテキスト。DataStore にアクセスするために使用される。
 */
class TokenStore(private val context: Context) {
    private val appContext = context.applicationContext

    val tokenFlow: Flow<String?> = appContext.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs -> prefs[TokenStoreKeys.TOKEN] }

    suspend fun save(token: String) {
        appContext.dataStore.edit { it[TOKEN] = token }
    }

    suspend fun clear() {
        appContext.dataStore.edit { it.remove(TOKEN) }
    }
}