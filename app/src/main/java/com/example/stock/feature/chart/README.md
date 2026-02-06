# チャート機能

## 概要

`chart`機能モジュールは、MPAndroidChartライブラリを使用して株価のローソク足チャートを表示する。
データ層、UI層、ViewModel層を明確に分離したMVVMアーキテクチャパターンに従う。

### 主な機能

- **ローソク足チャート**: 色分けされたローソクでOHLC（始値、高値、安値、終値）データを表示
- **出来高チャート**: ローソク足チャートの下に取引量を棒グラフで表示
- **チャート同期**: チャート間のズーム、スクロール、ハイライトの双方向同期
- **期間選択**: 日足、週足、月足のデータ間隔をサポート
- **ローディング状態**: データ取得中にローディングインジケータを表示
- **エラーハンドリング**: 文字列リソースを使用したローカライズされたエラーメッセージを表示

## 依存関係図

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              UI層                                        │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                       ChartScreen                               │    │
│  │  ┌───────────────┐  ┌─────────────────┐  ┌─────────────────┐    │    │
│  │  │ CandleUiState │  │ CandleChartView │  │ VolumeChartView │    │    │
│  │  └───────────────┘  └────────┬────────┘  └────────┬────────┘    │    │
│  │                              │                    │              │    │
│  │                              └────────┬───────────┘              │    │
│  │                                       │                          │    │
│  │                              ┌────────▼────────┐                 │    │
│  │                              │   ChartSync     │                 │    │
│  │                              │ （ズーム/スクロール │                 │    │
│  │                              │  を同期）        │                 │    │
│  │                              └─────────────────┘                 │    │
│  └──────────────────────────────┬──────────────────────────────────┘    │
└─────────────────────────────────┼───────────────────────────────────────┘
                                  │ observes
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           ViewModel層                                    │
│                    ┌─────────────────────┐                              │
│                    │  CandlesViewModel   │                              │
│                    │  - ui: StateFlow    │                              │
│                    │  - load()           │                              │
│                    │  - clear()          │                              │
│                    └──────────┬──────────┘                              │
└───────────────────────────────┼─────────────────────────────────────────┘
                                │ calls
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          Repository層                                    │
│                    ┌─────────────────────┐                              │
│                    │  CandleRepository   │                              │
│                    │  - candles: Flow    │                              │
│                    │  - fetchCandles()   │                              │
│                    │  - clearCandles()   │                              │
│                    └─────────┬───────────┘                              │
└──────────────────────────────┼──────────────────────────────────────────┘
                               │ depends on
                               ▼
                    ┌─────────────────────┐
                    │      ChartApi       │
                    │     (Retrofit)      │
                    │  - getCandles()     │
                    └─────────┬───────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │   バックエンドAPI     │
                    │ GET /candles/{code} │
                    └─────────────────────┘
```

## ディレクトリ構成

```
feature/chart/
├── data/
│   ├── remote/
│   │   └── ChartApi.kt          # Retrofitインターフェース + CandleDto
│   └── repository/
│       └── CandleRepository.kt  # ローソク足データを取得しキャッシュ
│
├── domain/
│   └── model/
│       └── Candle.kt            # ローソク足ドメインエンティティ
│
├── ui/
│   ├── CandleItem.kt            # ローソク足データ用UIモデル
│   ├── CandleUiState.kt         # チャート画面の状態データクラス
│   ├── ChartScreen.kt           # メインチャートComposable
│   └── chart/                   # チャートコンポーネントサブディレクトリ
│       ├── CandleChartView.kt   # ローソク足チャート（MPAndroidChart）
│       ├── VolumeChartView.kt   # 出来高バーチャート（MPAndroidChart）
│       ├── ChartSync.kt         # 双方向同期ロジック
│       ├── SyncChartsOnce.kt    # 同期セットアップ用Composableラッパー
│       ├── ChartStyle.kt        # チャートのスタイリングとテーマ
│       ├── ChartToken.kt        # デザイントークンと定数
│       └── ChartUtils.kt        # 軸フォーマットユーティリティ
│
├── viewmodel/
│   └── CandlesViewModel.kt      # チャートの状態とデータ読み込みを管理
│
└── README.md                    # このファイル
```

## チャート同期

チャート機能は、ローソク足チャートと出来高チャート間の高度な双方向同期を実装：

### 動作の仕組み

1. **AtomicBooleanロック**: `vpLock`と`hlLock`が無限コールバックループを防止
2. **ビューポート同期**: 一方のチャートをズーム/スクロールすると、もう一方が追従
3. **ハイライト同期**: ローソクを選択すると対応する出来高バーがハイライト
4. **慣性スクロール無効化**: `isDragDecelerationEnabled = false`でスクロールラグを防止

### 主要コンポーネント

| コンポーネント      | 目的                                          |
|-------------------|----------------------------------------------|
| `ChartSync.kt`    | `attachSynchronizedPair()`関数を含む          |
| `SyncChartsOnce`  | 1回限りの同期アタッチを保証するComposable        |
| `ChartStyle.kt`   | 両チャートに一貫したスタイリングを適用            |

## テスト

### テストの場所

テストは以下に配置：

| テスト種類 | 場所                                                     |
|----------|----------------------------------------------------------|
| 単体テスト | `app/src/test/java/com/example/stock/feature/chart/`     |

### テストファイル

#### 単体テスト

| ファイル                                        | 説明                                                   |
|------------------------------------------------|-------------------------------------------------------|
| `viewmodel/CandlesViewModelTest.kt`            | ロード/クリア/エラーハンドリングを含むCandlesViewModelのテスト |
| `data/repository/CandleRepositoryTest.kt`      | CandleRepositoryのAPI呼び出しテスト                      |

### テストの実行

```bash
# chart機能の全単体テストを実行
./gradlew testDebugUnitTest --tests "*.CandlesViewModelTest.*"
./gradlew testDebugUnitTest --tests "*.CandleRepositoryTest.*"
```

### テストカバレッジ

#### CandlesViewModelテスト

- ロード成功 - DTOをUIアイテムに変換しローディングをクリア
- ロード失敗（IOException）- ネットワークエラーリソースIDを設定
- 空のコードでロード - リポジトリを呼び出さずにバリデーションエラーを設定
- ロードは正しいパラメータでfetchCandlesを呼び出す
- クリアはUI状態をリセットし実行中のリクエストをキャンセル

#### CandleRepositoryテスト

- fetchCandlesはデータをStateFlowに保存
- clearCandlesはフローを空リストにリセット

### テストパターン

この機能では以下のテストパターンを使用：

1. **MainDispatcherRule**: `Dispatchers.Main`を`StandardTestDispatcher`に置換
2. **DispatcherProviderモック**: 決定論的テストのためテストディスパッチャを注入
3. **MockK**: シンプルなモック用に`relaxed = true`を使用するモックフレームワーク
4. **Truth**: `assertThat()`スタイルを使用するアサーションライブラリ

テストパターンの例：

```kotlin
@Test
fun `load sets error when repository throws IOException`() =
    runTest(mainRule.scheduler) {
        // given
        coEvery { repo.fetchCandles(any(), any(), any()) } throws IOException("network")

        // when
        vm.load("AAPL")
        advanceUntilIdle()

        // then
        val ui = vm.ui.value
        assertThat(ui.isLoading).isFalse()
        assertThat(ui.errorResId).isEqualTo(R.string.error_network)
        assertThat(ui.items).isEmpty()
    }
```
