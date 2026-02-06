package com.example.stock.config

import com.example.stock.BuildConfig

/**
 * API設定オブジェクト。
 *
 * BuildConfigからAPI設定値への集約的なアクセスを提供する。
 * ビルドバリアント（debug、staging、release）ごとに異なるBASE_URL値を持つ。
 */
object ApiConfig {
    const val BASE_URL: String = BuildConfig.BASE_URL
}