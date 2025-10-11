package com.example.stock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.stock.data.repository.StockRepository
import com.example.stock.navigation.AppNavGraph
import com.example.stock.ui.factory.AuthViewModelFactory
import com.example.stock.ui.factory.CandlesViewModelFactory
import com.example.stock.ui.factory.SymbolViewModelFactory
import com.example.stock.ui.theme.StockTheme
import com.example.stock.viewmodel.AuthViewModel
import com.example.stock.viewmodel.CandlesViewModel
import com.example.stock.viewmodel.SymbolViewModel

/**
 * アプリのエントリーポイントとなるActivity。
 * 各ViewModelの初期化と、ナビゲーショングラフのセットアップを行う。
 */
class MainActivity : ComponentActivity() {
    /**
     * 株価データ取得用リポジトリ。必要時に初期化される。
     */
    private val stockRepo by lazy { StockRepository() }

    /**
     * 認証関連のViewModel。
     */
    private val authVm by viewModels<AuthViewModel> {
        AuthViewModelFactory(applicationContext)
    }

    /**
     * 銘柄リスト管理用のViewModel。
     */
    private val symbolVm by viewModels<SymbolViewModel> {
        SymbolViewModelFactory(stockRepo)
    }

    /**
     * ローソク足データ管理用のViewModel。
     */
    private val candlesVm by viewModels<CandlesViewModel> {
        CandlesViewModelFactory(stockRepo)
    }

    /**
     * Activity生成時の初期化処理。
     * ナビゲーショングラフをセットし、UIを構築する。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // アプリのナビゲーションをセットアップ
            StockTheme {
                AppNavGraph(authVm, symbolVm, candlesVm)
            }
        }
    }
}
