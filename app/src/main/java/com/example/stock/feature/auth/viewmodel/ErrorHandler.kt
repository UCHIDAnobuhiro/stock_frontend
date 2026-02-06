package com.example.stock.feature.auth.viewmodel

import androidx.annotation.StringRes
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

/**
 * 認証関連エラーを処理するユーティリティオブジェクト。
 *
 * ログ出力やエラーメッセージのマッピングを含む、
 * 認証操作の共通エラー処理ロジックを提供する。
 */
object ErrorHandler {

    /**
     * シンプルなメッセージ形式でエラーをログ出力する。
     *
     * @param exception ログ出力する例外
     * @param operation 失敗した操作を説明する文字列
     */
    fun logError(exception: Throwable, operation: String) {
        val logMessage = when (exception) {
            is HttpException -> "HTTP error: ${exception.code()} - ${exception.message()}"
            is IOException -> "Network error: ${exception.message}"
            is SerializationException -> "JSON parse error: ${exception.message}"
            else -> "Unknown error: ${exception.message}"
        }
        Timber.e(exception, "$operation failed: $logMessage")
    }

    /**
     * 例外をその種類に基づいて文字列リソースIDにマッピングする。
     *
     * @param exception マッピングする例外
     * @param httpErrorMapper HTTPステータスコードを文字列リソースIDにマッピングする関数
     * @param defaultErrorResId 特定のマッピングが存在しない場合に使用するデフォルトのエラーリソースID
     * @return エラーメッセージの文字列リソースID
     */
    fun mapErrorToResource(
        exception: Throwable,
        httpErrorMapper: ((HttpException) -> Int)? = null,
        @StringRes defaultErrorResId: Int
    ): Int {
        return when (exception) {
            is HttpException -> httpErrorMapper?.invoke(exception) ?: defaultErrorResId
            else -> defaultErrorResId
        }
    }
}
