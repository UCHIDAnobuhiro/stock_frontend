package com.example.stock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.stock.core.ui.theme.StockTheme
import com.example.stock.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint

/**
 * アプリケーションのエントリーポイントとなるActivity。
 * Hilt注入されたViewModelを使用したナビゲーショングラフをセットアップする。
 * @AndroidEntryPointアノテーションでHilt依存性注入を有効化。
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /**
     * Activity作成時の初期化処理。
     * ナビゲーショングラフをセットアップし、UIを構築する。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Hilt注入されたViewModelでアプリケーションナビゲーションをセットアップ
            StockTheme {
                AppNavGraph()
            }
        }
    }
}
