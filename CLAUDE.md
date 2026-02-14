# CLAUDE.md

このファイルは、このリポジトリのコードを扱う際にClaude Code（claude.ai/code）にガイダンスを提供します。

## プロジェクト概要

Kotlin + Jetpack Compose + MVVMアーキテクチャを使用したAndroid株価チャートビューアアプリ。JWT認証とGoバックエンドAPIで米国株のローソク足チャート（日足/週足/月足）を表示する。

- **minSdk**: 26 / **targetSdk**: 35 / **compileSdk**: 35
- **Kotlin**: 2.3.0 / **Compose BOM**: 2026.01.00
- **主要ライブラリ**: Hilt（DI）、Retrofit（HTTP）、MPAndroidChart（チャート描画）、DataStore（永続化）、MockK（テスト）、Truth（アサーション）
- **バージョン管理**: `libs.versions.toml` でライブラリバージョンを一元管理

## よく使うコマンド

### ビルド & 実行

```bash
./gradlew assembleDebug      # デバッグビルド（ローカル開発用）
./gradlew assembleStaging     # ステージングビルド（ステージング環境検証用）
./gradlew assembleRelease     # リリースビルド（本番配布用・署名設定が必要）
./gradlew installDebug        # 接続デバイスにインストール
```

### テスト

```bash
./gradlew testDebugUnitTest                                                          # 全ユニットテスト
./gradlew testDebugUnitTest --tests "com.example.stock.feature.auth.viewmodel.LoginViewModelTest"  # 特定クラス
./gradlew testDebugUnitTest --tests "*.LoginViewModelTest.*"                          # パターン指定
./gradlew connectedAndroidTest                                                        # インストルメンテーションテスト（要エミュレータ）
./gradlew lint                                                                        # Lintチェック
```

### クリーン & 同期

```bash
./gradlew clean
./gradlew --no-daemon --stacktrace help
```

## アーキテクチャ

### MVVMパターン

- **UIレイヤー**（Compose）: StateFlow/SharedFlow経由でViewModelの状態を監視
- **ViewModelレイヤー**: UI状態とビジネスロジックを管理し、UiStateクラスを公開
- **Repositoryレイヤー**: データソースを調整し、DTOをUIモデルに変換
- **DataSourceレイヤー**: API呼び出し（Retrofit）とローカルストレージ（DataStore）

### 主要なアーキテクチャパターン

**UiStateパターン**: ViewModelは専用のUiStateクラス経由でドメインモデルを公開する（DTOを直接公開しない）。例: `CandleUiState`、`SymbolUiState`、`LoginUiState`。

**Hilt DI**: ViewModelは`@HiltViewModel`と`@Inject constructor`でHiltを使用した依存性注入を行う。Compose画面では`hiltViewModel()`でインスタンスを取得する。

**トークン管理**: `InMemoryTokenProvider`（メモリ上）+ `TokenStore`（DataStore永続化）の二層アプローチ。`AuthInterceptor`がAPIリクエストにJWT Bearerトークンを自動付与する。

### 画面遷移フロー

1. `LoginScreen`（Routes.LOGIN）→ メール/パスワードを検証 → `LoginViewModel.login()`を呼び出す
2. `SignupScreen`（Routes.SIGNUP）→ 新規ユーザー登録 → `SignupViewModel.signup()`を呼び出す
3. 認証成功時 → バックスタックをクリアして`StockListScreen`（Routes.STOCK）へ遷移
4. 銘柄を選択 → URLパラメータ付きで`chart/{name}/{code}`へ遷移
5. どの画面からでもログアウト → トークンをクリア → ログイン画面へ戻る

### チャート同期

MPAndroidChartを使用し、ローソク足チャートと出来高チャート間でカスタム同期を実装：
- `ChartSync.kt`は`AtomicBoolean`ロックを使用して無限ループを防ぎながら、双方向のズーム/スクロール/ハイライト同期を実装
- ラグを防ぐため慣性スクロールを無効化（`isDragDecelerationEnabled = false`）
- 参照: `ui/chart/ChartSync.kt`の`attachSynchronizedPair()`

### API設定

| ビルドバリアント | BASE_URL | 用途 |
|-----------------|----------|------|
| debug | `http://10.0.2.2:8080/` | ローカル開発（エミュレータ + ローカルGoサーバー） |
| staging | `https://api.stockviewapp.com/` | ステージング環境での動作検証 |
| release | `https://api.stockviewapp.com/` | 本番配布（署名設定が必要） |

BASE_URLは`BuildConfig.BASE_URL`経由で公開され、`ApiConfig`からアクセスする。

### 主要APIエンドポイント

| メソッド | パス | 説明 |
|---------|------|------|
| POST | `/auth/login` | ログイン（JWT取得） |
| POST | `/auth/signup` | ユーザー登録 |
| GET | `/symbols` | 銘柄一覧取得 |
| GET | `/candles?code={code}&period={period}` | ローソク足データ取得（period: daily/weekly/monthly） |

### エラーハンドリング

ViewModelレイヤーで統一的にエラーをキャッチしてマッピングする。`ErrorHandler`（`feature/auth/viewmodel/`）が共通ロジックを提供：
- `HttpException` → ステータスコード別のユーザーフレンドリーなメッセージ
- `IOException` → ネットワークエラー
- `SerializationException` → JSONパースエラー

### テスト戦略

- `MainDispatcherRule`で`Dispatchers.Main`を`StandardTestDispatcher`に置き換え
- MockKでモック作成（シンプルなケースでは`relaxed = true`）
- Truthライブラリでアサーション（`assertThat()`）
- パターン: リポジトリをモック → ViewModelに注入 → `ui.value`経由で状態変化を検証
- 非同期操作の後に必ず`advanceUntilIdle()`を呼び出す

テスト生成の詳細なルールは `/test-generate` スキル（`.claude/skills/test-generate/SKILL.md`）を参照。

## CI/CD

GitHub Actionsは`main`へのプルリクエスト時に実行される：
- JDK 17をセットアップ → `./gradlew lint` → `./gradlew testDebugUnitTest`
- テストとLintレポートをアーティファクトとしてアップロード

## パッケージ構造

フィーチャーベースのパッケージ構造を採用。関連コンポーネントはレイヤーごとではなくフィーチャーごとにグループ化。

```text
com.example.stock/
├── feature/
│   ├── auth/                    # 認証フィーチャー
│   │   ├── data/remote/         # AuthApi, AuthDto
│   │   ├── data/repository/     # AuthRepository
│   │   ├── ui/login/            # LoginScreen, LoginUiState
│   │   ├── ui/signup/           # SignupScreen, SignupUiState
│   │   └── viewmodel/           # LoginViewModel, LogoutViewModel, SignupViewModel, ErrorHandler, InputValidator
│   ├── stocklist/               # 銘柄リストフィーチャー
│   │   ├── data/remote/         # SymbolApi, SymbolDto
│   │   ├── data/repository/     # SymbolRepository
│   │   ├── domain/model/        # Symbolエンティティ
│   │   ├── ui/                  # SymbolListScreen, SymbolUiState, SymbolItem
│   │   └── viewmodel/           # SymbolViewModel
│   └── chart/                   # チャート表示フィーチャー
│       ├── data/remote/         # ChartApi, CandleDto
│       ├── data/repository/     # CandleRepository
│       ├── domain/model/        # Candleエンティティ
│       ├── ui/                  # ChartScreen, CandleUiState, CandleItem
│       ├── ui/chart/            # CandleChartView, VolumeChartView, ChartSync, ChartStyle, ChartUtils, ChartToken, SyncChartsOnce
│       └── viewmodel/           # CandlesViewModel
├── core/
│   ├── data/auth/               # TokenProviderインターフェース, InMemoryTokenProvider, AuthEventManager
│   ├── data/local/              # TokenStore（DataStore永続化）
│   ├── network/                 # AuthInterceptor, TokenAuthenticator
│   ├── ui/component/            # 再利用可能なComposable（ヘッダー、ドロップダウン）
│   ├── ui/theme/                # Material3テーマ、タイポグラフィ、ディメンション
│   ├── ui/util/                 # ダブルクリック防止用ClickGuard
│   └── util/                    # DispatcherProvider
├── config/                      # ApiConfig（BuildConfigからのBASE_URL）
├── di/                          # Hiltモジュール（DataModule, NetworkModule, DispatcherModule）
└── navigation/                  # NavGraph
```

### 新機能追加時の配置ルール

各フィーチャーは同じ構造に従う：
- **data/remote/**: Retrofit APIインターフェース + DTO
- **data/repository/**: Repositoryクラス（データ操作、ビジネスロジック、StateFlow公開）
- **domain/model/**: ドメインエンティティ（DTOとUI表示モデルの中間層）
- **ui/**: Composable画面 + `*UiState`データクラス（対応する画面と同じパッケージ）
- **viewmodel/**: `@HiltViewModel` + `@Inject constructor`を持つViewModelクラス

## 主要な実装メモ

- **コルーチン**: 全API呼び出しは`viewModelScope.launch`とIOディスパッチャーを使用（`withContext(Dispatchers.IO)`）
- **状態管理**: Repositoryは内部で`MutableStateFlow`を使用し、リアクティブな更新のために`StateFlow`を公開
- **コード規約**: Kotlinの標準命名規則に準拠。フォーマットはAndroid Studioのデフォルト設定を使用

## スキル（Claude Code）

以下のスキルが `.claude/skills/` に定義されており、スラッシュコマンドで呼び出せる：

| コマンド | 説明 |
|---------|------|
| `/commit` | 変更内容を確認し、日本語でコミットメッセージを自動生成してコミット |
| `/pull-request` | 現在のブランチの変更内容を分析し、日本語でPRを作成 |
| `/test-generate` | 指定ファイルまたは変更差分に対してテストコードを生成 |
| `/code-check` | コミット前にコードの品質をレビュー（変更は行わない） |

### スキルの推奨ワークフロー

コミット時は以下の順序で実行すること：
1. `/code-check` でコード品質を確認（問題があれば修正）
2. `/commit` で日本語コミットメッセージを生成してコミット

PR作成時：
1. `/pull-request` で日本語のPRタイトル・説明を自動生成

テスト作成・修正時：
- `/test-generate` のルール（`.claude/skills/test-generate/SKILL.md`）に従う

## コミット・PR作成の言語ルール

コミットメッセージおよびプルリクエストのタイトル・説明はすべて**日本語**で記述すること。