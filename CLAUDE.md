# CLAUDE.md

このファイルは、このリポジトリのコードを扱う際にClaude Code（claude.ai/code）にガイダンスを提供します。

## プロジェクト概要

Kotlin + Jetpack Compose + MVVMアーキテクチャを使用したAndroid株価チャートビューアアプリ。JWT認証とGoバックエンドAPIで米国株のローソク足チャート（日足/週足/月足）を表示する。

## よく使うコマンド

### ビルド & 実行

```bash
# プロジェクトをビルド
./gradlew build

# デバッグバリアントをビルド
./gradlew assembleDebug

# ステージングバリアントをビルド
./gradlew assembleStaging

# リリースバリアントをビルド
./gradlew assembleRelease

# 接続されたデバイスにインストール
./gradlew installDebug
```

### テスト

```bash
# 全ユニットテストを実行
./gradlew testDebugUnitTest

# 特定のクラスのテストを実行
./gradlew testDebugUnitTest --tests "com.example.stock.feature.auth.viewmodel.LoginViewModelTest"

# カバレッジ付きでテストを実行
./gradlew testDebugUnitTest --tests "*.LoginViewModelTest.*"

# インストルメンテーションテストを実行（エミュレータ/デバイスが必要）
./gradlew connectedAndroidTest

# Lintチェックを実行
./gradlew lint
```

### クリーン & 同期

```bash
# ビルド成果物をクリーン
./gradlew clean

# プロジェクト依存関係を同期
./gradlew --no-daemon --stacktrace help
```

## アーキテクチャ

### MVVMパターン

- **UIレイヤー**（Compose）: 画面はStateFlow/SharedFlow経由でViewModelの状態を監視
- **ViewModelレイヤー**: UI状態とビジネスロジックを管理し、UiStateクラスを公開
- **Repositoryレイヤー**: データソースを調整し、DTOをUIモデルに変換
- **DataSourceレイヤー**: API呼び出し（Retrofit）とローカルストレージ（DataStore）

### 主要なアーキテクチャパターン

**UiStateパターン**: ViewModelは専用のUiStateクラス経由でドメインモデルを公開する（DTOを直接公開しない）。例: `CandleUiState`、`SymbolUiState`、`LoginUiState`。

**Hilt DI**: ViewModelは`@HiltViewModel`と`@Inject constructor`でHiltを使用した依存性注入を行う。リポジトリや`DispatcherProvider`などの依存関係は自動的に注入される。

**トークン管理**: `InMemoryTokenProvider`（メモリ上）+ `TokenStore`（DataStore永続化）の二層アプローチ。`AuthInterceptor`がAPIリクエストにJWT Bearerトークンを自動付与する。

### 画面遷移フロー

1. `LoginScreen`（Routes.LOGIN）→ メール/パスワードを検証 → `LoginViewModel.login()`を呼び出す
2. 成功時 → バックスタックをクリアして`StockListScreen`（Routes.STOCK）へ遷移
3. 銘柄を選択 → URLパラメータ付きで`chart/{name}/{code}`へ遷移
4. どの画面からでもログアウト → トークンをクリア → ログイン画面へ戻る

### チャート同期

アプリはMPAndroidChartを使用し、ローソク足チャートと出来高チャート間でカスタム同期を実装している：

- `ChartSync.kt`は`AtomicBoolean`ロックを使用して無限ループを防ぎながら、双方向のズーム/スクロール/ハイライト同期を実装
- ラグを防ぐため慣性スクロールを無効化（`isDragDecelerationEnabled = false`）
- `ui/chart/ChartSync.kt`の`attachSynchronizedPair()`関数を参照

### API設定

- **デバッグビルド**: `BASE_URL = "http://10.0.2.2:8080/"`（Androidエミュレータのlocalhost）
- **ステージングビルド**: `BASE_URL = "https://api.stockviewapp.com/"`
- **リリースビルド**: `BASE_URL = "https://api.stockviewapp.com/"`
- BASE_URLは`BuildConfig.BASE_URL`経由で公開され、`ApiConfig`からアクセスする

### テスト戦略

- 決定論的なコルーチンテストのため、`MainDispatcherRule`を使用して`Dispatchers.Main`を`StandardTestDispatcher`に置き換える
- 依存関係のモックにはMockKを使用（シンプルなケースでは`relaxed = true`）
- アサーションにはTruthライブラリを使用（`assertThat()`）
- パターン: リポジトリをモック → ViewModelに注入 → `ui.value`経由で状態変化を検証
- テストでは非同期操作の後に必ず`advanceUntilIdle()`を呼び出す

## CI/CD

GitHub Actionsは`main`へのプルリクエスト時に実行される：

- JDK 17をセットアップ
- `./gradlew lint`を実行
- `./gradlew testDebugUnitTest`を実行
- テストとLintレポートをアーティファクトとしてアップロード

## パッケージ構造

このプロジェクトは**フィーチャーベース**のパッケージ構造を採用しており、関連するコンポーネント（data、UI、ViewModel）はレイヤーごとではなくフィーチャーごとにグループ化されている。これによりモジュール性が向上し、各フィーチャーを独立して理解・修正しやすくなる。

```
com.example.stock/
├── feature/
│   ├── auth/                    # 認証フィーチャー
│   │   ├── data/
│   │   │   ├── remote/          # AuthApi（Retrofit）、AuthDto（リクエスト/レスポンスDTO）
│   │   │   └── repository/      # AuthRepository
│   │   ├── ui/
│   │   │   ├── login/           # LoginScreen、LoginUiState
│   │   │   └── signup/          # SignupScreen、SignupUiState
│   │   └── viewmodel/           # LoginViewModel、LogoutViewModel、SignupViewModel、ErrorHandler、InputValidator
│   ├── stocklist/               # 銘柄リストフィーチャー
│   │   ├── data/
│   │   │   ├── remote/          # SymbolApi（Retrofit）、SymbolDto
│   │   │   └── repository/      # SymbolRepository
│   │   ├── domain/model/        # Symbolエンティティ
│   │   ├── ui/                  # SymbolListScreen、SymbolUiState、SymbolItem
│   │   └── viewmodel/           # SymbolViewModel
│   └── chart/                   # チャート表示フィーチャー
│       ├── data/
│       │   ├── remote/          # ChartApi（Retrofit）、CandleDto
│       │   └── repository/      # CandleRepository
│       ├── domain/model/        # Candleエンティティ
│       ├── ui/                  # ChartScreen、CandleUiState、CandleItem
│       │   └── chart/           # CandleChartView、VolumeChartView、ChartSync、ChartStyle、ChartUtils、ChartToken、SyncChartsOnce
│       └── viewmodel/           # CandlesViewModel
├── core/
│   ├── data/                    # 共有データコンポーネント
│   │   ├── auth/                # TokenProviderインターフェース、InMemoryTokenProvider、AuthEventManager
│   │   └── local/               # TokenStore（DataStore永続化）
│   ├── network/                 # AuthInterceptor、TokenAuthenticator
│   ├── ui/
│   │   ├── component/           # 再利用可能なComposable（ヘッダー、ドロップダウン）
│   │   ├── theme/               # Material3テーマ、タイポグラフィ、ディメンション
│   │   └── util/                # ダブルクリック防止用ClickGuard
│   └── util/                    # 共有ユーティリティ（DispatcherProvider）
├── config/                      # ApiConfig（BuildConfigからのBASE_URL）
├── di/                          # Hiltモジュール（DataModule、NetworkModule、DispatcherModule）
└── navigation/                  # NavGraph
```

### フィーチャーモジュールの構成

各フィーチャーモジュール（`auth`、`stocklist`、`chart`）は一貫した構造に従う：

- **data/remote/**: ネットワーク通信用のRetrofit APIインターフェースとDTO（データ転送オブジェクト）
- **data/repository/**: データソースとビジネスロジックを調整するRepositoryクラス
- **domain/model/**: ドメインエンティティ（DTOとUI表示モデルの中間層）
- **ui/**: Composable画面、UI状態クラス、フィーチャー固有のUIコンポーネント
  - 特定の画面用にサブディレクトリを含む場合がある（例: `auth/ui/login/`）
  - 再利用可能なUIコンポーネント用にサブディレクトリを含む場合がある（例: `chart/ui/chart/`）
- **viewmodel/**: `@HiltViewModel`アノテーション付きのViewModelクラス

### 推奨パッケージ構造ガイドライン

新機能の追加や既存機能の修正時は、以下の規約に従う：

1. **APIインターフェース & DTO** → `feature/*/data/remote/`
   - Retrofit APIインターフェース
   - API通信に使用するリクエスト/レスポンスDTO
   - シリアライズ可能なデータクラス

2. **Repository** → `feature/*/data/repository/`
   - データ操作をオーケストレーションするRepositoryクラス
   - ビジネスロジックとデータ変換
   - StateFlow/SharedFlowパブリッシャー

3. **ドメインエンティティ** → `feature/*/domain/model/`
   - DTOから変換されたドメインモデル
   - UI表示モデルへの変換元となるエンティティ

4. **UI状態クラス** → `feature/*/ui/`
   - 画面状態を表す`*UiState`データクラス
   - 対応する画面と同じパッケージに配置

5. **Composable画面** → `feature/*/ui/`
   - メイン画面のComposable
   - 画面固有のUIコンポーネント
   - 必要に応じて画面名でサブディレクトリに整理

6. **ViewModel** → `feature/*/viewmodel/`
   - `@HiltViewModel`と`@Inject constructor`を持つViewModelクラス
   - 共有ユーティリティ（ErrorHandler、InputValidatorなど）も含む

この構造により以下が実現される：
- **関心の明確な分離**: API/DTO、ビジネスロジック、UIが明確に分離
- **容易なナビゲーション**: フィーチャーの全コンポーネントがまとめてグループ化
- **スケーラビリティ**: 新機能も同じパターンに従う
- **テスト容易性**: 依存関係が明確でモックしやすい

## 主要な実装メモ

- **Hilt DI**: ViewModelは依存性注入に`@HiltViewModel`と`@Inject constructor`を使用。
  Compose画面では`hiltViewModel()`を使用してViewModelインスタンスを取得する。
- **コルーチン**: 全てのAPI呼び出しは`viewModelScope.launch`とIOディスパッチャーを使用（`withContext(Dispatchers.IO)`）
- **状態管理**: Repositoryは内部で`MutableStateFlow`を使用し、リアクティブな更新のために`StateFlow`を公開する
- **エラーハンドリング**: ViewModelは例外をキャッチしてマッピング（HttpException → ユーザーフレンドリーなメッセージ、IOException → ネットワークエラー、SerializationException → JSONエラー）

## プルリクエストの作成

プルリクエストを作成する際は、以下を含む明確で簡潔なPR説明を記述する：

1. **概要**: PRが何をするか、なぜそれを行うかの簡潔な説明
2. **変更点**: 主な変更の箇条書きリスト
3. **テスト**: 変更がどのようにテストされたか（ユニットテスト、統合テスト、手動テストなど）
4. **レビューポイント**（任意）: レビュアーが注目すべき特定の領域や側面
