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
 * JWTトークンを保存するためのDataStoreを[Context]に提供する拡張プロパティ。
 * DataStoreのファイル名は"token"。
 */
private val Context.dataStore by preferencesDataStore(name = "token")

/**
 * DataStoreに保存するキーをまとめたオブジェクト。
 * - [TOKEN]: JWTアクセストークンを保存するためのキー。
 */
object TokenStoreKeys {
    val TOKEN = stringPreferencesKey("jwt_token")
}

/**
 * DataStoreを通じてJWTトークンを永続化するクラス。
 * このクラスを使用することで、アプリ内でのトークンの保存・取得・削除が可能になる。
 * @property context アプリケーションコンテキスト。DataStoreへのアクセスに使用。
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